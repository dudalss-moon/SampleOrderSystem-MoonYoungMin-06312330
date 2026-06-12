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
    @DisplayName("findByStatus는 해당 상태의 주문만 반환한다")
    void 상태별_주문_조회() {
        Sample s2 = new Sample("S002", "베타칩", 20, 0.8, 5);
        sampleRepository.save(s2);
        Order o1 = new Order(repository.generateId(), sample, "홍길동", 5);
        Order o2 = new Order(repository.generateId(), sample, "김철수", 3);
        Order o3 = new Order(repository.generateId(), s2,     "이영희", 2);
        o3.changeStatus(OrderStatus.CONFIRMED);
        repository.save(o1);
        repository.save(o2);
        repository.save(o3);

        var reserved = repository.findByStatus(OrderStatus.RESERVED);

        assertEquals(2, reserved.size());
        assertTrue(reserved.stream().allMatch(o -> o.getStatus() == OrderStatus.RESERVED));
    }

    @Test
    @DisplayName("generateId는 ORD-0001, ORD-0002 순으로 순번이 증가한다")
    void ID_자동생성_순번_증가() {
        String first  = repository.generateId();
        String second = repository.generateId();

        assertEquals("ORD-0001", first);
        assertEquals("ORD-0002", second);
    }

    @Test
    @DisplayName("markStockDeducted 후 save하면 DB의 stock_deducted가 true로 저장된다")
    void stockDeducted_플래그_DB_반영() {
        String orderId = repository.generateId();
        Order order = new Order(orderId, sample, "홍길동", 5);
        repository.save(order);

        order.markStockDeducted();
        repository.save(order);

        Order found = repository.findById(orderId).orElseThrow();
        assertTrue(found.isStockDeducted());
    }

    @Test
    @DisplayName("changeStatus 후 save하면 DB의 status 값이 갱신된다")
    void 상태_변경_DB_반영() {
        String orderId = repository.generateId();
        Order order = new Order(orderId, sample, "홍길동", 5);
        repository.save(order);

        order.changeStatus(OrderStatus.CONFIRMED);
        repository.save(order);

        Order found = repository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, found.getStatus());
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
