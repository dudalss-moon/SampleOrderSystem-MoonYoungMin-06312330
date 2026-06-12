package ssemi.order.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig implements AutoCloseable {

    private final Connection connection;

    public DatabaseConfig(String jdbcUrl) {
        try {
            this.connection = DriverManager.getConnection(jdbcUrl);
        } catch (SQLException e) {
            throw new IllegalStateException("DB 연결 실패: " + e.getMessage(), e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB 연결 종료 실패: " + e.getMessage(), e);
        }
    }
}
