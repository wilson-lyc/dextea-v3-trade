package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CustomerMapper {

    /**
     * 按主键查询顾客，无记录返回 null，由调用方判定顾客ID合法性。
     */
    @Select("SELECT id, name, status FROM customers WHERE id = #{id}")
    Customer selectById(@Param("id") Long id);
}
