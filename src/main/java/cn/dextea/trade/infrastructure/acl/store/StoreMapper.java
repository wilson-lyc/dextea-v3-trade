package cn.dextea.trade.infrastructure.acl.store;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreMapper {

    @Select("SELECT id, name, province, city, district, address, status, " +
            "business_hours AS businessHours, phone, longitude, latitude, email, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM stores WHERE id = #{id}")
    StorePO selectById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT id, name, province, city, district, address, status, " +
            "business_hours AS businessHours, phone, longitude, latitude, email, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM stores WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<StorePO> selectByIds(@Param("ids") List<Long> ids);
}
