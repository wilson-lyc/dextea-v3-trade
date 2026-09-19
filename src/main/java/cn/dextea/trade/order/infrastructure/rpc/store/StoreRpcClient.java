package cn.dextea.trade.order.infrastructure.rpc.store;

import dextea.store.v1.StoreBusinessServiceGrpc;
import dextea.store.v1.StoreOuterClass;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/** Store Service 业务只读客户端，屏蔽 gRPC stub、超时和请求组装。 */
@RequiredArgsConstructor
public class StoreRpcClient {

    private final StoreBusinessServiceGrpc.StoreBusinessServiceBlockingStub storeService;
    private final StoreRpcProperties properties;

    public StoreOuterClass.GetBusinessStoresResponse getStores(Collection<Long> storeIds) {
        StoreOuterClass.GetBusinessStoresRequest.Builder request =
                StoreOuterClass.GetBusinessStoresRequest.newBuilder();
        storeIds.forEach(request::addIds);
        return stub().getStores(request.build());
    }

    private StoreBusinessServiceGrpc.StoreBusinessServiceBlockingStub stub() {
        return storeService.withDeadlineAfter(properties.getDeadlineMillis(), TimeUnit.MILLISECONDS);
    }
}
