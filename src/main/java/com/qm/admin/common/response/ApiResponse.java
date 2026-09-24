package com.qm.admin.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "统一接口响应")
public record ApiResponse<T>(
        @Schema(description = "业务状态码", example = "0")
        int code,
        @Schema(description = "响应消息", example = "success")
        String message,
        @Schema(description = "响应数据")
        T data,
        @Schema(description = "请求追踪 ID", example = "018f8d1f6e994b91b6d56c7a4eaa0001")
        String traceId

) {

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return of(CommonResultCode.SUCCESS, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return of(CommonResultCode.SUCCESS, data, traceId, null);
    }

    public static <T> ApiResponse<T> failure(ResultCode resultCode, String traceId) {
        return of(resultCode, null, traceId, null);
    }

    public static <T> ApiResponse<T> failure(ResultCode resultCode, String traceId, String message) {
        return of(resultCode, null, traceId, message);
    }

    public static <T> ApiResponse<T> of(ResultCode resultCode, T data, String traceId, String message) {
        return new ApiResponse<>(
                resultCode.code(),
                message == null || message.isBlank() ? resultCode.message() : message,
                data,
                traceId
        );
    }
}
