package ssemi.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.inmemory.InMemoryOrderRepository;
import ssemi.order.repository.inmemory.InMemorySampleRepository;
import ssemi.order.service.ReleaseService;
import ssemi.order.ui.InputHandler;
import ssemi.order.ui.ReleaseUI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseUITest {

    private InMemorySampleRepository sampleRepo;
    private InMemoryOrderRepository orderRepo;
    private ByteArrayOutputStream out;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        sampleRepo = new InMemorySampleRepository();
        orderRepo = new InMemoryOrderRepository();
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

    private ReleaseUI uiWith(InputHandler input) {
        return new ReleaseUI(new ReleaseService(orderRepo, sampleRepo), input);
    }

    private Order confirmedOrder(Sample sample, int qty, boolean stockDeducted) {
        String orderId = orderRepo.generateId();
        Order order = new Order(orderId, sample, "홍길동", qty);
        order.changeStatus(OrderStatus.CONFIRMED);
        if (stockDeducted) {
            order.markStockDeducted();
        }
        orderRepo.save(order);
        return order;
    }

    @Test
    @DisplayName("출고 대기 주문이 없을 때 '출고 대기 중인 주문이 없습니다' 메시지가 출력된다")
    void 출고_대기_없음() {
        InputHandler input = inputOf("1", "0");
        ReleaseUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("출고 대기 중인 주문이 없습니다"), "출력: " + output);
    }

    @Test
    @DisplayName("CONFIRMED 주문 출고 처리 성공 시 '출고 완료'와 'RELEASE' 메시지가 출력된다")
    void 출고_처리_성공() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 10);
        sampleRepo.save(sample);
        Order order = confirmedOrder(sample, 3, true);
        String orderId = order.getOrderId();

        InputHandler input = inputOf("1", orderId, "0");
        ReleaseUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("출고 완료"), "출력: " + output);
        assertTrue(output.contains("RELEASE"), "출력: " + output);
    }

    @Test
    @DisplayName("존재하지 않는 주문번호로 출고 시도 시 '오류:' 메시지가 출력된다")
    void 잘못된_주문번호() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 10);
        sampleRepo.save(sample);
        confirmedOrder(sample, 3, true);

        InputHandler input = inputOf("1", "NONE-9999", "0");
        ReleaseUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("오류:"), "출력: " + output);
    }

    @Test
    @DisplayName("CONFIRMED 주문 2건 등록 후 출고 대기 목록 조회 시 주문번호가 포함 출력된다")
    void 출고_대기_목록_출력() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 10);
        sampleRepo.save(sample);
        Order order1 = confirmedOrder(sample, 2, true);
        confirmedOrder(sample, 3, true);

        InputHandler input = inputOf("1", order1.getOrderId(), "0");
        ReleaseUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains(order1.getOrderId()), "출력: " + output);
    }
}
