package com.chat.chat.common.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 헬스체크 및 데이터베이스 연결 테스트 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 기본 헬스체크
     */
    @GetMapping
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "chat-backend");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 데이터베이스 연결 테스트
     */
    @GetMapping("/db")
    public Map<String, Object> databaseCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. DataSource 연결 테스트
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5);
                response.put("connection", isValid ? "SUCCESS" : "FAILED");
                response.put("database", connection.getCatalog());
                response.put("url", connection.getMetaData().getURL());
                response.put("driver", connection.getMetaData().getDriverName());
                response.put("driverVersion", connection.getMetaData().getDriverVersion());
            }

            // 2. 간단한 쿼리 실행 테스트
            String result = jdbcTemplate.queryForObject("SELECT version()", String.class);
            response.put("postgresVersion", result);

            // 3. 현재 시간 조회 테스트
            String currentTime = jdbcTemplate.queryForObject("SELECT NOW()", String.class);
            response.put("currentTime", currentTime);

            response.put("status", "SUCCESS");
            log.info("✅ Database connection test SUCCESS");

        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            log.error("❌ Database connection test FAILED: {}", e.getMessage(), e);
        }

        return response;
    }

    /**
     * 테이블 존재 여부 확인
     */
    @GetMapping("/tables")
    public Map<String, Object> checkTables() {
        Map<String, Object> response = new HashMap<>();

        try {
            String query = """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                AND table_type = 'BASE TABLE'
                ORDER BY table_name
                """;

            var tables = jdbcTemplate.queryForList(query, String.class);
            response.put("status", "SUCCESS");
            response.put("tableCount", tables.size());
            response.put("tables", tables);

            log.info("✅ Found {} tables in database", tables.size());

        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            log.error("❌ Failed to retrieve tables: {}", e.getMessage(), e);
        }

        return response;
    }
}
