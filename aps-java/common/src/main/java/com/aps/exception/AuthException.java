package com.aps.exception;

import lombok.Getter;

/**
 * 认证/授权异常
 */
@Getter
public class AuthException extends RuntimeException {

    private final Integer code;

    public AuthException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public AuthException(String message) {
        super(message);
        this.code = 40100;
    }
}