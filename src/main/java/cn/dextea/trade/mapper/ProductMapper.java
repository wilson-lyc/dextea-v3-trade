package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("<script>" +
            "SELECT id, name, status, price " +
            "FROM products " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<Product> selectByIds(@Param("ids") List<Long> ids);
}
