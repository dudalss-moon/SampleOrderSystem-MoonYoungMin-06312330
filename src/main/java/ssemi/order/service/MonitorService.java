package ssemi.order.service;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.SampleStockInfo;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.SampleRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MonitorService {

    private static final Set<OrderStatus> MONITOR_STATUSES =
        Set.of(OrderStatus.RESERVED, OrderStatus.PRODUCING, OrderStatus.CONFIRMED, OrderStatus.RELEASE);

    private final OrderRepository orderRepository;
    private final SampleRepository sampleRepository;

    public MonitorService(OrderRepository orderRepository, SampleRepository sampleRepository) {
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
    }

    public Map<OrderStatus, List<Order>> getOrdersByStatus() {
        Map<OrderStatus, List<Order>> grouped = orderRepository.findAll().stream()
            .filter(o -> MONITOR_STATUSES.contains(o.getStatus()))
            .collect(Collectors.groupingBy(Order::getStatus));
        // 주문이 없는 상태도 빈 리스트로 보장
        for (OrderStatus status : MONITOR_STATUSES) {
            grouped.putIfAbsent(status, List.of());
        }
        return grouped;
    }

    public List<SampleStockInfo> getStockInfos() {
        return sampleRepository.findAll().stream()
            .map(sample -> new SampleStockInfo(sample, calcPendingQty(sample.getId())))
            .toList();
    }

    private int calcPendingQty(String sampleId) {
        return orderRepository.findAll().stream()
            .filter(o -> o.getSample().getId().equals(sampleId))
            .filter(o -> o.getStatus() == OrderStatus.RESERVED || o.getStatus() == OrderStatus.PRODUCING)
            .mapToInt(Order::getQuantity)
            .sum();
    }
}
