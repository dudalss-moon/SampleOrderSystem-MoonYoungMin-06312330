package ssemi.order.domain;

import java.util.Set;
import java.util.Map;

public final class Order {

    // 허용된 상태 전이 규칙
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
        OrderStatus.RESERVED,  Set.of(OrderStatus.CONFIRMED, OrderStatus.PRODUCING, OrderStatus.REJECTED),
        OrderStatus.PRODUCING, Set.of(OrderStatus.CONFIRMED),
        OrderStatus.CONFIRMED, Set.of(OrderStatus.RELEASE),
        OrderStatus.REJECTED,  Set.of(),
        OrderStatus.RELEASE,   Set.of()
    );

    private final String orderId;
    private final Sample sample;
    private final String customerName;
    private final int quantity;
    private OrderStatus status;

    public Order(String orderId, Sample sample, String customerName, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다.");
        if (customerName == null || customerName.isBlank()) throw new IllegalArgumentException("고객명은 필수입니다.");
        this.orderId = orderId;
        this.sample = sample;
        this.customerName = customerName;
        this.quantity = quantity;
        this.status = OrderStatus.RESERVED;
    }

    public void changeStatus(OrderStatus newStatus) {
        if (!ALLOWED_TRANSITIONS.get(this.status).contains(newStatus)) {
            throw new IllegalStateException(
                String.format("'%s' → '%s' 전이는 허용되지 않습니다.", this.status, newStatus));
        }
        this.status = newStatus;
    }

    public String getOrderId() { return orderId; }
    public Sample getSample() { return sample; }
    public String getCustomerName() { return customerName; }
    public int getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
}
