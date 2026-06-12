package ssemi.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.db.DatabaseConfig;
import ssemi.order.db.SchemaInitializer;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaInitializerTest {

    private DatabaseConfig config;

    @BeforeEach
    void setUp() {
        config = new DatabaseConfig("jdbc:h2:mem:schematest;DB_CLOSE_DELAY=-1");
    }

    @AfterEach
    void tearDown() {
        config.close();
    }

    @Test
    @DisplayName("initialize() 호출 후 samples, orders, production_jobs, sequences 테이블이 존재한다")
    void 스키마_초기화_테이블_생성() throws Exception {
        SchemaInitializer.initialize(config.getConnection());

        DatabaseMetaData meta = config.getConnection().getMetaData();
        Set<String> tables = new HashSet<>();
        try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME").toUpperCase());
            }
        }

        assertTrue(tables.contains("SAMPLES"));
        assertTrue(tables.contains("ORDERS"));
        assertTrue(tables.contains("PRODUCTION_JOBS"));
        assertTrue(tables.contains("SEQUENCES"));
    }
}
