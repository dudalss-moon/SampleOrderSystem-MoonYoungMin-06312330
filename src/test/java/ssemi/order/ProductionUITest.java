package ssemi.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.Sample;
import ssemi.order.repository.inmemory.InMemoryOrderRepository;
import ssemi.order.repository.inmemory.InMemoryProductionQueueRepository;
import ssemi.order.repository.inmemory.InMemorySampleRepository;
import ssemi.order.service.ProductionService;
import ssemi.order.ui.InputHandler;
import ssemi.order.ui.ProductionUI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ProductionUITest {

    private InMemorySampleRepository sampleRepo;
    private InMemoryOrderRepository orderRepo;
    private InMemoryProductionQueueRepository queueRepo;
    private ByteArrayOutputStream out;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        sampleRepo = new InMemorySampleRepository();
        orderRepo = new InMemoryOrderRepository();
        queueRepo = new InMemoryProductionQueueRepository();
        out = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(out));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private InputHandler inputOf(String... lines) {
        String joined = String.join(System.lineSeparator(), lines) + System.lineSeparator();
        ByteArrayInputStream in = new ByteArrayInputStream(joined.getBytes());
        return new InputHandler(in, new PrintStream(out));
    }

    private Order producingOrder(Sample sample, int qty) {
        String orderId = orderRepo.generateId();
        Order order = new Order(orderId, sample, "홍길동", qty);
        order.changeStatus(OrderStatus.PRODUCING);
        orderRepo.save(order);
        return order;
    }

    @Test
    @DisplayName("생산 큐가 비어있을 때 현재 작업 조회 시 '현재 생산 중인 작업이 없습니다' 메시지가 출력된다")
    void 현재_작업_없음() {
        ProductionService ps = new ProductionService(queueRepo, orderRepo, sampleRepo);
        InputHandler input = inputOf("1", "0");
        ProductionUI ui = new ProductionUI(ps, input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("현재 생산 중인 작업이 없습니다"), "출력: " + output);
    }

    @Test
    @DisplayName("생산 큐에 작업이 있을 때 현재 작업 조회 시 작업 ID와 시료명이 출력된다")
    void 현재_작업_있음() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 0);
        sampleRepo.save(sample);
        Order order = producingOrder(sample, 5);
        ProductionJob job = new ProductionJob("JOB-0001", order, 5);
        queueRepo.enqueue(job);

        ProductionService ps = new ProductionService(queueRepo, orderRepo, sampleRepo);
        InputHandler input = inputOf("1", "0");
        ProductionUI ui = new ProductionUI(ps, input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("JOB-0001"), "출력: " + output);
        assertTrue(output.contains("알파센서"), "출력: " + output);
    }

    @Test
    @DisplayName("생산 대기 목록이 비어있을 때 '대기 중인 작업이 없습니다' 메시지가 출력된다")
    void 대기_목록_빈_경우() {
        ProductionService ps = new ProductionService(queueRepo, orderRepo, sampleRepo);
        InputHandler input = inputOf("2", "0");
        ProductionUI ui = new ProductionUI(ps, input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("대기 중인 작업이 없습니다"), "출력: " + output);
    }

    @Test
    @DisplayName("생산 라인 메뉴에 '생산 진행' 항목이 표시되지 않는다")
    void 메뉴_생산진행_항목_없음() {
        ProductionService ps = new ProductionService(queueRepo, orderRepo, sampleRepo);
        InputHandler input = inputOf("0");
        ProductionUI ui = new ProductionUI(ps, input);
        ui.show();
        assertFalse(out.toString().contains("3. 생산 진행"));
    }

    @Test
    @DisplayName("메뉴 3 선택 시 올바른 메뉴 안내 후 종료된다")
    void 메뉴_3선택_잘못된_선택_안내() {
        ProductionService ps = new ProductionService(queueRepo, orderRepo, sampleRepo);
        InputHandler input = inputOf("3", "0");
        ProductionUI ui = new ProductionUI(ps, input);
        ui.show();
        assertTrue(out.toString().contains("올바른 메뉴를 선택해주세요."));
    }
}
