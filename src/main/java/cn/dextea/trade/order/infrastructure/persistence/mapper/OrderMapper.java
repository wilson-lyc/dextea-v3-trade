package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.OrderPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

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
}
