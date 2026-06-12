package ssemi.order.service;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.SampleRepository;

import java.util.List;

public final class ReleaseService {

    private final OrderRepository orderRepository;
    private final SampleRepository sampleRepository;

    public ReleaseService(OrderRepository orderRepository, SampleRepository sampleRepository) {
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
    }

    public List<Order> findConfirmed() {
        return orderRepository.findByStatus(OrderStatus.CONFIRMED);
    }

    public Order release(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("CONFIRMED 상태의 주문만 출고할 수 있습니다.");
        }
        if (!order.isStockDeducted()) {
            order.getSample().deductStock(order.getQuantity());
            order.markStockDeducted();
        }
        order.changeStatus(OrderStatus.RELEASE);
        return order;
    }
}
