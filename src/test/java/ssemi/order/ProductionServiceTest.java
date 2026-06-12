package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.ProductionResult;
import ssemi.order.domain.Sample;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.ProductionQueueRepository;
import ssemi.order.service.ProductionService;

import static org.junit.jupiter.api.Assertions.*;

class ProductionServiceTest {

    private ProductionService productionService;
    private ProductionQueueRepository productionQueueRepo;
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        productionQueueRepo = new ProductionQueueRepository();
        orderRepository = new OrderRepository();
        productionService = new ProductionService(productionQueueRepo, orderRepository);
    }

    private ProductionJob enqueueJob(String sampleId, int stock, int quantity) {
        Sample sample = new Sample(sampleId, "알파센서", 30, 0.9, stock);
        String orderId = orderRepository.generateId();
        Order order = new Order(orderId, sample, "홍길동", quantity);
        orderRepository.save(order);
        order.changeStatus(OrderStatus.PRODUCING);
        int shortage = quantity - stock;
        ProductionJob job = new ProductionJob(productionQueueRepo.generateJobId(), order, shortage);
        productionQueueRepo.enqueue(job);
        return job;
    }

    @Test
    @DisplayName("processProduction 호출 후 목표 미달 시 isCompleted는 false이고 producedQty가 증가한다")
    void 생산_진행_미완료() {
        ProductionJob job = enqueueJob("S001", 0, 4); // targetQty=ceil(4/0.81)=5

        ProductionResult result = productionService.processProduction(2);

        assertAll(
            () -> assertFalse(result.isCompleted()),
            () -> assertEquals(2, job.getProducedQty())
        );
    }

    @Test
    @DisplayName("생산 완료 시 sample.stock에 targetQty가 추가된다")
    void 생산_진행_완료_재고증가() {
        // stock=2, quantity=5, shortage=3, yield=0.9 → targetQty=ceil(3/0.81)=4
        ProductionJob job = enqueueJob("S001", 2, 5);
        Sample sample = job.getOrder().getSample();

        productionService.processProduction(job.getTargetQty());

        assertEquals(2 + job.getTargetQty(), sample.getStock());
    }

    @Test
    @DisplayName("생산 완료 시 주문 상태가 CONFIRMED로 변경된다")
    void 생산_완료_주문상태_CONFIRMED() {
        ProductionJob job = enqueueJob("S001", 0, 4);

        productionService.processProduction(job.getTargetQty());

        assertEquals(OrderStatus.CONFIRMED, job.getOrder().getStatus());
    }

    @Test
    @DisplayName("생산 완료 후 해당 작업이 큐에서 제거된다")
    void 생산_완료_큐에서_제거() {
        ProductionJob job = enqueueJob("S001", 0, 4);

        productionService.processProduction(job.getTargetQty());

        assertTrue(productionService.getCurrentJob().isEmpty());
    }

    @Test
    @DisplayName("2개 작업 등록 후 첫 번째 완료 시 다음 작업을 peek할 수 있다")
    void 생산_완료_다음_작업_자동시작() {
        ProductionJob job1 = enqueueJob("S001", 0, 4);
        ProductionJob job2 = enqueueJob("S002", 0, 4);

        productionService.processProduction(job1.getTargetQty());

        assertEquals(job2, productionService.getCurrentJob().get());
    }

    @Test
    @DisplayName("큐가 비어있을 때 processProduction 호출 시 IllegalStateException이 발생한다")
    void 생산_큐_없을때_진행_예외() {
        assertThrows(IllegalStateException.class,
            () -> productionService.processProduction(5));
    }

    @Test
    @DisplayName("targetQty를 초과하는 수량 입력 시 targetQty로 보정된다")
    void 생산량_초과_입력_처리() {
        ProductionJob job = enqueueJob("S001", 0, 4); // targetQty=ceil(4/0.81)=5

        ProductionResult result = productionService.processProduction(100);

        assertAll(
            () -> assertEquals(job.getTargetQty(), job.getProducedQty()),
            () -> assertTrue(result.isCompleted())
        );
    }
}
