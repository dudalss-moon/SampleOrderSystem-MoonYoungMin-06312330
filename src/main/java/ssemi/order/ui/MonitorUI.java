package ssemi.order.ui;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.SampleStockInfo;
import ssemi.order.domain.StockStatus;
import ssemi.order.service.MonitorService;

import java.util.List;
import java.util.Map;

public final class MonitorUI {

    private final MonitorService monitorService;
    private final InputHandler input;

    public MonitorUI(MonitorService monitorService, InputHandler input) {
        this.monitorService = monitorService;
        this.input = input;
    }

    public void show() {
        while (true) {
            printMenu();
            int choice = input.readInt("선택 > ");
            switch (choice) {
                case 1 -> displayOrderMonitor();
                case 2 -> displayStockMonitor();
                case 0 -> { return; }
                default -> System.out.println("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    private void printMenu() {
        System.out.println("========================================");
        System.out.println("              모니터링");
        System.out.println("========================================");
        System.out.println("  1. 주문 현황");
        System.out.println("  2. 재고 현황");
        System.out.println("  0. 메인 메뉴로");
        System.out.println("----------------------------------------");
    }

    void displayOrderMonitor() {
        Map<OrderStatus, List<Order>> grouped = monitorService.getOrdersByStatus();
        System.out.println("[주문 현황]");
        System.out.println("============================================================");
        printOrderSection("RESERVED - 접수 대기",  grouped.get(OrderStatus.RESERVED));
        printOrderSection("PRODUCING - 생산 중",   grouped.get(OrderStatus.PRODUCING));
        printOrderSection("CONFIRMED - 출고 대기", grouped.get(OrderStatus.CONFIRMED));
        printOrderSection("RELEASE - 출고 완료",   grouped.get(OrderStatus.RELEASE));
        System.out.println("============================================================");
    }

    private void printOrderSection(String title, List<Order> orders) {
        System.out.printf("[%s]%n", title);
        if (orders.isEmpty()) {
            System.out.println(" (0건)");
        } else {
            System.out.printf(" %-10s| %-11s| %-8s| %s%n", "주문번호", "시료명", "고객명", "수량");
            for (Order o : orders) {
                System.out.printf(" %-10s| %-11s| %-8s| %4d%n",
                    o.getOrderId(), o.getSample().getName(), o.getCustomerName(), o.getQuantity());
            }
            System.out.printf(" (%d건)%n", orders.size());
        }
        System.out.println();
    }

    void displayStockMonitor() {
        List<SampleStockInfo> infos = monitorService.getStockInfos();
        System.out.println("[재고 현황]");
        System.out.println("------------------------------------------------------------");
        System.out.printf(" %-11s| %-9s| %-10s| %s%n", "시료명", "현재재고", "대기주문량", "상태");
        System.out.println("------------------------------------------------------------");
        for (SampleStockInfo info : infos) {
            System.out.printf(" %-11s| %6d개  | %7d개  | %s%n",
                info.getSample().getName(),
                info.getSample().getStock(),
                info.getPendingQuantity(),
                statusLabel(info.getStockStatus()));
        }
        System.out.println("------------------------------------------------------------");
    }

    private String statusLabel(StockStatus status) {
        return switch (status) {
            case DEPLETED -> "[고갈]";
            case SHORTAGE -> "[부족]";
            case PLENTY   -> "[여유]";
        };
    }
}
