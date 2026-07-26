package cn.dextea.trade.catalog.infrastructure.persistence;

import cn.dextea.trade.catalog.domain.model.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CustomerMapper {

    /**
     * 按主键查询顾客
     *
     * @param id 顾客ID
     * @return 顾客（无则 null）
     */
    @Select("SELECT id, name, status, alipay_open_id, weixin_open_id FROM customers WHERE id = #{id}")
    Customer selectById(@Param("id") Long id);
}
