package ssemi.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.db.DatabaseConfig;
import ssemi.order.db.SchemaInitializer;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.Sample;
import ssemi.order.repository.jdbc.JdbcOrderRepository;
import ssemi.order.repository.jdbc.JdbcProductionQueueRepository;
import ssemi.order.repository.jdbc.JdbcSampleRepository;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class JdbcProductionQueueRepositoryTest {

    private DatabaseConfig config;
    private JdbcProductionQueueRepository repository;
    private JdbcOrderRepository orderRepository;
    private Sample sample;
    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() throws Exception {
        config = new DatabaseConfig("jdbc:h2:mem:jobqueuetest;DB_CLOSE_DELAY=-1");
        Connection conn = config.getConnection();
        SchemaInitializer.initialize(conn);
        conn.createStatement().execute("DELETE FROM production_jobs");
        conn.createStatement().execute("DELETE FROM orders");
        conn.createStatement().execute("DELETE FROM samples");
        conn.createStatement().execute("UPDATE sequences SET next_val = 1 WHERE name = 'order'");
        conn.createStatement().execute("UPDATE sequences SET next_val = 1 WHERE name = 'job'");

        JdbcSampleRepository sampleRepository = new JdbcSampleRepository(conn);
        orderRepository = new JdbcOrderRepository(conn);
        repository = new JdbcProductionQueueRepository(conn, orderRepository);

        sample = new Sample("S001", "알파센서", 30, 0.9, 2);
        sampleRepository.save(sample);

        String id1 = orderRepository.generateId();
        order1 = new Order(id1, sample, "홍길동", 5);
        order1.changeStatus(OrderStatus.PRODUCING);
        orderRepository.save(order1);

        String id2 = orderRepository.generateId();
        order2 = new Order(id2, sample, "김철수", 4);
        order2.changeStatus(OrderStatus.PRODUCING);
        orderRepository.save(order2);
    }

    @AfterEach
    void tearDown() {
        config.close();
    }

    @Test
    @DisplayName("enqueue(job) 재호출 시 producedQty가 DB에 갱신된다")
    void 생산_진행_후_producedQty_DB_반영() {
        ProductionJob job = new ProductionJob(repository.generateJobId(), order1, 3);
        repository.enqueue(job);

        job.produce(2);
        repository.enqueue(job);  // MERGE INTO → producedQty 갱신

        ProductionJob loaded = repository.peek().orElseThrow();
        assertEquals(2, loaded.getProducedQty());
    }

    @Test
    @DisplayName("enqueue 후 peek으로 첫 번째 작업이 반환되고 FIFO 순서가 보장된다")
    void 작업_저장_후_enqueue_순서_조회() {
        ProductionJob job1 = new ProductionJob(repository.generateJobId(), order1, 3);
        ProductionJob job2 = new ProductionJob(repository.generateJobId(), order2, 4);
        repository.enqueue(job1);
        repository.enqueue(job2);

        var peeked = repository.peek();
        assertTrue(peeked.isPresent());
        assertEquals(job1.getJobId(), peeked.get().getJobId());

        repository.dequeue();
        assertEquals(job2.getJobId(), repository.peek().get().getJobId());
    }
}
