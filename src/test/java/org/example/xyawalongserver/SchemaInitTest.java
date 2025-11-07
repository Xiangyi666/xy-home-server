package org.example.xyawalongserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SchemaInitTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDataPersistedAfterRestart() throws SQLException {
//        // 插入测试数据
//        System.out.println("------sql test-----");
//
//        try (Connection conn = dataSource.getConnection();
//             Statement stmt = conn.createStatement()) {
//
//            stmt.execute("INSERT INTO ingredient (name, unit) VALUES ('测试原料', '个')");
//
//            // 重启应用后，这个数据应该还在
//            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ingredient WHERE name = '测试原料'");
//            rs.next();
//            assertTrue(rs.getInt(1) > 0);
//        }
    }
}