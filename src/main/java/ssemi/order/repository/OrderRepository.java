package ssemi.order.repository;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String orderId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findAll();
    String generateId();
}
