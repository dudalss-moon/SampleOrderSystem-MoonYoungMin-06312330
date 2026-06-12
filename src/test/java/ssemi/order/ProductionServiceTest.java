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
}
