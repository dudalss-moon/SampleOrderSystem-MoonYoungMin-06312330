package ssemi.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.Sample;
import ssemi.order.repository.inmemory.InMemoryOrderRepository;
import ssemi.order.repository.inmemory.InMemoryProductionQueueRepository;
import ssemi.order.repository.inmemory.InMemorySampleRepository;
import ssemi.order.service.OrderService;
import ssemi.order.service.ProductionService;
import ssemi.order.ui.InputHandler;
import ssemi.order.ui.OrderUI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class OrderUITest {

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

    private ProductionService newProductionService() {
        return new ProductionService(queueRepo, orderRepo, sampleRepo);
    }

    private OrderService newOrderService(ProductionService ps) {
        return new OrderService(orderRepo, sampleRepo, ps);
    }

    @Test
    @DisplayName("유효한 시료 ID로 주문 예약 성공 시 '주문이 접수되었습니다' 메시지가 출력된다")
    void 주문_예약_성공() {
        sampleRepo.save(new Sample("S01", "알파", 30, 0.9, 10));
        ProductionService ps = newProductionService();
        OrderService os = newOrderService(ps);
        InputHandler input = inputOf("1", "S01", "Hong", "2", "0");
        OrderUI ui = new OrderUI(os, ps, input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("주문이 접수되었습니다"), "출력: " + output);
    }

    @Test
    @DisplayName("존재하지 않는 시료 ID로 주문 예약 시 '오류:' 메시지가 출력된다")
    void 주문_예약_없는_시료_오류() {
        ProductionService ps = newProductionService();
        OrderService os = newOrderService(ps);
        InputHandler input = inputOf("1", "INVALID", "Hong", "2", "0");
        OrderUI ui = new OrderUI(os, ps, input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("오류:"), "출력: " + output);
    }

    @Test
    @DisplayName("접수된 주문이 없을 때 '접수된 주문이 없습니다.' 메시지가 출력된다")
    void 접수_주문_없음() {
        ProductionService ps = newProductionService();
        OrderService os = newOrderService(ps);
        InputHandler input = inputOf("2", "0");
        OrderUI ui = new OrderUI(os, ps, input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("접수된 주문이 없습니다."), "출력: " + output);
    }

    @Test
    @DisplayName("재고가 충분한 주문 승인 시 '즉시 출고 대기' 메시지가 출력된다")
    void 주문_승인_재고_충분() {
        sampleRepo.save(new Sample("S01", "알파", 30, 0.9, 10));
        ProductionService ps = newProductionService();
        OrderService os = newOrderService(ps);
        Order reserved = os.reserve("S01", "홍길동", 2);
        String orderId = reserved.getOrderId();

        InputHandler input = inputOf("2", orderId, "1", "0");
        OrderUI ui = new OrderUI(os, ps, input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("즉시 출고 대기"), "출력: " + output);
    }

    @Test
    @DisplayName("재고가 부족한 주문 승인 시 '생산라인에 등록' 메시지가 출력된다")
    void 주문_승인_재고_부족() {
        sampleRepo.save(new Sample("S01", "알파", 30, 0.9, 0));
        ProductionService ps = newProductionService();
        OrderService os = newOrderService(ps);
        Order reserved = os.reserve("S01", "홍길동", 5);
        String orderId = reserved.getOrderId();

        InputHandler input = inputOf("2", orderId, "1", "0");
        OrderUI ui = new OrderUI(os, ps, input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("생산라인에 등록"), "출력: " + output);
    }

    @Test
    @DisplayName("주문 거절 시 '거절되었습니다' 메시지가 출력된다")
    void 주문_거절() {
        sampleRepo.save(new Sample("S01", "알파", 30, 0.9, 0));
        ProductionService ps = newProductionService();
        OrderService os = newOrderService(ps);
        Order reserved = os.reserve("S01", "홍길동", 5);
        String orderId = reserved.getOrderId();

        InputHandler input = inputOf("2", orderId, "2", "0");
        OrderUI ui = new OrderUI(os, ps, input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("거절되었습니다"), "출력: " + output);
    }

    @Test
    @DisplayName("존재하지 않는 주문번호 입력 시 '오류:' 메시지가 출력된다")
    void 잘못된_주문번호_입력() {
        sampleRepo.save(new Sample("S01", "알파", 30, 0.9, 10));
        ProductionService ps = newProductionService();
        OrderService os = newOrderService(ps);
        os.reserve("S01", "홍길동", 2);

        InputHandler input = inputOf("2", "NONE-9999", "0");
        OrderUI ui = new OrderUI(os, ps, input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("오류:"), "출력: " + output);
    }
}
