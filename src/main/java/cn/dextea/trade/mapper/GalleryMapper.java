package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.Gallery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GalleryMapper {

    /**
     * 按主键查询图片
     *
     * @param id 图片ID
     * @return 图片（无则 null）
     */
    @Select("SELECT id, url, object_key AS objectKey, created_at AS createdAt, name " +
            "FROM gallery " +
            "WHERE id = #{id}")
    Gallery selectById(@Param("id") Long id);

    /**
     * 按主键批量查询图片
     *
     * @param ids 图片ID列表
     * @return 图片列表（无则空列表）
     */
    @Select("<script>" +
            "SELECT id, url, object_key AS objectKey, created_at AS createdAt, name " +
            "FROM gallery " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<Gallery> selectByIds(@Param("ids") List<Long> ids);
}
