package cn.dextea.trade.shared.infrastructure.web;
import cn.dextea.trade.shared.api.APIResponse;
import cn.dextea.trade.shared.domain.error.BizError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.sql.SQLException;
import java.util.stream.Collectors;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizError.class)
    public APIResponse<Void> handleBizError(BizError ex) {
        log.warn("业务异常: code={}, errorCode={}, message={}",
                ex.getCode(), ex.getErrorCode(), ex.getMessage());
        return APIResponse.error(ex.getCode(), ex.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public APIResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return APIResponse.error(HttpStatus.BAD_REQUEST.value(), message);
    }
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public APIResponse<Void> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return APIResponse.error(HttpStatus.BAD_REQUEST.value(), message);
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public APIResponse<Void> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = "缺少必填参数: " + ex.getParameterName();
        log.warn(message);
        return APIResponse.error(HttpStatus.BAD_REQUEST.value(), message);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public APIResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "参数类型不正确: " + ex.getName();
        log.warn("{}, requiredType={}", message, ex.getRequiredType());
        return APIResponse.error(HttpStatus.BAD_REQUEST.value(), message);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public APIResponse<Void> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败: {}", ex.getMessage());
        return APIResponse.error(HttpStatus.BAD_REQUEST.value(), "请求体格式错误");
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public APIResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String message = "不支持的请求方法: " + ex.getMethod();
        log.warn(message);
        return APIResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), message);
    }
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public APIResponse<Void> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = "请求资源不存在: " + ex.getRequestURL();
        log.warn(message);
        return APIResponse.error(HttpStatus.NOT_FOUND.value(), message);
    }
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public APIResponse<Void> handleDataAccess(DataAccessException ex) {
        log.error("数据库访问异常", ex);
        return APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "数据库繁忙，请稍后重试");
    }
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public APIResponse<Void> handleSqlException(SQLException ex) {
        log.error("SQL 执行异常", ex);
        return APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "数据库繁忙，请稍后重试");
    }
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public APIResponse<Void> handleRuntimeException(RuntimeException ex) {
        log.error("运行时异常", ex);
        return APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常");
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public APIResponse<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常");
    }
    private static String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
