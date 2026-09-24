package com.qm.admin.common.response;

import org.springframework.http.HttpStatus;

public enum CommonResultCode implements ResultCode {

    SUCCESS(0, "success", HttpStatus.OK),
    BAD_REQUEST(400, "bad request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "forbidden", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "not found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(405, "method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    CONFLICT(409, "conflict", HttpStatus.CONFLICT),
    UNSUPPORTED_MEDIA_TYPE(415, "unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INTERNAL_SERVER_ERROR(500, "internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    CommonResultCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
