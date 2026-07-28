package cn.dextea.trade.health;
import cn.dextea.trade.common.api.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "健康检测")
@RequiredArgsConstructor
public class HealthController {
    private final HealthService healthService;
    @GetMapping("/mysql")
    @Operation(summary = "检测 MySQL 健康")
    public ResponseEntity<APIResponse<HealthResult>> mysql() {
        return of(healthService.checkMysql());
    }
    @GetMapping("/redis")
    @Operation(summary = "检测 Redis 健康")
    public ResponseEntity<APIResponse<HealthResult>> redis() {
        return of(healthService.checkRedis());
    }
    @GetMapping("/backend")
    @Operation(summary = "检测后端服务健康")
    public ResponseEntity<APIResponse<HealthResult>> backend() {
        return of(healthService.checkBackend());
    }
    private ResponseEntity<APIResponse<HealthResult>> of(HealthResult result) {
        APIResponse<HealthResult> body = result.isUp()
                ? APIResponse.success(result)
                : APIResponse.error(5001, result.getMessage(), result);
        HttpStatus status = result.isUp() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(body);
    }
}
