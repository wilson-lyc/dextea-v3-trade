package cn.dextea.trade.order.infrastructure.rpc.product;

import dextea.product.v1.ProductOuterClass;
import dextea.product.v1.ProductServiceGrpc;
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

    private final ProductServiceGrpc.ProductServiceBlockingStub productService;
    private final ProductRpcProperties properties;

    public ProductOuterClass.ListProductsResponse listProducts(
            int page, int pageSize, Integer status, String name) {
        ProductOuterClass.ListProductsRequest.Builder request =
                ProductOuterClass.ListProductsRequest.newBuilder()
                        .setPage(page)
                        .setPageSize(pageSize)
                        .setName(name == null ? "" : name);
        if (status != null) {
            request.setStatus(status);
        }
        return stub().listProducts(request.build());
    }

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

    public ProductOuterClass.ProductImagesResponse getProductImages(long productId) {
        return stub().getProductImages(ProductOuterClass.GetProductImagesRequest.newBuilder()
                .setProductId(productId)
                .build());
    }

    public ProductOuterClass.ListCustomizationItemsResponse listCustomizationItems(
            long productId, int page, int pageSize, Integer status, String name) {
        ProductOuterClass.ListCustomizationItemsRequest.Builder request =
                ProductOuterClass.ListCustomizationItemsRequest.newBuilder()
                .setProductId(productId)
                .setPage(page)
                .setPageSize(pageSize)
                .setName(name == null ? "" : name);
        if (status != null) {
            request.setStatus(status);
        }
        return stub().listCustomizationItems(request.build());
    }

    public ProductOuterClass.ListCustomizationOptionsResponse listCustomizationOptions(
            long itemId, int page, int pageSize, Integer status, String name) {
        ProductOuterClass.ListCustomizationOptionsRequest.Builder request =
                ProductOuterClass.ListCustomizationOptionsRequest.newBuilder()
                .setItemId(itemId)
                .setPage(page)
                .setPageSize(pageSize)
                .setName(name == null ? "" : name);
        if (status != null) {
            request.setStatus(status);
        }
        return stub().listCustomizationOptions(request.build());
    }

    public ProductOuterClass.Menu getMenu(long menuId) {
        return stub().getMenu(ProductOuterClass.GetMenuRequest.newBuilder()
                .setId(menuId)
                .build());
    }

    public ProductOuterClass.ListMenusResponse listMenus(int page, int pageSize, String name) {
        return stub().listMenus(ProductOuterClass.ListMenusRequest.newBuilder()
                .setPage(page)
                .setPageSize(pageSize)
                .setName(name == null ? "" : name)
                .build());
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

    public ProductOuterClass.ListMenuGroupsResponse listMenuGroups(
            long menuId, int page, int pageSize) {
        return stub().listMenuGroups(ProductOuterClass.ListMenuGroupsRequest.newBuilder()
                .setMenuId(menuId)
                .setPage(page)
                .setPageSize(pageSize)
                .build());
    }

    public ProductOuterClass.ListMenuProductsResponse listMenuProducts(
            long groupId, int page, int pageSize) {
        return stub().listMenuProducts(ProductOuterClass.ListMenuProductsRequest.newBuilder()
                .setGroupId(groupId)
                .setPage(page)
                .setPageSize(pageSize)
                .build());
    }

    private ProductServiceGrpc.ProductServiceBlockingStub stub() {
        return productService.withDeadlineAfter(properties.getDeadlineMillis(), TimeUnit.MILLISECONDS);
    }
}
