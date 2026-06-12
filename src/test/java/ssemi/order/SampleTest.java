package ssemi.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Sample;

import static org.junit.jupiter.api.Assertions.*;

class SampleTest {

    @Test
    @DisplayName("유효한 값으로 Sample을 생성하면 모든 필드가 정상 저장된다")
    void 시료_생성_정상() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 0);

        assertAll(
            () -> assertEquals("S001", sample.getId()),
            () -> assertEquals("알파센서", sample.getName()),
            () -> assertEquals(30, sample.getAvgProductionTime()),
            () -> assertEquals(0.9, sample.getYield()),
            () -> assertEquals(0, sample.getStock())
        );
    }

    @Test
    @DisplayName("수율이 0 이하이면 IllegalArgumentException이 발생한다")
    void 수율_0이하_예외() {
        assertThrows(IllegalArgumentException.class,
            () -> new Sample("S001", "알파센서", 30, 0.0, 0));
    }

    @Test
    @DisplayName("수율이 1.0 초과이면 IllegalArgumentException이 발생한다")
    void 수율_1초과_예외() {
        assertThrows(IllegalArgumentException.class,
            () -> new Sample("S001", "알파센서", 30, 1.1, 0));
    }

    @Test
    @DisplayName("평균생산시간이 0 이하이면 IllegalArgumentException이 발생한다")
    void 평균생산시간_0이하_예외() {
        assertThrows(IllegalArgumentException.class,
            () -> new Sample("S001", "알파센서", 0, 0.9, 0));
    }

    @Test
    @DisplayName("addStock(10) 호출 시 재고가 10 증가한다")
    void 재고_추가_정상() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 0);

        sample.addStock(10);

        assertEquals(10, sample.getStock());
    }

    @Test
    @DisplayName("deductStock(3) 호출 시 재고가 3 감소한다")
    void 재고_차감_정상() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 10);

        sample.deductStock(3);

        assertEquals(7, sample.getStock());
    }

    @Test
    @DisplayName("재고보다 많은 수량을 차감하면 IllegalStateException이 발생한다")
    void 재고_차감_부족_예외() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 5);

        assertThrows(IllegalStateException.class,
            () -> sample.deductStock(10));
    }

    @Test
    @DisplayName("hasEnoughStock은 재고 충분 시 true, 부족 시 false를 반환한다")
    void 재고_충분_여부_확인() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 5);

        assertAll(
            () -> assertTrue(sample.hasEnoughStock(5)),
            () -> assertFalse(sample.hasEnoughStock(6))
        );
    }
}
