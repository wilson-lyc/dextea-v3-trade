package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.CustomerPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CustomerMapper {

    @Select("SELECT * FROM customers WHERE id = #{id}")
    CustomerPO getCustomerById(@Param("id") Long id);
}
