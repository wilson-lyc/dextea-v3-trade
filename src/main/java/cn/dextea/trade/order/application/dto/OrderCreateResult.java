package cn.dextea.trade.order.application.dto;

import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

/**
 * 创建订单结果（应用层 DTO），供接口层映射为对外响应。
 *
 * <p>该对象会被 {@code OrderApplicationServiceImpl} 经 Jackson 序列化进 Redis 幂等缓存，
 * 重试时再反序列化复用，故必须可被 Jackson 重建——{@link Jacksonized} 让 Lombok 生成
 * 基于 Builder 的 {@code @JsonCreator}，否则私有字段无 setter 会导致反序列化得到全空对象。</p>
 */
@Getter
@Builder
@Jacksonized
public class OrderCreateResult {

    private Long id;

    private String orderNo;

    private String tradeNo;

    /** 支付过期时间点（系统计算并已同步支付宝），前端可据此做支付倒计时 */
    private LocalDateTime payExpireAt;

    private PreBuildResult preBuild;
}
