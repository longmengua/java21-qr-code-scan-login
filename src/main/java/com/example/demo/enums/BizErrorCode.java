package com.example.demo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum BizErrorCode {

    AUTH_INVALID_CREDENTIAL(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤"),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "登入已過期"),
    USER_DISABLED(HttpStatus.FORBIDDEN, "帳號已停權"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "參數錯誤"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "系統錯誤");

    private final HttpStatus status;
    private final String message;
}
