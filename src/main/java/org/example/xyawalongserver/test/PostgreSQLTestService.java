package org.example.xyawalongserver.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;

@Service
public class PostgreSQLTestService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLTestService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== PostgreSQL 数据库连接测试开始 ===");

        try {
            // 测试1: 检查 EntityManager 连接状态
            testConnection();

            // 测试2: 执行简单查询验证数据库可用性
            testSimpleQuery();

            // 测试3: 检查数据库版本信息
            testDatabaseInfo();

            logger.info("🎉 PostgreSQL 数据库连接测试成功！");

        } catch (Exception e) {
            logger.error("❌ PostgreSQL 数据库连接测试失败: {}", e.getMessage());
            e.printStackTrace();
        }

        logger.info("=== PostgreSQL 数据库连接测试结束 ===");
    }

    /**
     * 测试基础连接
     */
    private void testConnection() {
        logger.info("--- 测试1: 基础连接测试 ---");

        boolean isOpen = entityManager.isOpen();
        if (isOpen) {
            logger.info("✅ EntityManager 连接状态: 正常");
        } else {
            logger.error("❌ EntityManager 连接状态: 异常");
            throw new RuntimeException("EntityManager 连接失败");
        }
    }

    /**
     * 测试简单查询
     */
    private void testSimpleQuery() {
        logger.info("--- 测试2: 简单查询测试 ---");

        // 使用原生 SQL 查询当前时间
        Query query = entityManager.createNativeQuery("SELECT NOW() as current_time");
        Object result = query.getSingleResult();

        logger.info("✅ 数据库当前时间: {}", result);
        logger.info("✅ 简单查询执行成功");
    }

    /**
     * 测试数据库信息
     */
    private void testDatabaseInfo() {
        logger.info("--- 测试3: 数据库信息测试 ---");

        // 查询 PostgreSQL 版本
        Query versionQuery = entityManager.createNativeQuery("SELECT version()");
        Object version = versionQuery.getSingleResult();
        logger.info("✅ PostgreSQL 版本: {}", version);

        // 查询当前数据库名称
        Query dbQuery = entityManager.createNativeQuery("SELECT current_database()");
        Object dbName = dbQuery.getSingleResult();
        logger.info("✅ 当前数据库: {}", dbName);

        // 查询当前用户
        Query userQuery = entityManager.createNativeQuery("SELECT current_user");
        Object userName = userQuery.getSingleResult();
        logger.info("✅ 当前用户: {}", userName);

        // 查询连接数信息（可选）
        try {
            Query connQuery = entityManager.createNativeQuery(
                    "SELECT count(*) FROM pg_stat_activity WHERE datname = current_database()"
            );
            Object connectionCount = connQuery.getSingleResult();
            logger.info("✅ 当前数据库连接数: {}", connectionCount);
        } catch (Exception e) {
            logger.warn("⚠️ 连接数查询失败（可能权限不足）: {}", e.getMessage());
        }
    }
}