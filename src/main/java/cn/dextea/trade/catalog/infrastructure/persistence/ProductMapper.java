package cn.dextea.trade.catalog.infrastructure.persistence;

import cn.dextea.trade.catalog.domain.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper {

    /**
     * 批量查询商品
     *
     * @param ids 商品ID列表
     * @return 商品列表
     */
    @Select("<script>" +
            "SELECT id, name, status, price " +
            "FROM products " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<Product> selectByIds(@Param("ids") List<Long> ids);
}
