package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.GalleryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GalleryMapper {

    @Select("SELECT * FROM gallery WHERE id = #{id}")
    GalleryPO selectById(@Param("id") Long id);
}
