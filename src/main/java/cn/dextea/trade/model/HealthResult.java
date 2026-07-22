package cn.dextea.trade.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 健康检查返回结果。
 * status 取值约定：UP 表示组件正常，DOWN 表示异常。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResult {

    /**
     * 被检查的组件名称，如 mysql / redis / backend
     */
    private String component;

    /**
     * 健康状态：UP / DOWN
     */
    private String status;

    /**
     * 描述信息
     */
    private String message;

    /**
     * 检测耗时（毫秒）
     */
    private Long durationMillis;

    /**
     * 检测发生时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 附带细节（如连接信息、异常信息等），可为空
     */
    private Map<String, Object> details;

    public boolean isUp() {
        return "UP".equalsIgnoreCase(status);
    }

    public static HealthResult up(String component, String message, long durationMillis, Map<String, Object> details) {
        return HealthResult.builder()
                .component(component)
                .status("UP")
                .message(message)
                .durationMillis(durationMillis)
                .timestamp(System.currentTimeMillis())
                .details(details)
                .build();
    }

    public static HealthResult down(String component, String message, long durationMillis, Map<String, Object> details) {
        return HealthResult.builder()
                .component(component)
                .status("DOWN")
                .message(message)
                .durationMillis(durationMillis)
                .timestamp(System.currentTimeMillis())
                .details(details)
                .build();
    }
}
