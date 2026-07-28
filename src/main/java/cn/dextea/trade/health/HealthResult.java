package cn.dextea.trade.health;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResult {
    private String component;
    private String status;
    private String message;
    private Long durationMillis;
    private Long timestamp;
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
