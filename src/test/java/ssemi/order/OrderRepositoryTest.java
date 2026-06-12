package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.OrderRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    private OrderRepository repository;
    private Sample sample;

    @BeforeEach
    void setUp() {
        repository = new OrderRepository();
        sample = new Sample("S001", "알파센서", 30, 0.9, 10);
    }

    @Test
    @DisplayName("저장한 주문을 ID로 조회하면 동일한 객체가 반환된다")
    void 주문_저장_후_ID로_조회() {
        String id = repository.generateId();
        Order order = new Order(id, sample, "홍길동", 5);
        repository.save(order);

        var found = repository.findById(id);

        assertTrue(found.isPresent());
        assertEquals(id, found.get().getOrderId());
    }

    @Test
    @DisplayName("findByStatus는 해당 상태의 주문만 반환한다")
    void 상태별_주문_조회() {
        Order o1 = new Order(repository.generateId(), sample, "홍길동", 5);
        Order o2 = new Order(repository.generateId(), sample, "김철수", 3);
        Order o3 = new Order(repository.generateId(), sample, "이영희", 2);
        o3.changeStatus(OrderStatus.CONFIRMED);
        repository.save(o1);
        repository.save(o2);
        repository.save(o3);

        List<Order> reserved = repository.findByStatus(OrderStatus.RESERVED);

        assertEquals(2, reserved.size());
        assertTrue(reserved.stream().allMatch(o -> o.getStatus() == OrderStatus.RESERVED));
    }

    @Test
    @DisplayName("generateId는 ORD-0001, ORD-0002 순으로 순번이 증가한다")
    void ID_자동생성_순번_증가() {
        String first = repository.generateId();
        String second = repository.generateId();

        assertEquals("ORD-0001", first);
        assertEquals("ORD-0002", second);
    }

    @Test
    @DisplayName("findAll은 저장된 주문을 등록 순서대로 반환한다")
    void 전체_주문_조회_등록순_보장() {
        String id1 = repository.generateId();
        String id2 = repository.generateId();
        repository.save(new Order(id1, sample, "홍길동", 5));
        repository.save(new Order(id2, sample, "김철수", 3));

        List<Order> all = repository.findAll();

        assertEquals(2, all.size());
        assertEquals(id1, all.get(0).getOrderId());
        assertEquals(id2, all.get(1).getOrderId());
    }
}
