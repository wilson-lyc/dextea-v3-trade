package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
     * 更新订单交易号
     *
     * @param id 订单ID
     * @param tradeNo 支付宝交易号
     * @return 影响行数
     */
    @Update("UPDATE orders SET trade_no = #{tradeNo}, updated_at = NOW() WHERE id = #{id}")
    int updateTradeNo(@Param("id") Long id, @Param("tradeNo") String tradeNo);
}
