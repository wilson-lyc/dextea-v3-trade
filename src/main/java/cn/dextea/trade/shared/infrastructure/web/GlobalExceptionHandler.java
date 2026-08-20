package cn.dextea.trade.shared.infrastructure.web;

import cn.dextea.trade.shared.api.APIResponse;
import cn.dextea.trade.shared.error.BizError;
import cn.dextea.trade.shared.error.CommonErrorCode;
import cn.dextea.trade.shared.error.SystemException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private HttpServletRequest request;

    @ExceptionHandler(BizError.class)
    public ResponseEntity<APIResponse<Void>> handleBizError(BizError ex) {
        log.warn("业务异常: code={}, errorCode={}, message={}",
                ex.getCode(), ex.getErrorCode(), ex.getMessage());
        return ResponseUtils.of(ex.getCode(), ex.getMessage(),
                resolveHttpStatus(ex.getCode()), request);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<APIResponse<Void>> handleSystemException(SystemException ex) {
        log.error("系统异常: code={}, message={}", ex.getCode(), ex.getMessage(), ex);
        return ResponseUtils.of(ex.getCode(), ex.getMessage(),
                resolveHttpStatus(ex.getCode()), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<APIResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return ResponseUtils.of(CommonErrorCode.PARAM_MISSING.getCode(), message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<APIResponse<Void>> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return ResponseUtils.of(CommonErrorCode.PARAM_MISSING.getCode(), message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<APIResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = "缺少必填参数: " + ex.getParameterName();
        log.warn(message);
        return ResponseUtils.of(CommonErrorCode.PARAM_MISSING.getCode(), message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<APIResponse<Void>> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        String message = CommonErrorCode.MISSING_REQUEST_HEADER.getMessage() + ": " + ex.getHeaderName();
        log.warn(message);
        return ResponseUtils.of(CommonErrorCode.MISSING_REQUEST_HEADER.getCode(), message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<APIResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "参数类型不正确: " + ex.getName();
        log.warn("{}, requiredType={}", message, ex.getRequiredType());
        return ResponseUtils.of(CommonErrorCode.PARAM_MISSING.getCode(), message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<APIResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败: {}", ex.getMessage());
        return ResponseUtils.of(CommonErrorCode.PARAM_MISSING.getCode(), "请求体格式错误", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<APIResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String message = "不支持的请求方法: " + ex.getMethod();
        log.warn(message);
        return ResponseUtils.of(CommonErrorCode.PARAM_MISSING.getCode(), message, HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<APIResponse<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = "请求资源不存在: " + ex.getRequestURL();
        log.warn(message);
        return ResponseUtils.of(CommonErrorCode.NOT_FOUND.getCode(), message, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<APIResponse<Void>> handleDataAccess(DataAccessException ex) {
        log.error("数据库访问异常", ex);
        return ResponseUtils.of(CommonErrorCode.MYBATIS_SYSTEM_EXCEPTION.getCode(),
                CommonErrorCode.MYBATIS_SYSTEM_EXCEPTION.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<APIResponse<Void>> handleSqlException(SQLException ex) {
        log.error("SQL 执行异常", ex);
        return ResponseUtils.of(CommonErrorCode.MYBATIS_SYSTEM_EXCEPTION.getCode(),
                CommonErrorCode.MYBATIS_SYSTEM_EXCEPTION.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(MyBatisSystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<APIResponse<Void>> handleMyBatisSystem(MyBatisSystemException ex) {
        log.error("MyBatis 系统异常", ex);
        return ResponseUtils.of(CommonErrorCode.MYBATIS_SYSTEM_EXCEPTION.getCode(),
                CommonErrorCode.MYBATIS_SYSTEM_EXCEPTION.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<APIResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("运行时异常", ex);
        return ResponseUtils.of(CommonErrorCode.SYSTEM_ERROR.getCode(),
                CommonErrorCode.SYSTEM_ERROR.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<APIResponse<Void>> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ResponseUtils.of(CommonErrorCode.SYSTEM_ERROR.getCode(),
                CommonErrorCode.SYSTEM_ERROR.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private static HttpStatus resolveHttpStatus(int code) {
        if (code >= 40000 && code < 50000) {
            return HttpStatus.BAD_REQUEST;
        }
        if (code >= 50000 && code < 60000) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        if (code >= 20000 && code < 30000) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static String formatFieldError(FieldError fieldError) {
        return fieldError.getDefaultMessage();
    }
}
