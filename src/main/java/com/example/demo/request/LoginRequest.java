package com.example.demo.request;

import com.example.demo.enums.BizErrorCode;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.model.LoginType;
import com.example.demo.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class LoginRequest{
    private String username;
    private String password;
    private LoginType loginType;

    /**
     * 參數檢查
     */
    public void validateInput() {
        if (StringUtil.isBlank(username)) {
            log.info("missed username");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        if (StringUtil.isBlank(password)) {
            log.info("missed password");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        if (loginType == null) {
            log.info("missed loginType");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }
    }
}

