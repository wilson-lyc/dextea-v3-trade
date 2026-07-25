package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.OrderStatusLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

/**
 * 订单状态变更日志 Mapper。
 */
@Mapper
public interface OrderStatusLogMapper {

    /**
     * 插入一条状态变更日志。
     *
     * @param log 日志记录
     * @return 影响行数
     */
    @Insert("INSERT INTO order_status_log (order_no, from_status, to_status, event, operator, version) " +
            "VALUES (#{orderNo}, #{fromStatus}, #{toStatus}, #{event}, #{operator}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderStatusLog log);

    /**
     * 按订单号查询变更日志条数（用于排查与审计）。
     *
     * @param orderNo 订单号
     * @return 日志条数
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM order_status_log WHERE order_no = #{orderNo}")
    int countByOrderNo(@Param("orderNo") String orderNo);
}
