package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders (order_no, trade_no, idempotency_key, customer_id, store_id, trade_status, making_status, version, pay_method, dining_method, note, total_price, total_quantity, pay_expire_at) " +
            "VALUES (#{orderNo}, #{tradeNo}, #{idempotencyKey}, #{customerId}, #{storeId}, #{tradeStatus}, #{makingStatus}, 0, #{payMethod}, #{diningMethod}, #{note}, #{totalPrice}, #{totalQuantity}, #{payExpireAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderPO order);

    @Select("SELECT * FROM orders WHERE idempotency_key = #{idempotencyKey}")
    OrderPO selectByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    @Select("SELECT * FROM orders WHERE id = #{id}")
    OrderPO selectById(@Param("id") Long id);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    OrderPO selectByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE orders SET trade_no = #{tradeNo}, updated_at = NOW() WHERE id = #{id}")
    int updateTradeNo(@Param("id") Long id, @Param("tradeNo") String tradeNo);

    @Update("UPDATE orders SET trade_status = #{targetStatus}, trade_no = #{tradeNo}, paid_at = NOW(), updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND trade_status = #{expectedStatus}")
    int markPaid(@Param("orderNo") String orderNo,
                 @Param("tradeNo") String tradeNo,
                 @Param("targetStatus") int targetStatus,
                 @Param("expectedStatus") int expectedStatus);

    @Update("UPDATE orders SET trade_status = #{targetStatus}, updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND trade_status = #{expectedStatus}")
    int updateTradeStatusByOrderNo(@Param("orderNo") String orderNo,
                                   @Param("targetStatus") int targetStatus,
                                   @Param("expectedStatus") int expectedStatus);

    @Update("<script>" +
            "UPDATE orders SET trade_status = #{targetStatus}, version = version + 1, updated_at = NOW()" +
            "<if test='tradeNo != null'>, trade_no = #{tradeNo}</if>" +
            "<if test='paidAt != null'>, paid_at = #{paidAt}</if>" +
            "<if test='refundedAt != null'>, refunded_at = #{refundedAt}</if>" +
            "<if test='pickupCode != null'>, pickup_code = #{pickupCode}</if>" +
            " WHERE order_no = #{orderNo} AND trade_status = #{expectedStatus} AND version = #{currentVersion}" +
            "</script>")
    int updateStatusCas(@Param("orderNo") String orderNo,
                        @Param("targetStatus") int targetStatus,
                        @Param("expectedStatus") int expectedStatus,
                        @Param("currentVersion") int currentVersion,
                        @Param("tradeNo") String tradeNo,
                        @Param("paidAt") LocalDateTime paidAt,
                        @Param("refundedAt") LocalDateTime refundedAt,
                        @Param("pickupCode") String pickupCode);

    @Select("SELECT * FROM orders WHERE customer_id = #{customerId} AND created_at >= #{start} AND created_at < #{end} ORDER BY created_at DESC")
    List<OrderPO> selectByCustomerIdAndCreatedBetween(@Param("customerId") Long customerId,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);
}
