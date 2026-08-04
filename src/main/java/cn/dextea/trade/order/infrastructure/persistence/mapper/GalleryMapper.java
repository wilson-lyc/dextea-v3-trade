package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.GalleryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface GalleryMapper {

    @Select("SELECT * FROM gallery WHERE id = #{id}")
    GalleryPO selectById(@Param("id") Long id);

    @Select("<script>SELECT * FROM gallery WHERE id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<GalleryPO> selectByIds(@Param("ids") Collection<Long> ids);
}
