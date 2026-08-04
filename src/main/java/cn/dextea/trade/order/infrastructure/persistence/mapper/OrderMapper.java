package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders (order_no, trade_no, idempotency_key, customer_id, store_id, "
            + "total_price, total_quantity, dining_method, note, source, pickup_code, making_status, "
            + "payment_method, payment_status, payment_expired_at, payment_paid_at, payment_refunded_at, version) "
            + "VALUES (#{orderNo}, #{tradeNo}, #{idempotencyKey}, #{customerId}, #{storeId}, "
            + "#{totalPrice}, #{totalQuantity}, #{diningMethod}, #{note}, #{source}, #{pickupCode}, #{makingStatus}, "
            + "#{paymentMethod}, #{paymentStatus}, #{paymentExpiredAt}, #{paymentPaidAt}, #{paymentRefundedAt}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderPO orderPO);

    @Select("SELECT * FROM orders WHERE customer_id = #{customerId} "
            + "AND YEAR(created_at) = #{year} AND MONTH(created_at) = #{month} "
            + "ORDER BY created_at DESC")
    List<OrderPO> selectByCustomerAndMonth(@Param("customerId") Long customerId,
                                           @Param("year") int year,
                                           @Param("month") int month);

    @Select("<script>SELECT * FROM orders WHERE id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<OrderPO> selectByIds(@Param("ids") Collection<Long> ids);

    @Select("SELECT * FROM orders WHERE id = #{orderId}")
    OrderPO selectById(@Param("orderId") Long orderId);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    OrderPO selectByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE orders SET payment_status = #{paymentStatus}, payment_paid_at = #{paymentPaidAt}, "
            + "updated_at = NOW(), version = version + 1 "
            + "WHERE id = #{id} AND version = #{version}")
    int updatePaymentStatus(OrderPO orderPO);
}
