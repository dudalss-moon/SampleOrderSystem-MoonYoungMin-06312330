package ssemi.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Sample sampleFixture() {
        return new Sample("S001", "알파센서", 30, 0.9, 10);
    }

    @Test
    @DisplayName("Order 생성 시 초기 status는 RESERVED이다")
    void 주문_생성_초기상태_RESERVED() {
        Order order = new Order("O001", sampleFixture(), "홍길동", 5);

        assertEquals(OrderStatus.RESERVED, order.getStatus());
    }

    @Test
    @DisplayName("RESERVED 상태에서 CONFIRMED로 정상 전이된다")
    void 주문_상태_정상_전이() {
        Order order = new Order("O001", sampleFixture(), "홍길동", 5);

        order.changeStatus(OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("RELEASE 상태에서 RESERVED로 전이 시 IllegalStateException이 발생한다")
    void 주문_상태_비허용_전이_예외() {
        Order order = new Order("O001", sampleFixture(), "홍길동", 5);
        order.changeStatus(OrderStatus.CONFIRMED);
        order.changeStatus(OrderStatus.RELEASE);

        assertThrows(IllegalStateException.class,
            () -> order.changeStatus(OrderStatus.RESERVED));
    }

    @Test
    @DisplayName("주문 수량이 0 이하이면 IllegalArgumentException이 발생한다")
    void 주문_수량_0이하_예외() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order("O001", sampleFixture(), "홍길동", 0));
    }

    @Test
    @DisplayName("고객명이 공백이면 IllegalArgumentException이 발생한다")
    void 고객명_공백_예외() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order("O001", sampleFixture(), "  ", 5));
    }
}
