package ssemi.order.ui;

import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.ProductionResult;
import ssemi.order.service.ProductionService;

import java.util.List;
import java.util.Optional;

public final class ProductionUI {

    private final ProductionService productionService;
    private final InputHandler input;

    public ProductionUI(ProductionService productionService, InputHandler input) {
        this.productionService = productionService;
        this.input = input;
    }

    public void show() {
        while (true) {
            printMenu();
            int choice = input.readInt("선택 > ");
            switch (choice) {
                case 1 -> displayCurrentJob();
                case 2 -> displayQueue();
                case 3 -> processProduction();
                case 0 -> { return; }
                default -> System.out.println("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    private void printMenu() {
        System.out.println("========================================");
        System.out.println("              생산 라인");
        System.out.println("========================================");
        System.out.println("  1. 현재 생산 현황");
        System.out.println("  2. 생산 대기 목록");
        System.out.println("  3. 생산 진행");
        System.out.println("  0. 메인 메뉴로");
        System.out.println("----------------------------------------");
    }

    void displayCurrentJob() {
        Optional<ProductionJob> current = productionService.getCurrentJob();
        if (current.isEmpty()) {
            System.out.println("[현재 생산 중인 작업이 없습니다.]");
            return;
        }
        ProductionJob job = current.get();
        int waitingCount = Math.max(0, productionService.getQueueList().size() - 1);
        System.out.println("[현재 생산 중]");
        System.out.println("------------------------------------------------------------");
        System.out.printf(" 작업 ID  : %s%n", job.getJobId());
        System.out.printf(" 주문번호  : %s%n", job.getOrder().getOrderId());
        System.out.printf(" 시료명    : %s%n", job.getOrder().getSample().getName());
        System.out.printf(" 고객명    : %s%n", job.getOrder().getCustomerName());
        System.out.printf(" 주문 수량 : %d개%n", job.getOrder().getQuantity());
        System.out.printf(" 실 생산량 : %d개 (목표)%n", job.getTargetQty());
        System.out.printf(" 현재 생산 : %d개%n", job.getProducedQty());
        System.out.printf(" 예상 시간 : %d분%n", job.getTotalTime());
        System.out.println("------------------------------------------------------------");
        System.out.printf("※ 생산 대기 중인 작업: %d건%n", waitingCount);
    }

    void displayQueue() {
        List<ProductionJob> queue = productionService.getQueueList();
        System.out.println("[생산 대기 목록]");
        System.out.println("------------------------------------------------------------");
        if (queue.isEmpty()) {
            System.out.println(" (대기 중인 작업이 없습니다.)");
            System.out.println("------------------------------------------------------------");
            return;
        }
        System.out.printf(" %-4s| %-9s| %-9s| %-10s| %s%n", "순서", "작업ID", "주문번호", "시료명", "생산량");
        System.out.println("------------------------------------------------------------");
        for (int i = 0; i < queue.size(); i++) {
            ProductionJob job = queue.get(i);
            System.out.printf(" %-4d| %-9s| %-9s| %-10s| %3d개%n",
                i + 1,
                job.getJobId(),
                job.getOrder().getOrderId(),
                job.getOrder().getSample().getName(),
                job.getTargetQty());
        }
        System.out.println("------------------------------------------------------------");
        System.out.printf("총 %d건 대기 중%n", queue.size());
    }

    void processProduction() {
        Optional<ProductionJob> current = productionService.getCurrentJob();
        if (current.isEmpty()) {
            System.out.println("[현재 생산 중인 작업이 없습니다.]");
            return;
        }
        ProductionJob job = current.get();
        System.out.println("[생산 진행]");
        System.out.printf("현재 작업: %s (%s %d개 생산)%n",
            job.getJobId(), job.getOrder().getSample().getName(), job.getTargetQty());

        int qty = input.readInt("생산 수량 입력 > ");
        ProductionResult result = productionService.processProduction(qty);
        ProductionJob finished = result.getJob();

        if (result.isCompleted()) {
            System.out.printf("→ 생산 완료! %s %d개가 재고에 추가되었습니다.%n",
                finished.getOrder().getSample().getName(), finished.getTargetQty());
            System.out.printf("→ 주문 %s 상태가 CONFIRMED로 변경되었습니다.%n",
                finished.getOrder().getOrderId());
            productionService.getCurrentJob().ifPresent(next ->
                System.out.printf("→ 다음 작업 %s 생산을 시작합니다.%n", next.getJobId()));
        } else {
            System.out.printf("→ 생산 진행 중: %d / %d개 완료%n",
                finished.getProducedQty(), finished.getTargetQty());
        }
    }
}
