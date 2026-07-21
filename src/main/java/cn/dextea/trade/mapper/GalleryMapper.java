package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.Gallery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GalleryMapper {

    @Select("SELECT id, url, object_key AS objectKey, created_at AS createdAt, name " +
            "FROM gallery " +
            "WHERE id = #{id}")
    Gallery selectById(@Param("id") Long id);
}
