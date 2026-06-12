package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.Sample;
import ssemi.order.repository.inmemory.InMemoryOrderRepository;
import ssemi.order.repository.inmemory.InMemoryProductionQueueRepository;
import ssemi.order.service.ProductionService;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AutoProductionServiceTest {

    private ProductionService productionService;
    private InMemoryProductionQueueRepository queueRepo;
    private InMemoryOrderRepository orderRepo;

    @BeforeEach
    void setUp() {
        queueRepo = new InMemoryProductionQueueRepository();
        orderRepo = new InMemoryOrderRepository();
        productionService = new ProductionService(queueRepo, orderRepo);
    }

    private ProductionJob enqueueJob(int avgTime, int stock, int quantity) {
        Sample sample = new Sample("S001", "알파센서", avgTime, 0.9, stock);
        String orderId = orderRepo.generateId();
        Order order = new Order(orderId, sample, "홍길동", quantity);
        orderRepo.save(order);
        order.changeStatus(OrderStatus.PRODUCING);
        int shortage = quantity - stock;
        ProductionJob job = new ProductionJob(queueRepo.generateJobId(), order, shortage);
        queueRepo.enqueue(job);
        return job;
    }

    @Test
    @DisplayName("큐가 비어있을 때 processAutoProduction 호출 시 예외 없이 종료된다")
    void 빈_큐_processAutoProduction_무시() {
        assertDoesNotThrow(() -> productionService.processAutoProduction());
    }

    @Test
    @DisplayName("startTime이 null이면 processAutoProduction 호출 시 현재 시각으로 자동 설정된다")
    void startTime_없을때_자동설정() {
        ProductionJob job = enqueueJob(30, 0, 5);
        assertNull(job.getStartTime());
        productionService.processAutoProduction();
        assertNotNull(job.getStartTime());
    }

    @Test
    @DisplayName("과거 startTime 설정 시 processAutoProduction 호출로 생산량이 증가한다")
    void 경과시간_기반_생산량_자동_갱신() {
        ProductionJob job = enqueueJob(1, 0, 3);
        job.setStartTime(Instant.now().minusSeconds(100));
        productionService.processAutoProduction();
        assertTrue(job.getProducedQty() > 0);
    }

    @Test
    @DisplayName("자동 생산 완료 시 재고가 추가되고 주문이 CONFIRMED로 변경된다")
    void 자동생산_완료_재고증가_CONFIRMED() {
        ProductionJob job = enqueueJob(1, 0, 3);
        int targetQty = job.getTargetQty();
        job.setStartTime(Instant.now().minusSeconds(targetQty + 10));
        productionService.processAutoProduction();
        assertAll(
            () -> assertEquals(OrderStatus.CONFIRMED, job.getOrder().getStatus()),
            () -> assertTrue(job.getOrder().getSample().getStock() > 0)
        );
    }

    @Test
    @DisplayName("첫 번째 작업 완료 후 다음 작업의 startTime이 자동 설정된다")
    void 자동생산_완료_후_다음작업_startTime_자동설정() {
        ProductionJob job1 = enqueueJob(1, 0, 3);

        Sample s2 = new Sample("S002", "베타칩", 1, 0.9, 0);
        String orderId2 = orderRepo.generateId();
        Order order2 = new Order(orderId2, s2, "김철수", 3);
        orderRepo.save(order2);
        order2.changeStatus(OrderStatus.PRODUCING);
        ProductionJob job2 = new ProductionJob(queueRepo.generateJobId(), order2, 3);
        queueRepo.enqueue(job2);

        job1.setStartTime(Instant.now().minusSeconds(job1.getTargetQty() + 10));
        productionService.processAutoProduction();

        assertNotNull(job2.getStartTime());
    }
}
