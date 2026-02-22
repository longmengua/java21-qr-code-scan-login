package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AdminProperties
 *
 * 從 application.yml 讀取管理員帳號設定
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {

    private List<AdminAccount> accounts;

    @Data
    public static class AdminAccount {
        private String username;
        private String password;
    }
}