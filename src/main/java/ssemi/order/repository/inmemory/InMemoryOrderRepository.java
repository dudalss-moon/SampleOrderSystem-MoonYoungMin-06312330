package ssemi.order.repository.inmemory;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.repository.OrderRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new LinkedHashMap<>();
    private int sequence = 1;

    @Override
    public void save(Order order) {
        store.put(order.getOrderId(), order);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return store.values().stream()
            .filter(o -> o.getStatus() == status)
            .toList();
    }

    @Override
    public List<Order> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public String generateId() {
        return String.format("ORD-%04d", sequence++);
    }
}
