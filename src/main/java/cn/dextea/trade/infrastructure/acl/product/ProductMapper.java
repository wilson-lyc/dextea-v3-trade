package cn.dextea.trade.infrastructure.acl.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("SELECT id, name, brief, description, status, price, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM products WHERE id = #{id}")
    ProductPO selectById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT id, name, brief, description, status, price, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM products " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<ProductPO> selectByIds(@Param("ids") List<Long> ids);

}
