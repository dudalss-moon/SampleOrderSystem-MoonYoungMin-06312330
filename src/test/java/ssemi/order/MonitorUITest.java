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
import ssemi.order.service.MonitorService;
import ssemi.order.ui.InputHandler;
import ssemi.order.ui.MonitorUI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MonitorUITest {

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

    private MonitorUI uiWith(InputHandler input) {
        return new MonitorUI(new MonitorService(orderRepo, sampleRepo), input);
    }

    @Test
    @DisplayName("주문이 없을 때 주문 현황 조회 시 각 상태에 '(0건)'이 포함 출력된다")
    void 주문_현황_빈_경우() {
        InputHandler input = inputOf("1", "0");
        MonitorUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("(0건)"), "출력: " + output);
    }

    @Test
    @DisplayName("RESERVED·CONFIRMED 주문 각 1건이 있을 때 상태별 그룹으로 출력된다")
    void 주문_현황_상태별_그룹() {
        Sample sample = new Sample("S001", "알파", 30, 0.9, 10);
        sampleRepo.save(sample);
        Order order1 = new Order(orderRepo.generateId(), sample, "고객1", 2);
        orderRepo.save(order1);
        Order order2 = new Order(orderRepo.generateId(), sample, "고객2", 3);
        order2.changeStatus(OrderStatus.CONFIRMED);
        orderRepo.save(order2);

        InputHandler input = inputOf("1", "0");
        MonitorUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("RESERVED"), "출력: " + output);
        assertTrue(output.contains("CONFIRMED"), "출력: " + output);
    }

    @Test
    @DisplayName("재고가 충분한 시료의 재고 현황에 '[여유]'가 출력된다")
    void 재고_현황_레이블_여유() {
        sampleRepo.save(new Sample("S001", "알파", 30, 0.9, 100));

        InputHandler input = inputOf("2", "0");
        MonitorUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("[여유]"), "출력: " + output);
    }

    @Test
    @DisplayName("재고가 0인 시료의 재고 현황에 '[고갈]'이 출력된다")
    void 재고_현황_레이블_고갈() {
        sampleRepo.save(new Sample("S001", "알파", 30, 0.9, 0));

        InputHandler input = inputOf("2", "0");
        MonitorUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("[고갈]"), "출력: " + output);
    }
}
