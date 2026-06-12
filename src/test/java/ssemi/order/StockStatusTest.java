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
}
