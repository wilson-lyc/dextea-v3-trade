package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders (order_no, trade_no, idempotency_key, customer_id, store_id, status, pay_method, total_price, total_quantity) " +
            "VALUES (#{orderNo}, #{tradeNo}, #{idempotencyKey}, #{customerId}, #{storeId}, #{status}, #{payMethod}, #{totalPrice}, #{totalQuantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT * FROM orders WHERE idempotency_key = #{idempotencyKey}")
    Order selectByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    @Update("UPDATE orders SET trade_no = #{tradeNo}, updated_at = NOW() WHERE id = #{id}")
    int updateTradeNo(@Param("id") Long id, @Param("tradeNo") String tradeNo);
}
