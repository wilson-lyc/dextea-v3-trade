package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.CustomerPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CustomerMapper {

    @Select("SELECT id, name, email, password, weixin_open_id, alipay_open_id, status, created_at, updated_at "
            + "FROM customer WHERE id = #{id}")
    CustomerPO selectById(@Param("id") Long id);
}
