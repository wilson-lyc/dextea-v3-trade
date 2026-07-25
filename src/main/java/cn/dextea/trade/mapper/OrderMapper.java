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
    @Insert("INSERT INTO orders (order_no, trade_no, idempotency_key, customer_id, store_id, trade_status, making_status, version, pay_method, dining_method, note, total_price, total_quantity) " +
            "VALUES (#{orderNo}, #{tradeNo}, #{idempotencyKey}, #{customerId}, #{storeId}, #{tradeStatus}, #{makingStatus}, 0, #{payMethod}, #{diningMethod}, #{note}, #{totalPrice}, #{totalQuantity})")
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
    @Update("UPDATE orders SET trade_status = #{targetStatus}, trade_no = #{tradeNo}, paid_at = NOW(), updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND trade_status = #{expectedStatus}")
    int markPaid(@Param("orderNo") String orderNo,
                 @Param("tradeNo") String tradeNo,
                 @Param("targetStatus") int targetStatus,
                 @Param("expectedStatus") int expectedStatus);

    /**
     * 按订单号更新订单状态（乐观更新：仅当订单处于指定期望状态时生效）。
     *
     * @param orderNo 订单号
     * @param status 目标状态
     * @param expectedStatus 期望的当前状态
     * @return 影响行数（0 表示条件未命中）
     */
    @Update("UPDATE orders SET trade_status = #{targetStatus}, updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND trade_status = #{expectedStatus}")
    int updateTradeStatusByOrderNo(@Param("orderNo") String orderNo,
                                   @Param("targetStatus") int targetStatus,
                                   @Param("expectedStatus") int expectedStatus);

    /**
     * CAS 状态变更（带版本号）：仅当订单号、当前状态、版本号三者同时匹配时才更新。
     * <p>这是状态不可逆流转的最终原子保障。{@code version} 条件防止 ABA 问题：
     * 即使两个并发请求读到相同状态，只有一个能命中 {@code version}，另一个返回 0。</p>
     *
     * <p>成功时 {@code trade_status} 更新为目标状态，{@code version + 1}，
     * 并按需回填 {@code trade_no}、{@code paid_at}、{@code refunded_at}。</p>
     *
     * @param orderNo        订单号
     * @param targetStatus   目标状态码
     * @param expectedStatus 期望的当前状态码
     * @param currentVersion 当前版本号
     * @param tradeNo        支付平台交易号（支付事件回填，其他事件传 null 不更新）
     * @param paidAt         支付时间（支付事件回填，其他事件传 null 不更新）
     * @param refundedAt     退款时间（退款事件回填，其他事件传 null 不更新）
     * @return 影响行数（0 表示 CAS 失败：状态已被并发变更或版本不匹配）
     */
    @Update("<script>" +
            "UPDATE orders SET trade_status = #{targetStatus}, version = version + 1, updated_at = NOW()" +
            "<if test='tradeNo != null'>, trade_no = #{tradeNo}</if>" +
            "<if test='paidAt != null'>, paid_at = #{paidAt}</if>" +
            "<if test='refundedAt != null'>, refunded_at = #{refundedAt}</if>" +
            " WHERE order_no = #{orderNo} AND trade_status = #{expectedStatus} AND version = #{currentVersion}" +
            "</script>")
    int updateStatusCas(@Param("orderNo") String orderNo,
                        @Param("targetStatus") int targetStatus,
                        @Param("expectedStatus") int expectedStatus,
                        @Param("currentVersion") int currentVersion,
                        @Param("tradeNo") String tradeNo,
                        @Param("paidAt") java.time.LocalDateTime paidAt,
                        @Param("refundedAt") java.time.LocalDateTime refundedAt);

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
