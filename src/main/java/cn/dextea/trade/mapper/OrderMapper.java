package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.Order;
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

    /**
     * 插入订单
     *
     * @param order 订单
     * @return 影响行数
     */
    @Insert("INSERT INTO orders (order_no, trade_no, idempotency_key, customer_id, store_id, status, pay_method, dining_method, note, total_price, total_quantity) " +
            "VALUES (#{orderNo}, #{tradeNo}, #{idempotencyKey}, #{customerId}, #{storeId}, #{status}, #{payMethod}, #{diningMethod}, #{note}, #{totalPrice}, #{totalQuantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    /**
     * 按幂等键查询订单
     *
     * @param idempotencyKey 幂等键
     * @return 订单（无则 null）
     */
    @Select("SELECT * FROM orders WHERE idempotency_key = #{idempotencyKey}")
    Order selectByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    /**
     * 按主键查询订单
     *
     * @param id 订单ID
     * @return 订单（无则 null）
     */
    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order selectById(@Param("id") Long id);

    /**
     * 按订单号查询订单
     *
     * @param orderNo 订单号（支付平台 out_trade_no）
     * @return 订单（无则 null）
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新订单交易号
     *
     * @param id 订单ID
     * @param tradeNo 支付宝交易号
     * @return 影响行数
     */
    @Update("UPDATE orders SET trade_no = #{tradeNo}, updated_at = NOW() WHERE id = #{id}")
    int updateTradeNo(@Param("id") Long id, @Param("tradeNo") String tradeNo);

    /**
     * 标记订单已支付（乐观更新：仅当订单处于待支付时生效，保证幂等且不覆盖终态）。
     *
     * @param orderNo 订单号
     * @param tradeNo 支付平台交易号
     * @param status 目标状态（已支付）
     * @param expectedStatus 期望的当前状态（待支付）
     * @return 影响行数（0 表示条件未命中，订单已被处理或状态已变更）
     */
    @Update("UPDATE orders SET status = #{status}, trade_no = #{tradeNo}, paid_at = NOW(), updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND status = #{expectedStatus}")
    int markPaid(@Param("orderNo") String orderNo,
                 @Param("tradeNo") String tradeNo,
                 @Param("status") int status,
                 @Param("expectedStatus") int expectedStatus);

    /**
     * 按订单号更新订单状态（乐观更新：仅当订单处于指定期望状态时生效）。
     *
     * @param orderNo 订单号
     * @param status 目标状态
     * @param expectedStatus 期望的当前状态
     * @return 影响行数（0 表示条件未命中）
     */
    @Update("UPDATE orders SET status = #{status}, updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND status = #{expectedStatus}")
    int updateStatusByOrderNo(@Param("orderNo") String orderNo,
                              @Param("status") int status,
                              @Param("expectedStatus") int expectedStatus);

    /**
     * 按用户ID查询指定时间之后创建的订单（按下单时间倒序）
     *
     * @param customerId 用户ID
     * @param since 起始时间（含）
     * @return 订单列表（无则空列表）
     */
    @Select("SELECT * FROM orders WHERE customer_id = #{customerId} AND created_at >= #{since} ORDER BY created_at DESC")
    List<Order> selectByCustomerIdAndCreatedAtAfter(@Param("customerId") Long customerId, @Param("since") LocalDateTime since);
}
