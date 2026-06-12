package ssemi.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ssemi.order.db.DatabaseConfig;
import ssemi.order.db.SchemaInitializer;
import ssemi.order.domain.Order;
import ssemi.order.domain.Sample;
import ssemi.order.repository.jdbc.JdbcOrderRepository;
import ssemi.order.repository.jdbc.JdbcSampleRepository;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DbPersistenceIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("DB 재연결 후에도 저장된 시료와 주문 데이터가 유지된다")
    void DB_재연결_후_데이터_유지() throws Exception {
        String dbPath = tempDir.resolve("test").toString().replace("\\", "/");
        String jdbcUrl = "jdbc:h2:file:" + dbPath + ";DB_CLOSE_ON_EXIT=FALSE";

        // 1차 연결: 데이터 저장
        try (DatabaseConfig config = new DatabaseConfig(jdbcUrl)) {
            Connection conn = config.getConnection();
            SchemaInitializer.initialize(conn);
            JdbcSampleRepository sampleRepo = new JdbcSampleRepository(conn);
            JdbcOrderRepository orderRepo = new JdbcOrderRepository(conn);
            Sample sample = new Sample("S001", "알파센서", 30, 0.9, 10);
            sampleRepo.save(sample);
            String orderId = orderRepo.generateId();
            orderRepo.save(new Order(orderId, sample, "홍길동", 5));
        }

        // 2차 재연결: 데이터 확인
        try (DatabaseConfig config = new DatabaseConfig(jdbcUrl)) {
            Connection conn = config.getConnection();
            SchemaInitializer.initialize(conn);
            JdbcSampleRepository sampleRepo = new JdbcSampleRepository(conn);
            JdbcOrderRepository orderRepo = new JdbcOrderRepository(conn);

            List<Sample> samples = sampleRepo.findAll();
            List<Order> orders = orderRepo.findAll();

            assertAll(
                () -> assertEquals(1, samples.size()),
                () -> assertEquals("S001", samples.get(0).getId()),
                () -> assertEquals(1, orders.size()),
                () -> assertEquals("ORD-0001", orders.get(0).getOrderId())
            );
        }
    }
}
