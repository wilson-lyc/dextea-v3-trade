package cn.dextea.trade.shared.infrastructure.web;

import cn.dextea.trade.shared.api.APIResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

/**
 * 响应工具：统一封装 APIResponse 并透传上游链路 ID（tradeid）。
 * 本系统作为中台，只从上游转发请求 Header 中读取 tradeid；
 * 读到则原样写回响应 Header 并作为链路键，读不到则不补、不生成。
 */
public final class ResponseUtils {

    public static final String TRADE_ID_HEADER = "tradeid";

    private ResponseUtils() {
    }

    public static ResponseEntity<APIResponse<Void>> of(int code, String message, HttpStatus status,
                                                       HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        resolveTradeId(request).ifPresent(tradeId -> headers.add(TRADE_ID_HEADER, tradeId));
        return new ResponseEntity<>(APIResponse.error(code, message), headers, status);
    }

    public static Optional<String> resolveTradeId(HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        String tradeId = request.getHeader(TRADE_ID_HEADER);
        if (tradeId == null || tradeId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(tradeId);
    }
}
