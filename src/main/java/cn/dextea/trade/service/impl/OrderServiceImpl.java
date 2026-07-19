package cn.dextea.trade.service.impl;

import cn.dextea.trade.dto.CartItemDTO;
import cn.dextea.trade.dto.OrderCalculateRequest;
import cn.dextea.trade.dto.OrderCalculateResult;
import cn.dextea.trade.entity.Product;
import cn.dextea.trade.common.BizError;
import cn.dextea.trade.entity.enums.ProductGlobalStatus;
import cn.dextea.trade.error.OrderErrorCode;
import cn.dextea.trade.mapper.ProductMapper;
import cn.dextea.trade.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductMapper productMapper;

    @Override
    public OrderCalculateResult calculate(OrderCalculateRequest request) {
        // 校验购物车商品是否均可售（下架校验）
        validateProductsOnShelf(request.getCartItems());

        // TODO: 计算逻辑（商品明细金额、优惠、运费、应付总额等）

        return new OrderCalculateResult();
    }

    private void validateProductsOnShelf(List<CartItemDTO> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return;
        }

        // 提取商品ID并去重
        Set<Long> productIds = cartItems.stream()
                .map(CartItemDTO::getId)
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .collect(Collectors.toSet());

        if (productIds.isEmpty()) {
            return;
        }

        // 获取商品全局状态
        List<Product> products = productMapper.selectByIds(new ArrayList<>(productIds));
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        // 收集不存在的商品ID，以及不可售（已下架）的商品名称
        List<Long> notFoundIds = new ArrayList<>(productIds);
        notFoundIds.removeAll(productMap.keySet());

        List<String> unavailableNames = new ArrayList<>();
        for (Product product : products) {
            if (!isOnShelf(product)) {
                unavailableNames.add(product.getName());
            }
        }

        // 优先校验商品是否存在，缺失则直接抛出
        if (!notFoundIds.isEmpty()) {
            String message = buildNotFoundMessage(notFoundIds);
            throw new BizError(OrderErrorCode.PRODUCT_NOT_FOUND, message);
        }

        if (!unavailableNames.isEmpty()) {
            String message = buildUnavailableMessage(unavailableNames);
            throw new BizError(OrderErrorCode.PRODUCT_UNAVAILABLE, message);
        }
    }

    /**
     * 构建商品不存在提示文案，如 "商品 1003 不存在"。
     */
    private String buildNotFoundMessage(List<Long> ids) {
        String joined = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("、"));
        return "商品 " + joined + " 不存在";
    }

    private boolean isOnShelf(Product product) {
        return product.getStatus() != null
                && product.getStatus() == ProductGlobalStatus.ON_SHELF.getCode();
    }

    /**
     * 构建不可售提示文案，如 "B和C已下架，不可购买"。
     */
    private String buildUnavailableMessage(List<String> names) {
        String joined;
        if (names.size() == 1) {
            joined = names.get(0);
        } else if (names.size() == 2) {
            joined = names.get(0) + "和" + names.get(1);
        } else {
            joined = String.join("、", names.subList(0, names.size() - 1))
                    + "和" + names.get(names.size() - 1);
        }
        return joined + "已下架，不可购买";
    }
}
