package cn.dextea.trade.shared.config.otel;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class TraceInterceptor implements HandlerInterceptor {

    private final OpenTelemetry openTelemetry;

    private static final String SPAN_ATTRIBUTE = "otel.span";
    private static final String SCOPE_ATTRIBUTE = "otel.scope";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    private static final TextMapGetter<Map<String, String>> GETTER = new TextMapGetter<>() {
        @Override
        public String get(Map<String, String> carrier, String key) {
            return carrier.get(key);
        }

        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier.keySet();
        }
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Tracer tracer = openTelemetry.getTracer("dextea-trade", "1.0.0");

        Map<String, String> headers = extractHeaders(request);
        Context parentContext = openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), headers, GETTER);

        String spanName = (handler instanceof HandlerMethod hm)
                ? hm.getBeanType().getSimpleName() + "#" + hm.getMethod().getName()
                : request.getRequestURI();

        Span span = tracer.spanBuilder(spanName)
                .setParent(parentContext)
                .setAttribute("http.method", request.getMethod())
                .setAttribute("http.route", request.getRequestURI())
                .setAttribute("http.client_ip", request.getRemoteAddr())
                .startSpan();

        Scope scope = span.makeCurrent();
        MDC.put(TRACE_ID_MDC_KEY, span.getSpanContext().getTraceId());
        request.setAttribute(SPAN_ATTRIBUTE, span);
        request.setAttribute(SCOPE_ATTRIBUTE, scope);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Span span = (Span) request.getAttribute(SPAN_ATTRIBUTE);
        Scope scope = (Scope) request.getAttribute(SCOPE_ATTRIBUTE);
        try {
            if (span != null) {
                span.setAttribute("http.status_code", response.getStatus());
                if (ex != null) {
                    span.setStatus(StatusCode.ERROR, ex.getMessage());
                    span.recordException(ex);
                } else if (response.getStatus() >= 500) {
                    span.setStatus(StatusCode.ERROR, "server error " + response.getStatus());
                } else {
                    span.setStatus(StatusCode.OK);
                }
                span.end();
            }
        } finally {
            if (scope != null) {
                scope.close();
            }
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                headers.put(name.toLowerCase(), request.getHeader(name));
            }
        }
        return headers;
    }
}
