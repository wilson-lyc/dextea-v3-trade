package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.OrderPaymentStatusLogPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderPaymentStatusLogMapper {

    @Insert("INSERT INTO order_payment_status_log (order_id, from_status, to_status, event, version, created_at) "
            + "VALUES (#{orderId}, #{fromStatus}, #{toStatus}, #{event}, #{version}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderPaymentStatusLogPO po);

    @Insert("<script>INSERT INTO order_payment_status_log (order_id, from_status, to_status, event, version, created_at) "
            + "VALUES "
            + "<foreach collection='logs' item='log' separator=','>"
            + "(#{log.orderId}, #{log.fromStatus}, #{log.toStatus}, #{log.event}, #{log.version}, #{log.createdAt})"
            + "</foreach></script>")
    int batchInsert(@Param("logs") List<OrderPaymentStatusLogPO> logs);

    @Select("SELECT * FROM order_payment_status_log WHERE order_id = #{orderId} ORDER BY created_at ASC")
    List<OrderPaymentStatusLogPO> selectByOrderId(@Param("orderId") String orderId);
}
