package com.courseselect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 数据库初始化器 —— 仅在数据库为空时执行 data.sql 灌入种子数据。
 * 重启不会覆盖运行时数据（选课记录、enrolled 计数等）。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DataSource dataSource;

    public DataInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        if (isEmpty()) {
            log.info("数据库为空，执行 data.sql 种子数据初始化...");
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("data.sql"));
            populator.setSeparator(";");
            populator.execute(dataSource);
            log.info("种子数据初始化完成");
        } else {
            log.info("数据库已有数据，跳过种子数据初始化");
        }
    }

    /** 检查 teachers 表是否为空（作为数据库是否需要初始化的信号） */
    private boolean isEmpty() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM teachers")) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (Exception e) {
            // 表不存在 → 空库
            log.debug("teachers 表可能尚未创建: {}", e.getMessage());
            return true;
        }
        return true;
    }
}
