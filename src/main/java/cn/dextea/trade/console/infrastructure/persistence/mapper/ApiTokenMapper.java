package cn.dextea.trade.console.infrastructure.persistence.mapper;

import cn.dextea.trade.console.infrastructure.persistence.po.ApiTokenPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ApiTokenMapper {

    @Select("SELECT * FROM api_tokens WHERE token = #{token}")
    ApiTokenPO selectByToken(@Param("token") String token);

    @Select("SELECT * FROM api_tokens WHERE id = #{id}")
    ApiTokenPO selectById(@Param("id") Long id);

    @Insert("INSERT INTO api_tokens(token, name, enabled, expire_at, created_at, updated_at) "
            + "VALUES(#{token}, #{name}, #{enabled}, #{expireAt}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ApiTokenPO po);

    @Select("SELECT * FROM api_tokens ORDER BY created_at DESC")
    List<ApiTokenPO> list();

    @Update("UPDATE api_tokens SET name = #{name}, expire_at = #{expireAt}, updated_at = NOW() WHERE id = #{id}")
    int update(@Param("id") Long id, @Param("name") String name, @Param("expireAt") java.time.LocalDateTime expireAt);

    @Update("UPDATE api_tokens SET enabled = #{enabled}, updated_at = NOW() WHERE id = #{id}")
    int updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);

    @Delete("DELETE FROM api_tokens WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
