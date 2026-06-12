package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.SampleRepository;
import ssemi.order.repository.inmemory.InMemorySampleRepository;
import ssemi.order.service.MonitorService;

import ssemi.order.domain.SampleStockInfo;
import ssemi.order.domain.StockStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MonitorServiceTest {

    private MonitorService monitorService;
    private SampleRepository sampleRepository;
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        sampleRepository = new InMemorySampleRepository();
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
    @DisplayName("재고가 0이면 StockStatus가 DEPLETED다")
    void 고갈_판단_정확성() {
        sampleRepository.save(new Sample("S002", "베타칩", 20, 0.8, 0)); // stock=0

        List<SampleStockInfo> infos = monitorService.getStockInfos();
        SampleStockInfo info = infos.stream()
            .filter(i -> i.getSample().getId().equals("S002"))
            .findFirst().get();

        assertEquals(StockStatus.DEPLETED, info.getStockStatus());
    }

    @Test
    @DisplayName("재고가 대기주문량보다 적으면 StockStatus가 SHORTAGE다")
    void 부족_판단_정확성() {
        sampleRepository.save(new Sample("S003", "감마칩", 20, 0.8, 2)); // stock=2
        Order o = reserveOrder("S003", "홍길동", 5); // pending=5 → 2 < 5

        List<SampleStockInfo> infos = monitorService.getStockInfos();
        SampleStockInfo info = infos.stream()
            .filter(i -> i.getSample().getId().equals("S003"))
            .findFirst().get();

        assertEquals(StockStatus.SHORTAGE, info.getStockStatus());
    }

    @Test
    @DisplayName("pendingQuantity는 RESERVED와 PRODUCING 상태 주문 수량의 합이다")
    void 대기주문량_RESERVED_PRODUCING_합산() {
        // RESERVED 수량=3, PRODUCING 수량=4, CONFIRMED 수량=5 → pending=7
        Order o1 = reserveOrder("S001", "홍길동", 3);           // RESERVED
        Order o2 = reserveOrder("S001", "김철수", 4);
        o2.changeStatus(OrderStatus.PRODUCING);                   // PRODUCING
        Order o3 = reserveOrder("S001", "이영희", 5);
        o3.changeStatus(OrderStatus.CONFIRMED);                   // CONFIRMED (제외)

        List<SampleStockInfo> infos = monitorService.getStockInfos();
        SampleStockInfo info = infos.stream()
            .filter(i -> i.getSample().getId().equals("S001"))
            .findFirst().get();

        assertEquals(7, info.getPendingQuantity());
    }

    @Test
    @DisplayName("getStockInfos는 등록된 모든 시료에 대한 SampleStockInfo를 반환한다")
    void 재고현황_전체_시료_포함() {
        sampleRepository.save(new Sample("S002", "베타칩", 20, 0.8, 5));

        List<SampleStockInfo> infos = monitorService.getStockInfos();

        assertEquals(2, infos.size());
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
    @DisplayName("REJECTED 상태 주문은 주문 현황에 포함되지 않는다")
    void REJECTED_주문_제외_확인() {
        Order o = reserveOrder("S001", "홍길동", 3);
        o.changeStatus(OrderStatus.REJECTED);

        Map<OrderStatus, List<Order>> result = monitorService.getOrdersByStatus();

        assertFalse(result.containsKey(OrderStatus.REJECTED));
        assertTrue(result.get(OrderStatus.RESERVED).isEmpty());
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
