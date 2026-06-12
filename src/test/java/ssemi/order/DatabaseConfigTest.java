package ssemi.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.db.DatabaseConfig;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DatabaseConfigTest {

    @Test
    @DisplayName("H2 in-memory URL로 DatabaseConfig 생성 시 유효한 Connection이 반환된다")
    void 데이터베이스_연결_정상() throws Exception {
        DatabaseConfig config = new DatabaseConfig("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        Connection connection = config.getConnection();

        assertFalse(connection.isClosed());

        config.close();
    }
}
