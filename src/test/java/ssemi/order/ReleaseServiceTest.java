package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.SampleRepository;
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
}
