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
    private final ProductionService productionService;

    public OrderService(OrderRepository orderRepository, SampleRepository sampleRepository,
                        ProductionService productionService) {
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
        this.productionService = productionService;
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

    public Order approve(String orderId) {
        Order order = findById(orderId);
        if (order.getSample().hasEnoughStock(order.getQuantity())) {
            order.getSample().deductStock(order.getQuantity());
            order.changeStatus(OrderStatus.CONFIRMED);
        } else {
            order.changeStatus(OrderStatus.PRODUCING);
            productionService.createJob(order);
        }
        return order;
    }

    public Order reject(String orderId) {
        Order order = findById(orderId);
        order.changeStatus(OrderStatus.REJECTED);
        return order;
    }
}
