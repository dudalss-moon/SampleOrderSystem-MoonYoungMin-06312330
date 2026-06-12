package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.ProductionQueueRepository;
import ssemi.order.repository.SampleRepository;
import ssemi.order.service.OrderService;
import ssemi.order.service.ProductionService;
import ssemi.order.service.ReleaseService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseServiceTest {

    private ReleaseService releaseService;
    private OrderRepository orderRepository;
    private SampleRepository sampleRepository;

    @BeforeEach
    void setUp() {
        orderRepository = new OrderRepository();
        sampleRepository = new SampleRepository();
        releaseService = new ReleaseService(orderRepository, sampleRepository);
    }

    private Order saveConfirmedOrder(String sampleId, int stock, int quantity) {
        Sample sample = new Sample(sampleId, "알파센서", 30, 0.9, stock);
        sampleRepository.save(sample);
        String orderId = orderRepository.generateId();
        Order order = new Order(orderId, sample, "홍길동", quantity);
        order.changeStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        return order;
    }

    @Test
    @DisplayName("findConfirmed는 CONFIRMED 상태 주문만 반환한다")
    void CONFIRMED_주문_목록_조회() {
        saveConfirmedOrder("S001", 10, 3);
        saveConfirmedOrder("S002", 10, 5);
        String reservedId = orderRepository.generateId();
        Order reserved = new Order(reservedId, new Sample("S003", "감마칩", 30, 0.9, 10), "김철수", 2);
        orderRepository.save(reserved);

        List<Order> confirmed = releaseService.findConfirmed();

        assertEquals(2, confirmed.size());
        assertTrue(confirmed.stream().allMatch(o -> o.getStatus() == OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("release() 호출 시 주문 상태가 RELEASE로 변경된다")
    void 출고_처리_RELEASE_전환() {
        Order order = saveConfirmedOrder("S001", 10, 3);

        releaseService.release(order.getOrderId());

        assertEquals(OrderStatus.RELEASE, order.getStatus());
    }

    @Test
    @DisplayName("stockDeducted=false인 CONFIRMED 주문 출고 시 sample.stock이 차감된다")
    void 재고부족_경로_출고_재고차감() {
        Order order = saveConfirmedOrder("S001", 5, 3); // stockDeducted 기본값 false

        releaseService.release(order.getOrderId());

        assertEquals(2, order.getSample().getStock());
    }

    @Test
    @DisplayName("stockDeducted=true인 CONFIRMED 주문 출고 시 stock이 차감되지 않는다")
    void 재고충분_경로_출고_차감없음() {
        Order order = saveConfirmedOrder("S001", 5, 3);
        order.markStockDeducted(); // 재고 충분 경로: 승인 시 이미 차감됨

        releaseService.release(order.getOrderId());

        assertEquals(5, order.getSample().getStock());
    }

    @Test
    @DisplayName("CONFIRMED 상태가 아닌 주문을 release하면 IllegalStateException이 발생한다")
    void CONFIRMED_아닌_주문_출고_예외() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 10);
        sampleRepository.save(sample);
        String orderId = orderRepository.generateId();
        Order reserved = new Order(orderId, sample, "홍길동", 3);
        orderRepository.save(reserved); // RESERVED 상태

        assertThrows(IllegalStateException.class,
            () -> releaseService.release(orderId));
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 release 호출 시 IllegalArgumentException이 발생한다")
    void 존재하지않는_주문_출고_예외() {
        assertThrows(IllegalArgumentException.class,
            () -> releaseService.release("NONE"));
    }

    @Test
    @DisplayName("출고 처리 후 해당 주문이 findConfirmed() 목록에서 제외된다")
    void 출고_후_CONFIRMED_목록에서_제거() {
        Order order = saveConfirmedOrder("S001", 10, 3);

        releaseService.release(order.getOrderId());

        assertTrue(releaseService.findConfirmed().isEmpty());
    }

    @Test
    @DisplayName("재고 충분 경로에서 출고 시 재고는 approve 시 1회만 차감된다")
    void 전체_흐름_재고충분() {
        // stock=10, qty=5 → approve(재고 충분 → CONFIRMED, 재고 5 차감) → release()
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 10);
        sampleRepository.save(sample);
        OrderService orderService = new OrderService(orderRepository, sampleRepository,
            new ProductionService(new ProductionQueueRepository(), orderRepository));

        Order order = orderService.reserve("S001", "홍길동", 5);
        orderService.approve(order.getOrderId()); // CONFIRMED, stock=5, stockDeducted=true 이어야 함

        releaseService.release(order.getOrderId());

        assertAll(
            () -> assertEquals(OrderStatus.RELEASE, order.getStatus()),
            () -> assertEquals(5, sample.getStock()) // approve 시 1회만 차감
        );
    }
}
