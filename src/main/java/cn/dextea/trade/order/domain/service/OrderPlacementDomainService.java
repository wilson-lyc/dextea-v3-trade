package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.enums.DiningMethodEnum;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.aggregate.Customer;
import cn.dextea.trade.order.domain.model.aggregate.Product;
import cn.dextea.trade.order.domain.model.aggregate.Store;
import cn.dextea.trade.order.domain.model.valueobject.CatalogSnapshot;
import cn.dextea.trade.order.domain.model.valueobject.Customization;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import cn.dextea.trade.order.domain.model.valueobject.PricedOrderItem;
import cn.dextea.trade.order.domain.model.valueobject.ProductCover;
import cn.dextea.trade.order.domain.model.valueobject.SkuSelection;
import cn.dextea.trade.order.domain.model.valueobject.UnavailableCustomization;
import cn.dextea.trade.order.domain.model.valueobject.UnavailableProduct;
import cn.dextea.trade.shared.domain.error.BizError;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class OrderPlacementDomainService {

    private static final int ALIPAY_PLATFORM_CODE = 2;

    public PreBuildResult preBuild(List<SkuSelection> selections, CatalogSnapshot snapshot) {
        assertStoreAvailable(snapshot.getStore());
        assertCustomerAvailable(snapshot.getCustomer());

        List<UnavailableProduct> unavailableProducts = new ArrayList<>();
        List<UnavailableCustomization> unavailableOptions = new ArrayList<>();
        Set<Long> reportedProductIds = new LinkedHashSet<>();
        Set<Long> reportedOptionIds = new LinkedHashSet<>();
        List<PricedOrderItem> availableProducts = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (SkuSelection selection : selections) {
            Long productId = selection.getProductId();
            Product product = snapshot.product(productId);

            if (!isProductAvailable(product, snapshot)) {
                if (reportedProductIds.add(productId)) {
                    unavailableProducts.add(UnavailableProduct.builder()
                            .id(productId)
                            .name(product != null ? product.getName() : null)
                            .build());
                }
                continue;
            }

            List<UnavailableCustomization> badOptions = collectUnavailableOptions(selection, product, snapshot);
            if (!badOptions.isEmpty()) {
                for (UnavailableCustomization bad : badOptions) {
                    if (reportedOptionIds.add(bad.getOptionId())) {
                        unavailableOptions.add(bad);
                    }
                }
                continue;
            }

            PricedOrderItem priced = priceItem(selection, product, snapshot);
            totalQuantity += priced.getQuantity();
            totalPrice = totalPrice.add(priced.getSubtotal());
            availableProducts.add(priced);
        }

        return PreBuildResult.builder()
                .unavailableProducts(unavailableProducts)
                .unavailableCustomizations(unavailableOptions)
                .products(availableProducts)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    public void validatePlacement(int platformCode, Integer diningMethodCode) {
        if (!isPlatformSupported(platformCode)) {
            throw new BizError(OrderErrorCode.PAY_PLATFORM_NOT_SUPPORTED, "暂不支持的支付方式: " + platformCode);
        }
        if (DiningMethodEnum.of(diningMethodCode) == null) {
            throw new BizError(OrderErrorCode.DINING_METHOD_INVALID, "用餐方式错误: " + diningMethodCode);
        }
    }

    public boolean needsImmediatePayment(int payMethodCode) {
        return ALIPAY_PLATFORM_CODE == payMethodCode;
    }

    public String resolveBuyerOpenId(Customer customer) {
        if (customer == null || customer.getAlipayOpenId() == null) {
            throw new BizError(OrderErrorCode.ALIPAY_BUYER_NOT_BOUND, "顾客未绑定支付宝，无法创建支付");
        }
        return customer.getAlipayOpenId();
    }

    public void assertStoreAvailable(Store store) {
        if (store == null) {
            throw new BizError(OrderErrorCode.STORE_ID_INVALID, "门店ID非法");
        }
        if (!store.isOpen()) {
            throw new BizError(OrderErrorCode.STORE_UNAVAILABLE, "门店不可用，无法下单: " + store.getId());
        }
    }

    public void assertCustomerAvailable(Customer customer) {
        if (customer == null) {
            throw new BizError(OrderErrorCode.CUSTOMER_ID_INVALID, "顾客ID非法");
        }
        if (!customer.isActive()) {
            throw new BizError(OrderErrorCode.CUSTOMER_UNAVAILABLE, "顾客不可用，无法下单: " + customer.getId());
        }
    }

    private boolean isPlatformSupported(int platformCode) {
        return ALIPAY_PLATFORM_CODE == platformCode;
    }

    private boolean isProductAvailable(Product product, CatalogSnapshot snapshot) {
        return product != null && product.isAvailableInStore(snapshot.productStoreStatus(product.getId()));
    }

    private List<UnavailableCustomization> collectUnavailableOptions(
            SkuSelection selection, Product product, CatalogSnapshot snapshot) {
        List<UnavailableCustomization> badOptions = new ArrayList<>();
        List<Long> optionIds = selection.getOptionIds();
        List<Long> customizationIds = selection.getCustomizationIds();

        for (int i = 0; i < optionIds.size(); i++) {
            Long optionId = optionIds.get(i);
            Long customizationId = customizationIds.get(i);
            product.assertCustomizationBinding(customizationId, optionId);

            CustomizationOption option = snapshot.option(optionId);
            Customization customization = snapshot.customization(customizationId);
            boolean customizationUnavailable = customization == null || !customization.isGloballyAvailable();
            boolean optionUnavailable = option == null
                    || !product.isOptionAvailableInStore(option, snapshot.optionStoreStatus(optionId));

            if (customizationUnavailable || optionUnavailable) {
                badOptions.add(UnavailableCustomization.builder()
                        .optionId(optionId)
                        .optionName(option != null ? option.getName() : null)
                        .productId(product.getId())
                        .productName(product.getName())
                        .itemId(customizationId)
                        .itemName(customization != null ? customization.getName() : null)
                        .build());
            }
        }
        return badOptions;
    }

    private PricedOrderItem priceItem(SkuSelection selection, Product product, CatalogSnapshot snapshot) {
        int quantity = selection.getQuantity();
        BigDecimal unitPrice = nullToZero(product.getPrice());
        for (Long optionId : selection.getOptionIds()) {
            CustomizationOption option = snapshot.option(optionId);
            unitPrice = unitPrice.add(option == null ? BigDecimal.ZERO : nullToZero(option.getPrice()));
        }
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        String customizationText = selection.getOptionIds().stream()
                .map(snapshot::option)
                .filter(Objects::nonNull)
                .map(CustomizationOption::getName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.joining(" / "));

        ProductCover cover = snapshot.cover(product.getId());
        return PricedOrderItem.builder()
                .skuId(selection.getSkuId())
                .quantity(quantity)
                .productId(product.getId())
                .productName(product.getName())
                .coverId(cover != null ? cover.coverId() : null)
                .coverUrl(cover != null ? cover.coverUrl() : null)
                .customizationText(customizationText.isEmpty() ? null : customizationText)
                .unitPrice(unitPrice.setScale(2, RoundingMode.HALF_UP))
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
