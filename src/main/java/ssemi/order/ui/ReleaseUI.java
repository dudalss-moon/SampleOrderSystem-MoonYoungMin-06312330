package ssemi.order.ui;

import ssemi.order.domain.Order;
import ssemi.order.service.ReleaseService;

import java.util.List;

public final class ReleaseUI {

    private final ReleaseService releaseService;
    private final InputHandler input;

    public ReleaseUI(ReleaseService releaseService, InputHandler input) {
        this.releaseService = releaseService;
        this.input = input;
    }

    public void show() {
        while (true) {
            printMenu();
            int choice = input.readInt("선택 > ");
            switch (choice) {
                case 1 -> executeRelease();
                case 0 -> { return; }
                default -> System.out.println("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    private void printMenu() {
        System.out.println("========================================");
        System.out.println("              출고 처리");
        System.out.println("========================================");
        System.out.println("  1. 출고 처리");
        System.out.println("  0. 메인 메뉴로");
        System.out.println("----------------------------------------");
    }

    void executeRelease() {
        List<Order> confirmed = releaseService.findConfirmed();
        if (confirmed.isEmpty()) {
            System.out.println("[출고 대기 중인 주문이 없습니다.]");
            return;
        }
        displayConfirmedOrders(confirmed);

        String orderId = input.readString("출고할 주문번호 > ");
        try {
            Order order = releaseService.release(orderId);
            System.out.printf("→ 출고 완료! [주문번호: %s | 상태: %s]%n",
                order.getOrderId(), order.getStatus());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    private void displayConfirmedOrders(List<Order> orders) {
        System.out.println("[CONFIRMED - 출고 대기 목록]");
        System.out.println("------------------------------------------------------------");
        System.out.printf(" %-10s| %-10s| %-8s| %s%n", "주문번호", "시료명", "고객명", "수량");
        System.out.println("------------------------------------------------------------");
        for (Order o : orders) {
            System.out.printf(" %-10s| %-10s| %-8s| %4d%n",
                o.getOrderId(), o.getSample().getName(),
                o.getCustomerName(), o.getQuantity());
        }
        System.out.println("------------------------------------------------------------");
        System.out.printf("총 %d건%n", orders.size());
    }
}
