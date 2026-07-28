package cn.dextea.trade.order.infrastructure.persistence.mapper;
import cn.dextea.trade.order.infrastructure.persistence.po.OrderItemPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
@Mapper
public interface OrderItemMapper {
    @Insert("<script>" +
            "INSERT INTO order_items (order_id, product_id, sku_id, product_name, cover_id, customization_text, quantity, unit_price, subtotal) VALUES " +
            "<foreach collection='items' item='item' separator=','>" +
            "(#{item.orderId}, #{item.productId}, #{item.skuId}, #{item.productName}, #{item.coverId}, #{item.customizationText}, #{item.quantity}, #{item.unitPrice}, #{item.subtotal})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("items") List<OrderItemPO> items);
    @Select("<script>" +
            "SELECT order_id, cover_id FROM order_items WHERE order_id IN " +
            "<foreach collection='orderIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<OrderItemPO> selectByOrderIds(@Param("orderIds") List<Long> orderIds);
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId} ORDER BY id ASC")
    List<OrderItemPO> selectFullByOrderId(@Param("orderId") Long orderId);
}
