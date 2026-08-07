package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.PickupCodeCounterPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface PickupCodeCounterMapper {

    @Insert("INSERT INTO pickup_code_counter (store_id, date, daily_count) "
            + "VALUES (#{storeId}, #{date}, 1) "
            + "ON DUPLICATE KEY UPDATE daily_count = daily_count + 1")
    int incrementAndGet(@Param("storeId") Long storeId, @Param("date") LocalDate date);

    @Select("SELECT daily_count FROM pickup_code_counter WHERE store_id = #{storeId} AND date = #{date}")
    Integer selectDailyCount(@Param("storeId") Long storeId, @Param("date") LocalDate date);
}
