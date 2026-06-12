package ssemi.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.db.DatabaseConfig;
import ssemi.order.db.SchemaInitializer;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.jdbc.JdbcOrderRepository;
import ssemi.order.repository.jdbc.JdbcSampleRepository;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcOrderRepositoryTest {

    private DatabaseConfig config;
    private JdbcOrderRepository repository;
    private JdbcSampleRepository sampleRepository;
    private Sample sample;

    @BeforeEach
    void setUp() throws Exception {
        config = new DatabaseConfig("jdbc:h2:mem:orderrepotest;DB_CLOSE_DELAY=-1");
        Connection conn = config.getConnection();
        SchemaInitializer.initialize(conn);
        conn.createStatement().execute("DELETE FROM production_jobs");
        conn.createStatement().execute("DELETE FROM orders");
        conn.createStatement().execute("DELETE FROM samples");
        conn.createStatement().execute("UPDATE sequences SET next_val = 1 WHERE name = 'order'");
        sampleRepository = new JdbcSampleRepository(conn);
        repository = new JdbcOrderRepository(conn);
        sample = new Sample("S001", "알파센서", 30, 0.9, 10);
        sampleRepository.save(sample);
    }

    @AfterEach
    void tearDown() {
        config.close();
    }

    @Test
    @DisplayName("save 후 findById로 동일한 주문이 반환된다")
    void 주문_저장_후_ID로_조회() {
        String orderId = repository.generateId();
        Order order = new Order(orderId, sample, "홍길동", 5);
        repository.save(order);

        Optional<Order> found = repository.findById(orderId);

        assertTrue(found.isPresent());
        assertAll(
            () -> assertEquals(orderId,           found.get().getOrderId()),
            () -> assertEquals("홍길동",           found.get().getCustomerName()),
            () -> assertEquals(5,                  found.get().getQuantity()),
            () -> assertEquals(OrderStatus.RESERVED, found.get().getStatus()),
            () -> assertFalse(found.get().isStockDeducted())
        );
    }
}
