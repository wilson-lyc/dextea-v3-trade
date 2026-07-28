package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.OrderStatusLogPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderStatusLogMapper {

    @Insert("INSERT INTO order_status_log (order_no, from_status, to_status, event, operator, version) " +
            "VALUES (#{orderId}, #{fromStatus}, #{toStatus}, #{event}, #{operator}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderStatusLogPO log);

    @Select("SELECT COUNT(*) FROM order_status_log WHERE order_no = #{orderNo}")
    int countByOrderNo(@Param("orderNo") String orderNo);
}
