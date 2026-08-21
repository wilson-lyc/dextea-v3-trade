package cn.dextea.trade.console.interfaces.http;

import cn.dextea.trade.console.application.usecase.TokenManageUseCase;
import cn.dextea.trade.shared.config.AuthConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthConfig authConfig;
    private final TokenManageUseCase tokenManageUseCase;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!authConfig.isEnabled()) {
            return true;
        }
        String header = request.getHeader(AUTH_HEADER);
        String token = null;
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            token = header.substring(BEARER_PREFIX.length()).trim();
        } else if (header != null && !header.isBlank()) {
            token = header.trim();
        }
        if (token == null || token.isBlank()) {
            writeUnauthorized(response, "请求未携带令牌");
            return false;
        }
        try {
            tokenManageUseCase.verify(token);
            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "令牌校验失败");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            response.getWriter().write("{\"code\":40100,\"message\":\"" + message + "\",\"data\":null}");
        } catch (Exception e) {
            log.warn("写入未授权响应失败", e);
        }
    }
}
