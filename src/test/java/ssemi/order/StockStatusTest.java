package ssemi.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.StockStatus;

import static org.junit.jupiter.api.Assertions.*;

class StockStatusTest {

    @Test
    @DisplayName("stock이 0이면 StockStatus는 DEPLETED다")
    void 재고0_고갈() {
        assertEquals(StockStatus.DEPLETED, StockStatus.of(0, 0));
    }

    @Test
    @DisplayName("stock이 pendingQty보다 적으면 StockStatus는 SHORTAGE다")
    void 재고부족_부족() {
        assertEquals(StockStatus.SHORTAGE, StockStatus.of(2, 5));
    }

    @Test
    @DisplayName("stock이 pendingQty보다 많으면 StockStatus는 PLENTY다")
    void 재고충분_여유() {
        assertEquals(StockStatus.PLENTY, StockStatus.of(10, 3));
    }

    @Test
    @DisplayName("stock이 pendingQty와 같으면 StockStatus는 PLENTY다")
    void 재고_대기량_동일_여유() {
        assertEquals(StockStatus.PLENTY, StockStatus.of(5, 5));
    }
}
