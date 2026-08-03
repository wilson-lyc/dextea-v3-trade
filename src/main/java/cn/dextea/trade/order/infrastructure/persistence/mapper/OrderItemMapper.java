package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.OrderItemPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Insert("<script>INSERT INTO order_items (order_id, product_id, product_name, sku_id, customization, "
            + "cover_id, quantity, unit_price, subtotal) VALUES "
            + "<foreach collection='items' item='item' separator=','>"
            + "(#{item.orderId}, #{item.productId}, #{item.productName}, #{item.skuId}, #{item.customization}, "
            + "#{item.coverId}, #{item.quantity}, #{item.unitPrice}, #{item.subtotal})"
            + "</foreach></script>")
    int batchInsert(@Param("items") List<OrderItemPO> items);
}
