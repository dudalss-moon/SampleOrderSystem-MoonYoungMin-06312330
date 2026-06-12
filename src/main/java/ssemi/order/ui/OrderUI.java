package ssemi.order.ui;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.service.OrderService;

import java.util.List;

public final class OrderUI {

    private final OrderService orderService;
    private final InputHandler input;

    public OrderUI(OrderService orderService, InputHandler input) {
        this.orderService = orderService;
        this.input = input;
    }

    public void show() {
        while (true) {
            printMenu();
            int choice = input.readInt("선택 > ");
            switch (choice) {
                case 1 -> reserveOrder();
                case 2 -> listReservedOrders();
                case 0 -> { return; }
                default -> System.out.println("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    private void printMenu() {
        System.out.println("========================================");
        System.out.println("          주문 (접수/승인/거절)");
        System.out.println("========================================");
        System.out.println("  1. 주문 예약");
        System.out.println("  2. 접수된 주문 목록 (승인/거절)");
        System.out.println("  0. 메인 메뉴로");
        System.out.println("----------------------------------------");
    }

    private void reserveOrder() {
        System.out.println("[주문 예약]");
        String sampleId = input.readString("시료 ID   > ");
        String customerName = input.readString("고객명    > ");
        int quantity = input.readInt("주문 수량 > ");
        try {
            Order order = orderService.reserve(sampleId, customerName, quantity);
            System.out.printf("→ 주문이 접수되었습니다. [주문번호: %s | 상태: %s]%n",
                order.getOrderId(), order.getStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    void listReservedOrders() {
        List<Order> orders = orderService.findReserved();
        System.out.println("[접수된 주문 목록 - RESERVED]");
        System.out.println("------------------------------------------------------------");
        System.out.printf(" %-10s| %-11s| %-8s| %-5s| %s%n", "주문번호", "시료명", "고객명", "수량", "상태");
        System.out.println("------------------------------------------------------------");
        for (Order o : orders) {
            System.out.printf(" %-10s| %-11s| %-8s| %4d | %s%n",
                o.getOrderId(), o.getSample().getName(),
                o.getCustomerName(), o.getQuantity(), o.getStatus());
        }
        System.out.println("------------------------------------------------------------");
        System.out.printf("총 %d건%n", orders.size());
    }
}
