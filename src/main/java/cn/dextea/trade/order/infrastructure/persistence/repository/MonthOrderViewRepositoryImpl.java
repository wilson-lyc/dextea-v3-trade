package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.MonthOrderView;
import cn.dextea.trade.order.domain.repository.MonthOrderViewRepository;
import cn.dextea.trade.order.infrastructure.persistence.mapper.GalleryMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderItemMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.OrderMapper;
import cn.dextea.trade.order.infrastructure.persistence.mapper.StoreMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.GalleryPO;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderItemPO;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import cn.dextea.trade.order.infrastructure.persistence.po.StorePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MonthOrderViewRepositoryImpl implements MonthOrderViewRepository {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StoreMapper storeMapper;
    private final GalleryMapper galleryMapper;

    @Override
    public Map<Long, MonthOrderView> findViews(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, OrderPO> orderById = orderMapper.selectByIds(orderIds).stream()
                .collect(Collectors.toMap(OrderPO::getId, Function.identity(), (a, b) -> a));

        Map<Long, String> storeNameByOrderId = orderById.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> findStoreName(entry.getValue().getStoreId())));

        Map<Long, List<Long>> coverIdsByOrderId = orderIds.stream()
                .collect(Collectors.toMap(Function.identity(), this::findCoverIds));

        Map<Long, String> urlByCoverId = findCoverUrls(coverIdsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet()));

        Map<Long, List<String>> coversByOrderId = coverIdsByOrderId.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(urlByCoverId::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())));

        return orderIds.stream()
                .filter(orderById::containsKey)
                .collect(Collectors.toMap(Function.identity(), orderId -> new MonthOrderView(
                        storeNameByOrderId.get(orderId),
                        coversByOrderId.getOrDefault(orderId, Collections.emptyList())), (a, b) -> a));
    }

    private String findStoreName(Long storeId) {
        StorePO storePO = storeMapper.selectById(storeId);
        return storePO == null || storePO.getName() == null ? "未知门店" : storePO.getName();
    }

    private List<Long> findCoverIds(Long orderId) {
        List<OrderItemPO> items = orderItemMapper.selectByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(OrderItemPO::getCoverId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<Long, String> findCoverUrls(Set<Long> coverIds) {
        if (coverIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<GalleryPO> galleryPOs = galleryMapper.selectByIds(coverIds);
        if (galleryPOs == null || galleryPOs.isEmpty()) {
            return Collections.emptyMap();
        }
        return galleryPOs.stream()
                .filter(po -> po.getUrl() != null && !po.getUrl().isEmpty())
                .collect(Collectors.toMap(GalleryPO::getId, GalleryPO::getUrl, (a, b) -> a));
    }
}
