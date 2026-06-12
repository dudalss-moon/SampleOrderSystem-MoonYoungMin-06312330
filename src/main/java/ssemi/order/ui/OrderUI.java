package ssemi.order.ui;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.ProductionJob;
import ssemi.order.service.OrderService;
import ssemi.order.service.ProductionService;

import java.util.List;

public final class OrderUI {

    private final OrderService orderService;
    private final ProductionService productionService;
    private final InputHandler input;

    public OrderUI(OrderService orderService, ProductionService productionService, InputHandler input) {
        this.orderService = orderService;
        this.productionService = productionService;
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
        if (orders.isEmpty()) {
            System.out.println("접수된 주문이 없습니다.");
            return;
        }
        printReservedTable(orders);

        String orderId = input.readString("처리할 주문번호 > ");
        Order order;
        try {
            order = orderService.findById(orderId);
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
            return;
        }
        if (order.getStatus() != OrderStatus.RESERVED) {
            System.out.println("오류: RESERVED 상태의 주문만 처리할 수 있습니다.");
            return;
        }

        System.out.println("  1. 승인");
        System.out.println("  2. 거절");
        System.out.println("  0. 취소");
        int choice = input.readInt("선택 > ");
        switch (choice) {
            case 1 -> processApprove(orderId, order);
            case 2 -> processReject(orderId);
            case 0 -> System.out.println("취소되었습니다.");
            default -> System.out.println("올바른 메뉴를 선택해주세요.");
        }
    }

    private void printReservedTable(List<Order> orders) {
        System.out.println("[접수된 주문 목록 - RESERVED]");
        System.out.println("------------------------------------------------------------");
        System.out.printf(" %-10s| %-11s| %-8s| %-5s| %s%n", "주문번호", "시료명", "고객명", "수량", "재고 현황");
        System.out.println("------------------------------------------------------------");
        for (Order o : orders) {
            int stock = o.getSample().getStock();
            String stockStatus = o.getSample().hasEnoughStock(o.getQuantity())
                ? String.format("재고:%d개 (여유)", stock)
                : String.format("재고:%d개 (부족)", stock);
            System.out.printf(" %-10s| %-11s| %-8s| %4d | %s%n",
                o.getOrderId(), o.getSample().getName(),
                o.getCustomerName(), o.getQuantity(), stockStatus);
        }
        System.out.println("------------------------------------------------------------");
    }

    private void processApprove(String orderId, Order order) {
        try {
            boolean hadEnoughStock = order.getSample().hasEnoughStock(order.getQuantity());
            orderService.approve(orderId);
            if (hadEnoughStock) {
                System.out.printf("→ [재고 충분] 즉시 출고 대기 상태로 전환되었습니다.%n");
                System.out.printf("   주문번호: %s | 상태: %s%n", orderId, OrderStatus.CONFIRMED);
            } else {
                ProductionJob job = productionService.getQueueList().get(productionService.getQueueList().size() - 1);
                System.out.printf("→ [재고 부족] 생산라인에 등록되었습니다.%n");
                System.out.printf("   주문번호: %s | 상태: %s%n", orderId, OrderStatus.PRODUCING);
                System.out.printf("   실 생산량: %d개 | 예상 생산시간: %d분%n", job.getTargetQty(), job.getTotalTime());
            }
        } catch (IllegalStateException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    private void processReject(String orderId) {
        try {
            orderService.reject(orderId);
            System.out.printf("→ 주문이 거절되었습니다.%n");
            System.out.printf("   주문번호: %s | 상태: %s%n", orderId, OrderStatus.REJECTED);
        } catch (IllegalStateException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }
}
