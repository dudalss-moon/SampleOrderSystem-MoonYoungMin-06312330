package ssemi.order.ui;

import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.ProductionQueueRepository;
import ssemi.order.repository.SampleRepository;
import ssemi.order.service.OrderService;
import ssemi.order.service.ProductionService;
import ssemi.order.service.SampleService;

public final class ConsoleMenu {

    private final InputHandler input;
    private final SampleRepository sampleRepository;
    private final SampleUI sampleUI;
    private final OrderUI orderUI;

    public ConsoleMenu(InputHandler input, SampleRepository sampleRepository, OrderRepository orderRepository) {
        this.input = input;
        this.sampleRepository = sampleRepository;
        this.sampleUI = new SampleUI(new SampleService(sampleRepository), input);
        ProductionService productionService = new ProductionService(new ProductionQueueRepository());
        this.orderUI = new OrderUI(new OrderService(orderRepository, sampleRepository, productionService), input);
    }

    public void run() {
        while (true) {
            displaySummary();
            displayMainMenu();
            int choice = readChoice();
            if (choice == 0) {
                System.out.println("시스템을 종료합니다.");
                break;
            }
            route(choice);
        }
    }

    public void displaySummary() {
        int sampleCount = sampleRepository.count();
        int totalStock = sampleRepository.totalStock();
        System.out.println("[시료 요약]");
        System.out.printf(" 등록 시료 수: %d개 | 전체 재고: %d개%n", sampleCount, totalStock);
    }

    public void displayMainMenu() {
        System.out.println("========================================");
        System.out.println("   S-Semi 반도체 시료생산 주문관리 시스템");
        System.out.println("========================================");
        System.out.println("[메뉴 선택]");
        System.out.println("  1. 시료 관리");
        System.out.println("  2. 주문 (접수/승인/거절)");
        System.out.println("  3. 모니터링");
        System.out.println("  4. 출고 처리");
        System.out.println("  5. 생산 라인");
        System.out.println("  0. 종료");
        System.out.println("----------------------------------------");
    }

    int readChoice() {
        return input.readInt("선택 > ");
    }

    private void route(int choice) {
        switch (choice) {
            case 1 -> sampleUI.show();
            case 2 -> orderUI.show();
            case 3 -> System.out.println("[모니터링] - 준비 중");
            case 4 -> System.out.println("[출고 처리] - 준비 중");
            case 5 -> System.out.println("[생산 라인] - 준비 중");
            default -> System.out.println("올바른 메뉴를 선택해주세요.");
        }
    }
}
