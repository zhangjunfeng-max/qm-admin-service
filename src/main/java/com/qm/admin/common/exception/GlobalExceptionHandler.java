package com.qm.admin.common.exception;

import com.qm.admin.common.response.ApiResponse;
import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.common.response.ResultCode;
import com.qm.admin.common.web.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex, WebRequest request) {
        logClientFailure("business exception", ex, request, ex.getResultCode(), ex.getMessage());
        return buildResponse(ex.getResultCode(), request, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex,
                                                                         WebRequest request) {
        logClientFailure("access denied", ex, request, CommonResultCode.FORBIDDEN, "无权执行该操作");
        return buildResponse(CommonResultCode.FORBIDDEN, request, "无权执行该操作");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex,
                                                                               WebRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        logClientFailure("constraint violation", ex, request, CommonResultCode.BAD_REQUEST, message);
        return buildResponse(CommonResultCode.BAD_REQUEST, request, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                         WebRequest request) {
        String message = getBindingMessage(ex);
        logClientFailure("method argument validation failure", ex, request, CommonResultCode.BAD_REQUEST, message);
        return buildResponse(CommonResultCode.BAD_REQUEST, request, message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex, WebRequest request) {
        String message = getBindingMessage(ex);
        logClientFailure("request binding failure", ex, request, CommonResultCode.BAD_REQUEST, message);
        return buildResponse(CommonResultCode.BAD_REQUEST, request, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        String message = ex.getParameterName() + " is required";
        logClientFailure("missing request parameter", ex, request, CommonResultCode.BAD_REQUEST, message);
        return buildResponse(CommonResultCode.BAD_REQUEST, request, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                         WebRequest request) {
        logClientFailure("malformed request body", ex, request, CommonResultCode.BAD_REQUEST,
                "request body is invalid");
        return buildResponse(CommonResultCode.BAD_REQUEST, request, "request body is invalid");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        logClientFailure("unsupported HTTP method", ex, request, CommonResultCode.METHOD_NOT_ALLOWED, null);
        return buildResponse(CommonResultCode.METHOD_NOT_ALLOWED, request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                            WebRequest request) {
        logClientFailure("unsupported request media type", ex, request, CommonResultCode.UNSUPPORTED_MEDIA_TYPE, null);
        return buildResponse(CommonResultCode.UNSUPPORTED_MEDIA_TYPE, request, null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                          WebRequest request) {
        logClientFailure("no handler found", ex, request, CommonResultCode.NOT_FOUND, null);
        return buildResponse(CommonResultCode.NOT_FOUND, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception ex, WebRequest request) {
        log.error("Unhandled exception: method={}, uri={}, traceId={}, exceptionType={}",
                getRequestMethod(request), getRequestUri(request), getTraceId(request), ex.getClass().getName(), ex);
        return buildResponse(CommonResultCode.INTERNAL_SERVER_ERROR, request, null);
    }

    /**
     * 客户端请求导致的异常属于可预期失败，记录 warn 便于定位问题，同时避免以 error 级别制造告警噪声。
     * 只记录请求方法、URI 和 traceId，不记录请求体或查询参数，避免日志泄露敏感信息。
     */
    private void logClientFailure(String event, Exception ex, WebRequest request, ResultCode resultCode,
                                  String responseMessage) {
        log.warn("{}: method={}, uri={}, traceId={}, exceptionType={}, exceptionMessage={}, resultCode={}, "
                        + "httpStatus={}, responseMessage={}",
                event,
                getRequestMethod(request),
                getRequestUri(request),
                getTraceId(request),
                ex.getClass().getName(),
                ex.getMessage(),
                resultCode.code(),
                resultCode.httpStatus().value(),
                responseMessage);
    }

    private String getRequestMethod(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getMethod();
        }
        return "UNKNOWN";
    }

    private String getRequestUri(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            HttpServletRequest servletRequest = servletWebRequest.getRequest();
            return servletRequest.getRequestURI();
        }
        return "UNKNOWN";
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ResultCode resultCode, WebRequest request, String message) {
        return ResponseEntity
                .status(resultCode.httpStatus())
                .body(ApiResponse.failure(resultCode, getTraceId(request), message));
    }

    private String getBindingMessage(BindException ex) {
        return ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));
    }

    private String getTraceId(WebRequest request) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getHeader(TraceIdFilter.TRACE_ID_HEADER);
        }
        return null;
    }
}
