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
}
