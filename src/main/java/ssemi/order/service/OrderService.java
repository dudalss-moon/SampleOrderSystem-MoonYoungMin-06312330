package ssemi.order.service;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.SampleRepository;

import java.util.List;

public final class OrderService {

    private final OrderRepository orderRepository;
    private final SampleRepository sampleRepository;

    public OrderService(OrderRepository orderRepository, SampleRepository sampleRepository) {
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
    }

    public Order reserve(String sampleId, String customerName, int quantity) {
        Sample sample = sampleRepository.findById(sampleId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시료 ID입니다: " + sampleId));
        String orderId = orderRepository.generateId();
        Order order = new Order(orderId, sample, customerName, quantity);
        orderRepository.save(order);
        return order;
    }

    public List<Order> findReserved() {
        return orderRepository.findByStatus(OrderStatus.RESERVED);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order findById(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID입니다: " + orderId));
    }
}
