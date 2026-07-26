package cn.dextea.trade.infrastructure.acl.customer;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomerMapper {

    @Select("SELECT id, name, email, phone, alipay_open_id AS alipayOpenId, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM customers WHERE id = #{id}")
    CustomerPO selectById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT id, name, email, phone, alipay_open_id AS alipayOpenId, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM customers WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomerPO> selectByIds(@Param("ids") List<Long> ids);
}
