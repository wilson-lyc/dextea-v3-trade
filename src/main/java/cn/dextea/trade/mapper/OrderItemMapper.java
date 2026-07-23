package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    /**
     * 批量插入订单明细
     *
     * @param items 订单明细列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO order_items (order_id, product_id, sku_id, product_name, cover_id, quantity, unit_price, subtotal) VALUES " +
            "<foreach collection='items' item='item' separator=','>" +
            "(#{item.orderId}, #{item.productId}, #{item.skuId}, #{item.productName}, #{item.coverId}, #{item.quantity}, #{item.unitPrice}, #{item.subtotal})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("items") List<OrderItem> items);

    /**
     * 按订单ID批量查询明细（仅取订单ID与封面图ID）
     *
     * @param orderIds 订单ID列表
     * @return 订单明细列表（无则空列表）
     */
    @Select("<script>" +
            "SELECT order_id, cover_id FROM order_items WHERE order_id IN " +
            "<foreach collection='orderIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<OrderItem> selectByOrderIds(@Param("orderIds") List<Long> orderIds);
}
