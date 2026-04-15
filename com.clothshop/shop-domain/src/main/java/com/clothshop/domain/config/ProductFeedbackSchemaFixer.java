package com.clothshop.domain.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductFeedbackSchemaFixer implements CommandLineRunner {

    private static final String TABLE_NAME = "product_feedback";
    private static final String TARGET_UNIQUE_NAME = "uk_product_feedback_product_customer";

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        if (!tableExists(TABLE_NAME)) {
            return;
        }

        dropWrongSingleColumnUniqueIndex();
        ensureCompositeUniqueIndex();
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private void dropWrongSingleColumnUniqueIndex() {
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList("""
                SELECT INDEX_NAME
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                GROUP BY INDEX_NAME
                HAVING MAX(NON_UNIQUE) = 0
                   AND INDEX_NAME <> 'PRIMARY'
                   AND SUM(CASE WHEN COLUMN_NAME = 'product_id' THEN 1 ELSE 0 END) = 1
                   AND COUNT(*) = 1
                """, TABLE_NAME);

        for (Map<String, Object> row : indexes) {
            String indexName = String.valueOf(row.get("INDEX_NAME"));
            jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " DROP INDEX " + indexName);
            log.info("Dropped wrong unique index '{}' on product_feedback(product_id).", indexName);
        }
    }

    private void ensureCompositeUniqueIndex() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT INDEX_NAME
                    FROM information_schema.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                    GROUP BY INDEX_NAME
                    HAVING MAX(NON_UNIQUE) = 0
                       AND SUM(CASE WHEN COLUMN_NAME = 'product_id' THEN 1 ELSE 0 END) = 1
                       AND SUM(CASE WHEN COLUMN_NAME = 'customer_id' THEN 1 ELSE 0 END) = 1
                       AND COUNT(*) = 2
                ) AS uq
                """, Integer.class, TABLE_NAME);

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE " + TABLE_NAME + " ADD CONSTRAINT " + TARGET_UNIQUE_NAME
                        + " UNIQUE (product_id, customer_id)"
        );
        log.info("Added unique constraint '{}' on product_feedback(product_id, customer_id).", TARGET_UNIQUE_NAME);
    }
}
