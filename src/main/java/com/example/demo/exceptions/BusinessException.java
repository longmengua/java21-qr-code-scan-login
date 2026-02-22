package com.example.demo.exceptions;

import com.example.demo.enums.BizErrorCode;

public class BusinessException extends RuntimeException {

    private final BizErrorCode bizErrorCode;

    public BusinessException(BizErrorCode bizErrorCode) {
        super(bizErrorCode.getMessage());
        this.bizErrorCode = bizErrorCode;
    }

    public BizErrorCode getErrorCode() {
        return bizErrorCode;
    }
}
