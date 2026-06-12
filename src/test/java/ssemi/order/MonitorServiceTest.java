package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.SampleRepository;
import ssemi.order.service.MonitorService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MonitorServiceTest {

    private MonitorService monitorService;
    private SampleRepository sampleRepository;
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        sampleRepository = new SampleRepository();
        orderRepository = new OrderRepository();
        monitorService = new MonitorService(orderRepository, sampleRepository);
        sampleRepository.save(new Sample("S001", "알파센서", 30, 0.9, 10));
    }

    private Order reserveOrder(String sampleId, String customer, int qty) {
        String orderId = orderRepository.generateId();
        Order order = new Order(orderId, sampleRepository.findById(sampleId).get(), customer, qty);
        orderRepository.save(order);
        return order;
    }

    @Test
    @DisplayName("해당 상태의 주문이 없으면 빈 리스트가 반환된다")
    void 주문없는_상태_빈리스트() {
        reserveOrder("S001", "홍길동", 3); // RESERVED만 존재

        Map<OrderStatus, List<Order>> result = monitorService.getOrdersByStatus();

        assertAll(
            () -> assertTrue(result.get(OrderStatus.PRODUCING).isEmpty()),
            () -> assertTrue(result.get(OrderStatus.CONFIRMED).isEmpty()),
            () -> assertTrue(result.get(OrderStatus.RELEASE).isEmpty())
        );
    }

    @Test
    @DisplayName("getOrdersByStatus는 RESERVED/PRODUCING/CONFIRMED/RELEASE 4개 키를 반환하고 REJECTED는 제외한다")
    void 주문현황_상태별_그룹핑() {
        Order o1 = reserveOrder("S001", "홍길동", 3);
        Order o2 = reserveOrder("S001", "김철수", 2);
        o2.changeStatus(OrderStatus.CONFIRMED);
        Order o3 = reserveOrder("S001", "이영희", 1);
        o3.changeStatus(OrderStatus.REJECTED);

        Map<OrderStatus, List<Order>> result = monitorService.getOrdersByStatus();

        assertAll(
            () -> assertTrue(result.containsKey(OrderStatus.RESERVED)),
            () -> assertTrue(result.containsKey(OrderStatus.PRODUCING)),
            () -> assertTrue(result.containsKey(OrderStatus.CONFIRMED)),
            () -> assertTrue(result.containsKey(OrderStatus.RELEASE)),
            () -> assertFalse(result.containsKey(OrderStatus.REJECTED))
        );
    }
}
