package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.infrastructure.persistence.mapper.PickupCodeCounterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PickupCodeGenerator {

    private static final String PREFIX = "8";
    private static final int MODULO = 1000;

    private final PickupCodeCounterMapper pickupCodeCounterMapper;

    public String generate(Long storeId, LocalDate date) {
        pickupCodeCounterMapper.incrementAndGet(storeId, date);
        Integer dailyCount = pickupCodeCounterMapper.selectDailyCount(storeId, date);
        if (dailyCount == null) {
            dailyCount = 1;
        }
        int suffix = dailyCount % MODULO;
        return PREFIX + String.format("%03d", suffix);
    }
}
