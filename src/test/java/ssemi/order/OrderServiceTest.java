package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.inmemory.InMemoryOrderRepository;
import ssemi.order.repository.ProductionQueueRepository;
import ssemi.order.repository.SampleRepository;
import ssemi.order.repository.inmemory.InMemorySampleRepository;
import ssemi.order.service.OrderService;
import ssemi.order.service.ProductionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;
    private SampleRepository sampleRepository;
    private ProductionQueueRepository productionQueueRepo;

    @BeforeEach
    void setUp() {
        sampleRepository = new InMemorySampleRepository();
        productionQueueRepo = new ProductionQueueRepository();
        OrderRepository orderRepository = new InMemoryOrderRepository();
        orderService = new OrderService(
            orderRepository, sampleRepository,
            new ProductionService(productionQueueRepo, orderRepository)
        );
        sampleRepository.save(new Sample("S001", "알파센서", 30, 0.9, 10));
    }

    @Test
    @DisplayName("유효한 시료와 수량으로 reserve 호출 시 RESERVED 상태 주문이 반환된다")
    void 정상_주문_예약() {
        Order order = orderService.reserve("S001", "홍길동", 5);

        assertAll(
            () -> assertEquals(OrderStatus.RESERVED, order.getStatus()),
            () -> assertTrue(order.getOrderId().startsWith("ORD-")),
            () -> assertEquals("홍길동", order.getCustomerName()),
            () -> assertEquals(5, order.getQuantity())
        );
    }

    @Test
    @DisplayName("존재하지 않는 시료 ID로 주문 시 IllegalArgumentException이 발생한다")
    void 존재하지않는_시료_주문_예외() {
        assertThrows(IllegalArgumentException.class,
            () -> orderService.reserve("NONE", "홍길동", 5));
    }

    @Test
    @DisplayName("주문 수량이 0 이하이면 IllegalArgumentException이 발생한다")
    void 수량_0이하_예약_예외() {
        assertThrows(IllegalArgumentException.class,
            () -> orderService.reserve("S001", "홍길동", 0));
    }

    @Test
    @DisplayName("재고가 충분할 때 approve 호출 시 주문 상태가 CONFIRMED로 변경되고 재고가 차감된다")
    void 재고_충분_승인_CONFIRMED() {
        // stock=10, quantity=5 → 재고 충분
        Order order = orderService.reserve("S001", "홍길동", 5);

        Order approved = orderService.approve(order.getOrderId());

        assertAll(
            () -> assertEquals(OrderStatus.CONFIRMED, approved.getStatus()),
            () -> assertEquals(5, sampleRepository.findById("S001").get().getStock())
        );
    }

    @Test
    @DisplayName("재고가 부족할 때 approve 호출 시 주문 상태가 PRODUCING으로 변경되고 생산큐에 등록된다")
    void 재고_부족_승인_PRODUCING() {
        // stock=10, quantity=15 → 재고 부족
        sampleRepository.save(new Sample("S002", "베타칩", 20, 0.9, 10));
        Order order = orderService.reserve("S002", "김철수", 15);

        Order approved = orderService.approve(order.getOrderId());

        assertAll(
            () -> assertEquals(OrderStatus.PRODUCING, approved.getStatus()),
            () -> assertEquals(1, productionQueueRepo.getQueue().size())
        );
    }

    @Test
    @DisplayName("reject 호출 시 주문 상태가 REJECTED로 변경된다")
    void 거절_REJECTED() {
        Order order = orderService.reserve("S001", "홍길동", 5);

        Order rejected = orderService.reject(order.getOrderId());

        assertEquals(OrderStatus.REJECTED, rejected.getStatus());
    }

    @Test
    @DisplayName("RESERVED 상태가 아닌 주문을 approve 하면 IllegalStateException이 발생한다")
    void RESERVED_아닌_주문_승인_예외() {
        // stock=10, quantity=5 → CONFIRMED으로 먼저 전이
        Order order = orderService.reserve("S001", "홍길동", 5);
        orderService.approve(order.getOrderId()); // CONFIRMED 상태로 전이

        assertThrows(IllegalStateException.class,
            () -> orderService.approve(order.getOrderId()));
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 approve 호출 시 IllegalArgumentException이 발생한다")
    void 존재하지않는_주문_승인_예외() {
        assertThrows(IllegalArgumentException.class,
            () -> orderService.approve("NONE"));
    }

    @Test
    @DisplayName("재고 부족 승인 시 생산 작업의 targetQty가 ceil(부족분 / (yield * 0.9))로 계산된다")
    void 재고_부족시_생산량_계산_정확() {
        // stock=2, quantity=10, yield=0.8 → shortage=8
        // targetQty = ceil(8 / (0.8 * 0.9)) = ceil(8 / 0.72) = ceil(11.11) = 12
        sampleRepository.save(new Sample("S003", "감마칩", 30, 0.8, 2));
        Order order = orderService.reserve("S003", "이영희", 10);

        orderService.approve(order.getOrderId());

        assertEquals(12, productionQueueRepo.peek().get().getTargetQty());
    }

    @Test
    @DisplayName("findReserved는 RESERVED 상태 주문만 반환한다")
    void RESERVED_주문_목록_조회() {
        orderService.reserve("S001", "홍길동", 5);
        orderService.reserve("S001", "김철수", 3);
        Order o3 = orderService.reserve("S001", "이영희", 2);
        o3.changeStatus(OrderStatus.CONFIRMED);

        List<Order> reserved = orderService.findReserved();

        assertEquals(2, reserved.size());
        assertTrue(reserved.stream().allMatch(o -> o.getStatus() == OrderStatus.RESERVED));
    }
}
