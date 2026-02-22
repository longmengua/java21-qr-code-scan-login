package com.example.demo.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter implements Filter {

    @Override
    public void doFilter(
            jakarta.servlet.ServletRequest request,
            jakarta.servlet.ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        try {
            // 1️⃣ 生成唯一 traceId（或從 header 讀）
            String traceId = UUID.randomUUID().toString();

            // 2️⃣ 放入 MDC
            MDC.put("traceId", traceId);

            // 3️⃣ 繼續處理 request
            chain.doFilter(request, response);

        } finally {
            // 4️⃣ 清理 MDC 避免 memory leak
            MDC.clear();
        }
    }
}