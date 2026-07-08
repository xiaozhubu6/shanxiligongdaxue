package com.zhuqilong.back.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try {
            initializeServiceReviewTable();
        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
        }
    }

    private void initializeServiceReviewTable() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建contract表（如果不存在）
            stmt.execute("CREATE TABLE IF NOT EXISTS contract (" +
                "id bigint NOT NULL AUTO_INCREMENT," +
                "elder_id bigint NOT NULL," +
                "child_id bigint NOT NULL," +
                "contract_date datetime DEFAULT CURRENT_TIMESTAMP," +
                "status varchar(20) DEFAULT 'ACTIVE'," +
                "created_at datetime DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (id)," +
                "KEY FK_contract_elder (elder_id)," +
                "KEY FK_contract_child (child_id)," +
                "FOREIGN KEY (elder_id) REFERENCES elder (id)," +
                "FOREIGN KEY (child_id) REFERENCES child (id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");
            
            // 检查字段是否已存在
            boolean hasType = false;
            boolean hasStatus = false;
            boolean hasCompletedAt = false;
            boolean hasAttitude = false;
            boolean hasResponse = false;
            boolean hasQuality = false;
            boolean hasSatisfaction = false;
            boolean hasSuggestion = false;
            
            // 检查表结构
            var rs = stmt.executeQuery("DESCRIBE service_review");
            while (rs.next()) {
                String columnName = rs.getString("Field");
                if ("type".equals(columnName)) hasType = true;
                if ("status".equals(columnName)) hasStatus = true;
                if ("completed_at".equals(columnName)) hasCompletedAt = true;
                if ("attitude".equals(columnName)) hasAttitude = true;
                if ("response".equals(columnName)) hasResponse = true;
                if ("quality".equals(columnName)) hasQuality = true;
                if ("satisfaction".equals(columnName)) hasSatisfaction = true;
                if ("suggestion".equals(columnName)) hasSuggestion = true;
            }
            rs.close();
            
            // 添加缺失的字段
            if (!hasType) {
                stmt.execute("ALTER TABLE service_review ADD COLUMN type VARCHAR(20) DEFAULT NULL COMMENT '评价类型：ELDER或CHILD'");
            }
            if (!hasStatus) {
                stmt.execute("ALTER TABLE service_review ADD COLUMN status VARCHAR(20) DEFAULT NULL COMMENT '状态：PENDING、PUSHED、COMPLETED'");
            }
            if (!hasCompletedAt) {
                stmt.execute("ALTER TABLE service_review ADD COLUMN completed_at datetime(6) DEFAULT NULL COMMENT '完成时间'");
            }
            if (!hasAttitude) {
                stmt.execute("ALTER TABLE service_review ADD COLUMN attitude int DEFAULT NULL COMMENT '服务态度评分'");
            }
            if (!hasResponse) {
                stmt.execute("ALTER TABLE service_review ADD COLUMN response int DEFAULT NULL COMMENT '响应速度评分'");
            }
            if (!hasQuality) {
                stmt.execute("ALTER TABLE service_review ADD COLUMN quality int DEFAULT NULL COMMENT '服务质量评分'");
            }
            if (!hasSatisfaction) {
                stmt.execute("ALTER TABLE service_review ADD COLUMN satisfaction int DEFAULT NULL COMMENT '整体满意度评分'");
            }
            if (!hasSuggestion) {
                stmt.execute("ALTER TABLE service_review ADD COLUMN suggestion varchar(500) DEFAULT NULL COMMENT '改进建议'");
            }
            
            // 更新现有记录的状态
            if (hasStatus) {
                // 将没有明确状态的记录设置为PUSHED（如果它们是最近创建的）
                stmt.executeUpdate("UPDATE service_review SET status = 'PUSHED' WHERE status IS NULL OR status = 'PENDING'");
            }
            
            // 更新现有记录的类型
            if (hasType) {
                // 根据childId判断类型：有childId的是CHILD，没有的是ELDER
                stmt.executeUpdate("UPDATE service_review SET type = CASE WHEN child_id IS NULL THEN 'ELDER' ELSE 'CHILD' END WHERE type IS NULL");
            }
            
            System.out.println("ServiceReview表结构更新完成");
            
        } catch (Exception e) {
            System.err.println("更新ServiceReview表失败: " + e.getMessage());
        }
    }
}
