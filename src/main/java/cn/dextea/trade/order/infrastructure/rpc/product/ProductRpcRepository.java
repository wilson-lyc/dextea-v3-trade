package cn.dextea.trade.order.infrastructure.rpc.product;

import cn.dextea.trade.order.domain.enumeration.CustomizationItemStatus;
import cn.dextea.trade.order.domain.enumeration.CustomizationOptionGlobalStatus;
import cn.dextea.trade.order.domain.enumeration.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.domain.enumeration.ProductGlobalStatus;
import cn.dextea.trade.order.domain.enumeration.ProductStoreStatus;
import cn.dextea.trade.order.domain.model.CustomizationItem;
import cn.dextea.trade.order.domain.model.CustomizationOption;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.model.ProductCover;
import cn.dextea.trade.order.domain.repository.ProductRepository;
import cn.dextea.trade.shared.error.DownstreamErrorCode;
import cn.dextea.trade.shared.error.SystemException;
import cn.dextea.trade.shared.model.Money;
import dextea.product.v1.ProductOuterClass;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品领域的 RPC 适配器。
 *
 * <p>order 不再直接读取商品、客制化、门店商品状态、门店选项状态和图片表，
 * 商品聚合统一由 product 的 GetProductDetail RPC 返回。</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRpcRepository implements ProductRepository {

    private final ProductRpcClient productRpcClient;

    @Override
    public Map<Long, Product> getProductByIdsWithStoreId(Set<Long> ids, Long storeId) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Product> products = new HashMap<>();
        for (Long productId : ids) {
            if (productId == null || productId <= 0) {
                continue;
            }

            try {
                ProductOuterClass.ProductDetail detail = productRpcClient
                        .getProductDetail(productId, storeId);
                products.put(productId, toDomain(detail));
            } catch (StatusRuntimeException ex) {
                if (ex.getStatus().getCode() == Status.Code.NOT_FOUND) {
                    log.info("商品服务未找到商品，按不可售商品处理, productId={}, storeId={}", productId, storeId);
                    continue;
                }
                throw new SystemException(DownstreamErrorCode.DOWNSTREAM_UNAVAILABLE,
                        "商品服务调用失败: " + ex.getStatus().getCode(), ex);
            }
        }
        return products;
    }

    private Product toDomain(ProductOuterClass.ProductDetail detail) {
        ProductOuterClass.Product product = detail.getProduct();
        return Product.builder()
                .id(product.getId())
                .name(product.getName())
                .globalStatus(ProductGlobalStatus.of(product.getStatus()))
                .storeStatus(ProductStoreStatus.of(detail.getStoreStatus()))
                .price(Money.of(BigDecimal.valueOf(product.getPrice())))
                .cover(toCover(detail.getImages()))
                .customization(detail.getCustomizationItemsList().stream()
                        .map(this::toCustomizationItem)
                        .collect(Collectors.toList()))
                .build();
    }

    private ProductCover toCover(ProductOuterClass.ProductImagesResponse images) {
        if (!images.hasCover()) {
            return null;
        }
        ProductOuterClass.ProductImage cover = images.getCover();
        return ProductCover.builder()
                .id(cover.getId())
                .url(cover.getUrl())
                .build();
    }

    private CustomizationItem toCustomizationItem(ProductOuterClass.ProductDetailItem source) {
        ProductOuterClass.CustomizationItem item = source.getItem();
        List<CustomizationOption> options = source.getOptionsList().stream()
                .map(this::toCustomizationOption)
                .collect(Collectors.toList());
        return CustomizationItem.builder()
                .id(item.getId())
                .name(item.getName())
                .status(CustomizationItemStatus.of(item.getStatus()))
                .options(options)
                .build();
    }

    private CustomizationOption toCustomizationOption(ProductOuterClass.ProductDetailOption source) {
        ProductOuterClass.CustomizationOption option = source.getOption();
        return CustomizationOption.builder()
                .id(option.getId())
                .name(option.getName())
                .price(Money.of(BigDecimal.valueOf(option.getPrice())))
                .globalStatus(CustomizationOptionGlobalStatus.of(option.getStatus()))
                .storeStatus(CustomizationOptionStoreStatus.of(source.getStoreStatus()))
                .build();
    }
}
