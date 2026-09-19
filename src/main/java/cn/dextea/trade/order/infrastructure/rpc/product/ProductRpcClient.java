package cn.dextea.trade.order.infrastructure.rpc.product;

import dextea.product.v1.ProductOuterClass;
import dextea.product.v1.ProductBusinessServiceGrpc;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * 商品领域只读 RPC 客户端。
 *
 * <p>商品、客制化、图片和菜单相关查询统一从这里访问 product，调用方不需要
 * 感知 gRPC stub、超时和连接配置。</p>
 */
@RequiredArgsConstructor
public class ProductRpcClient {

    private final ProductBusinessServiceGrpc.ProductBusinessServiceBlockingStub productService;
    private final ProductRpcProperties properties;

    public ProductOuterClass.ProductDetail getProductDetail(long productId, long storeId) {
        return stub().getProductDetail(ProductOuterClass.GetProductDetailRequest.newBuilder()
                .setProductId(productId)
                .setStoreId(storeId)
                .build());
    }

    public ProductOuterClass.ProductStoreStatusesResponse getProductStoreStatuses(
            long storeId, Iterable<Long> productIds) {
        ProductOuterClass.GetProductStoreStatusesRequest.Builder request =
                ProductOuterClass.GetProductStoreStatusesRequest.newBuilder()
                        .setStoreId(storeId);
        productIds.forEach(request::addProductIds);
        return stub().getProductStoreStatuses(request.build());
    }

    public ProductOuterClass.CustomizationOptionStoreStatusesResponse
    getCustomizationOptionStoreStatuses(long storeId, Iterable<Long> optionIds) {
        ProductOuterClass.GetCustomizationOptionStoreStatusesRequest.Builder request =
                ProductOuterClass.GetCustomizationOptionStoreStatusesRequest.newBuilder()
                        .setStoreId(storeId);
        optionIds.forEach(request::addOptionIds);
        return stub().getCustomizationOptionStoreStatuses(request.build());
    }

    public ProductOuterClass.MenuTreeResponse getMenuTree(
            long menuId, ProductOuterClass.MenuTreeMode mode, Long storeId) {
        ProductOuterClass.GetMenuTreeRequest.Builder request =
                ProductOuterClass.GetMenuTreeRequest.newBuilder()
                        .setMenuId(menuId)
                        .setMode(mode);
        if (storeId != null) {
            request.setStoreId(storeId);
        }
        return stub().getMenuTree(request.build());
    }

    private ProductBusinessServiceGrpc.ProductBusinessServiceBlockingStub stub() {
        return productService.withDeadlineAfter(properties.getDeadlineMillis(), TimeUnit.MILLISECONDS);
    }
}
