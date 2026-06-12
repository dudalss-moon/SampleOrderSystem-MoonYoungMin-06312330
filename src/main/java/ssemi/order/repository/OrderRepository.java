package ssemi.order.repository;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class OrderRepository {

    private final Map<String, Order> store = new LinkedHashMap<>();
    private int sequence = 1;

    public void save(Order order) {
        store.put(order.getOrderId(), order);
    }

    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    public List<Order> findByStatus(OrderStatus status) {
        return store.values().stream()
            .filter(o -> o.getStatus() == status)
            .toList();
    }

    public List<Order> findAll() {
        return List.copyOf(store.values());
    }

    public String generateId() {
        return String.format("ORD-%04d", sequence++);
    }
}
