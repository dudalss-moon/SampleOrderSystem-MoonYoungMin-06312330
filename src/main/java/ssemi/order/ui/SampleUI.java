package ssemi.order.ui;

import ssemi.order.domain.Sample;
import ssemi.order.service.SampleService;

import java.util.List;

public final class SampleUI {

    private final SampleService service;
    private final InputHandler input;

    public SampleUI(SampleService service, InputHandler input) {
        this.service = service;
        this.input = input;
    }

    public void show() {
        while (true) {
            printMenu();
            int choice = input.readInt("선택 > ");
            switch (choice) {
                case 1 -> registerSample();
                case 2 -> listSamples();
                case 3 -> searchSample();
                case 0 -> { return; }
                default -> System.out.println("올바른 메뉴를 선택해주세요.");
            }
        }
    }

    private void printMenu() {
        System.out.println("========================================");
        System.out.println("              시료 관리");
        System.out.println("========================================");
        System.out.println("  1. 시료 등록");
        System.out.println("  2. 시료 목록 조회");
        System.out.println("  3. 시료 검색");
        System.out.println("  0. 메인 메뉴로");
        System.out.println("----------------------------------------");
    }

    private void registerSample() {
        System.out.println("[시료 등록]");
        String id = input.readString("시료 ID       > ");
        String name = input.readString("시료 이름     > ");
        int avgTime = input.readInt("평균생산시간(분) > ");
        double yield = input.readDouble("수율 (0~1.0)  > ");
        try {
            Sample sample = service.register(id, name, avgTime, yield);
            System.out.printf("→ 시료 '%s - %s'이 등록되었습니다.%n", sample.getId(), sample.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    private void listSamples() {
        List<Sample> samples = service.findAll();
        System.out.println("[시료 목록]");
        System.out.println("------------------------------------------------------------");
        System.out.printf(" %-6s| %-13s| %-13s| %-6s| %s%n", "ID", "이름", "생산시간(분)", "수율", "재고");
        System.out.println("------------------------------------------------------------");
        for (Sample s : samples) {
            System.out.printf(" %-6s| %-13s| %11d  | %4.2f | %4d개%n",
                s.getId(), s.getName(), s.getAvgProductionTime(), s.getYield(), s.getStock());
        }
        System.out.println("------------------------------------------------------------");
        System.out.printf("총 %d개 등록됨%n", samples.size());
    }

    private void searchSample() {
        System.out.println("[시료 검색]");
        String keyword = input.readString("검색어 > ");
        List<Sample> result = service.search(keyword);
        System.out.println("------------------------------------------------------------");
        System.out.printf(" %-6s| %-13s| %-13s| %-6s| %s%n", "ID", "이름", "생산시간(분)", "수율", "재고");
        System.out.println("------------------------------------------------------------");
        for (Sample s : result) {
            System.out.printf(" %-6s| %-13s| %11d  | %4.2f | %4d개%n",
                s.getId(), s.getName(), s.getAvgProductionTime(), s.getYield(), s.getStock());
        }
        System.out.println("------------------------------------------------------------");
        if (result.isEmpty()) {
            System.out.println("검색 결과가 없습니다.");
        } else {
            System.out.printf("%d개 검색됨%n", result.size());
        }
    }
}
