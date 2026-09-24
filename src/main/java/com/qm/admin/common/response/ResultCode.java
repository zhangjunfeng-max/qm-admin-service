package com.qm.admin.common.response;

import org.springframework.http.HttpStatus;

/**
 * Unified response code contract.
 */
public interface ResultCode {

    int code();

    String message();

    HttpStatus httpStatus();
}
