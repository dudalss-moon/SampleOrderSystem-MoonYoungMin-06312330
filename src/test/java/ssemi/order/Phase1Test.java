package ssemi.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.Sample;
import ssemi.order.ui.InputHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class Phase1Test {

    @Test
    @DisplayName("OrderStatus는 RESERVED, REJECTED, PRODUCING, CONFIRMED, RELEASE 5가지 값을 가진다")
    void orderStatus_유효값_5가지() {
        OrderStatus[] values = OrderStatus.values();

        assertEquals(5, values.length);
        assertEquals(OrderStatus.RESERVED, values[0]);
        assertEquals(OrderStatus.REJECTED, values[1]);
        assertEquals(OrderStatus.PRODUCING, values[2]);
        assertEquals(OrderStatus.CONFIRMED, values[3]);
        assertEquals(OrderStatus.RELEASE, values[4]);
    }

    @Test
    @DisplayName("Sample 생성 시 id, name, avgProductionTime, yield, stock 필드가 정상 저장된다")
    void sample_생성_시_필드값_정상저장() {
        Sample sample = new Sample("S001", "실리콘A", 30, 0.85, 100);

        assertAll(
            () -> assertEquals("S001", sample.getId()),
            () -> assertEquals("실리콘A", sample.getName()),
            () -> assertEquals(30, sample.getAvgProductionTime()),
            () -> assertEquals(0.85, sample.getYield()),
            () -> assertEquals(100, sample.getStock())
        );
    }

    @Test
    @DisplayName("Order 생성 시 초기 status는 RESERVED이다")
    void order_생성_초기상태_RESERVED() {
        Sample sample = new Sample("S001", "실리콘A", 30, 0.85, 100);

        Order order = new Order("O001", sample, "홍길동", 10);

        assertEquals(OrderStatus.RESERVED, order.getStatus());
    }

    @Test
    @DisplayName("ProductionJob targetQty는 ceil(부족분 / (수율 * 0.9)) 공식으로 계산된다")
    void productionJob_실생산량_계산_정확성() {
        // 주문수량=10, 재고=3 → 부족분=7, yield=0.8
        // targetQty = ceil(7 / (0.8 * 0.9)) = ceil(7 / 0.72) = ceil(9.722) = 10
        Sample sample = new Sample("S001", "실리콘A", 30, 0.8, 3);
        Order order = new Order("O001", sample, "홍길동", 10);
        int shortage = order.getQuantity() - sample.getStock();

        ProductionJob job = new ProductionJob("J001", order, shortage);

        assertEquals(10, job.getTargetQty());
    }

    @Test
    @DisplayName("ProductionJob totalTime은 avgProductionTime * targetQty로 계산된다")
    void productionJob_총생산시간_계산() {
        // avgProductionTime=30, targetQty=10 → totalTime=300
        Sample sample = new Sample("S001", "실리콘A", 30, 0.8, 3);
        Order order = new Order("O001", sample, "홍길동", 10);
        int shortage = order.getQuantity() - sample.getStock();

        ProductionJob job = new ProductionJob("J001", order, shortage);

        assertEquals(300, job.getTotalTime());
    }

    @Test
    @DisplayName("InputHandler readInt는 유효한 정수 입력을 올바르게 반환한다")
    void inputHandler_숫자_입력_정상처리() {
        ByteArrayInputStream in = new ByteArrayInputStream("3\n".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputHandler handler = new InputHandler(in, new PrintStream(out));

        int result = handler.readInt("선택 > ");

        assertEquals(3, result);
    }

    @Test
    @DisplayName("InputHandler readInt는 비정수 입력 후 재입력을 받아 올바른 값을 반환한다")
    void inputHandler_잘못된_입력_예외처리() {
        ByteArrayInputStream in = new ByteArrayInputStream("abc\n5\n".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputHandler handler = new InputHandler(in, new PrintStream(out));

        int result = handler.readInt("선택 > ");

        assertEquals(5, result);
    }
}
