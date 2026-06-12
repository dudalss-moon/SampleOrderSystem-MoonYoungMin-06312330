package ssemi.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.Sample;

import static org.junit.jupiter.api.Assertions.*;

class ProductionJobTest {

    private ProductionJob createJob(int stock, int quantity, double yield) {
        Sample sample = new Sample("S001", "알파센서", 30, yield, stock);
        Order order = new Order("O001", sample, "홍길동", quantity);
        int shortage = quantity - stock;
        return new ProductionJob("JOB-0001", order, shortage);
    }

    @Test
    @DisplayName("produce(3) 호출 시 producedQty가 3 증가한다")
    void 생산량_누적() {
        ProductionJob job = createJob(3, 10, 0.8);

        job.produce(3);

        assertEquals(3, job.getProducedQty());
    }

    @Test
    @DisplayName("producedQty가 targetQty 이상이면 isCompleted()는 true를 반환한다")
    void 생산_완료_여부_확인() {
        ProductionJob job = createJob(3, 10, 0.8); // targetQty=10

        job.produce(9);
        assertFalse(job.isCompleted());

        job.produce(1); // 누적 10
        assertTrue(job.isCompleted());
    }
}
