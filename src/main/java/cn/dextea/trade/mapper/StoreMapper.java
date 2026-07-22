package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StoreMapper {

    /**
     * 按主键查询门店
     *
     * @param id 门店ID
     * @return 门店（无则 null）
     */
    @Select("SELECT id, name, status FROM stores WHERE id = #{id}")
    Store selectById(@Param("id") Long id);
}
