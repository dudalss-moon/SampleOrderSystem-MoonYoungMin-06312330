package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.Sample;
import ssemi.order.repository.ProductionQueueRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductionQueueRepositoryTest {

    private ProductionQueueRepository repo;

    @BeforeEach
    void setUp() {
        repo = new ProductionQueueRepository();
    }

    private ProductionJob createJob(String jobId, int stock, int quantity) {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, stock);
        Order order = new Order("O001", sample, "홍길동", quantity);
        return new ProductionJob(jobId, order, quantity - stock);
    }

    @Test
    @DisplayName("enqueue한 첫 번째 작업을 peek으로 조회하면 동일한 객체가 반환된다")
    void 작업_enqueue_후_peek_동일() {
        ProductionJob job = createJob("JOB-0001", 3, 10);

        repo.enqueue(job);

        assertEquals(job, repo.peek().get());
    }

    @Test
    @DisplayName("빈 큐에서 peek 호출 시 Optional.empty()가 반환된다")
    void 빈_큐_peek_빈값() {
        assertTrue(repo.peek().isEmpty());
    }

    @Test
    @DisplayName("enqueue 순서대로 dequeue된다")
    void FIFO_순서_보장() {
        ProductionJob job1 = createJob("JOB-0001", 3, 10);
        ProductionJob job2 = createJob("JOB-0002", 1, 5);

        repo.enqueue(job1);
        repo.enqueue(job2);

        assertEquals(job1, repo.dequeue().get());
        assertEquals(job2, repo.dequeue().get());
    }
}
