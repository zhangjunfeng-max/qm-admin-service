package com.qm.admin.common.exception;

import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.common.response.ResultCode;

/**
 * @author zjf
 */
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(String message) {
        this(CommonResultCode.BAD_REQUEST, message);
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode, resultCode.message());
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}
