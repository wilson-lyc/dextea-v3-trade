package cn.dextea.trade.order.infrastructure.rpc.store;

import cn.dextea.trade.order.domain.enumeration.StoreStatus;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.shared.error.DownstreamErrorCode;
import cn.dextea.trade.shared.error.SystemException;
import dextea.store.v1.StoreOuterClass;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Store domain adapter: Trade no longer reads the stores table directly. */
@Slf4j
@Repository
@RequiredArgsConstructor
public class StoreRpcRepository implements StoreRepository {

    private final StoreRpcClient storeRpcClient;

    @Override
    public Store getStoreById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        Map<Long, Store> stores = getStoresByIds(Collections.singleton(id));
        return stores.get(id);
    }

    @Override
    public Map<Long, Store> getStoresByIds(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            StoreOuterClass.GetBusinessStoresResponse response = storeRpcClient.getStores(storeIds);
            Map<Long, Store> stores = new HashMap<>();
            for (StoreOuterClass.BusinessStore source : response.getStoresList()) {
                stores.put(source.getId(), toDomain(source));
            }
            return stores;
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return Collections.emptyMap();
            }
            throw new SystemException(DownstreamErrorCode.DOWNSTREAM_UNAVAILABLE,
                    "门店服务调用失败: " + ex.getStatus().getCode(), ex);
        }
    }

    private Store toDomain(StoreOuterClass.BusinessStore source) {
        return Store.builder()
                .id(source.getId())
                .name(source.getName())
                .status(StoreStatus.of(source.getStatus()))
                .build();
    }
}
