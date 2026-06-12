package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.inmemory.InMemoryOrderRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryOrderRepositoryTest {

    private InMemoryOrderRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryOrderRepository();
    }

    private Sample sample() {
        return new Sample("S001", "알파센서", 30, 0.9, 100);
    }

    private Order order(String orderId, int quantity) {
        return new Order(orderId, sample(), "홍길동", quantity);
    }

    @Test
    @DisplayName("generateId는 ORD-0001, ORD-0002 순으로 순번이 증가한다")
    void generateId_순번_증가() {
        String id1 = repo.generateId();
        String id2 = repo.generateId();

        assertEquals("ORD-0001", id1);
        assertEquals("ORD-0002", id2);
    }

    @Test
    @DisplayName("같은 orderId로 재저장 시 최신 객체로 덮어쓴다")
    void save_동일_ID_덮어쓰기() {
        Order order1 = order("ORD-0001", 3);
        repo.save(order1);
        Order order2 = new Order("ORD-0001", sample(), "이순신", 7);
        repo.save(order2);

        Order found = repo.findById("ORD-0001").orElseThrow();
        assertEquals("이순신", found.getCustomerName());
        assertEquals(7, found.getQuantity());
    }

    @Test
    @DisplayName("해당 상태 주문이 없을 때 findByStatus는 빈 리스트를 반환한다")
    void findByStatus_빈_결과() {
        Order o = order("ORD-0001", 5);
        repo.save(o);

        List<Order> result = repo.findByStatus(OrderStatus.CONFIRMED);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 Optional.empty()를 반환한다")
    void findById_없는_ID() {
        Optional<Order> result = repo.findById("NONE");

        assertTrue(result.isEmpty());
    }
}
