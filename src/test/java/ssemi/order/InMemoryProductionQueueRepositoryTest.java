package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.Sample;
import ssemi.order.repository.inmemory.InMemoryProductionQueueRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryProductionQueueRepositoryTest {

    private InMemoryProductionQueueRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryProductionQueueRepository();
    }

    private ProductionJob job(String jobId, String orderId) {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 0);
        Order order = new Order(orderId, sample, "홍길동", 5);
        return new ProductionJob(jobId, order, 5);
    }

    @Test
    @DisplayName("generateJobId는 JOB-0001, JOB-0002 순으로 순번이 증가한다")
    void generateJobId_순번_증가() {
        String id1 = repo.generateJobId();
        String id2 = repo.generateJobId();

        assertEquals("JOB-0001", id1);
        assertEquals("JOB-0002", id2);
    }

    @Test
    @DisplayName("빈 큐에서 peek와 dequeue는 Optional.empty()를 반환한다")
    void 빈_큐_peek_dequeue() {
        Optional<ProductionJob> peeked = repo.peek();
        Optional<ProductionJob> dequeued = repo.dequeue();

        assertTrue(peeked.isEmpty());
        assertTrue(dequeued.isEmpty());
    }

    @Test
    @DisplayName("3개 enqueue 후 dequeue 순서는 등록 순서와 동일하다 (FIFO)")
    void FIFO_순서_보장() {
        ProductionJob job1 = job("JOB-0001", "ORD-0001");
        ProductionJob job2 = job("JOB-0002", "ORD-0002");
        ProductionJob job3 = job("JOB-0003", "ORD-0003");
        repo.enqueue(job1);
        repo.enqueue(job2);
        repo.enqueue(job3);

        assertSame(job1, repo.dequeue().orElseThrow());
        assertSame(job2, repo.dequeue().orElseThrow());
        assertSame(job3, repo.dequeue().orElseThrow());
    }

    @Test
    @DisplayName("getQueue가 반환한 리스트를 수정해도 내부 큐에 영향이 없다 (방어적 복사)")
    void getQueue_방어적_복사() {
        ProductionJob job1 = job("JOB-0001", "ORD-0001");
        repo.enqueue(job1);

        List<ProductionJob> snapshot = repo.getQueue();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());

        assertEquals(1, repo.getQueue().size());
    }
}
