package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders (order_no, trade_no, customer_id, store_id, status, pay_method, price) " +
            "VALUES (#{orderNo}, #{tradeNo}, #{customerId}, #{storeId}, #{status}, #{payMethod}, #{price})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);
}
