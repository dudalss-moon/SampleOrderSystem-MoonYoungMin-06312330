package ssemi.order.domain;

public final class Order {

    private final String orderId;
    private final Sample sample;
    private final String customerName;
    private final int quantity;
    private OrderStatus status;

    public Order(String orderId, Sample sample, String customerName, int quantity) {
        this.orderId = orderId;
        this.sample = sample;
        this.customerName = customerName;
        this.quantity = quantity;
        this.status = OrderStatus.RESERVED;
    }

    public String getOrderId() { return orderId; }
    public Sample getSample() { return sample; }
    public String getCustomerName() { return customerName; }
    public int getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
