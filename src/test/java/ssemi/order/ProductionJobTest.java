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
    @DisplayName("restore()로 생성한 ProductionJob은 주입된 targetQty/producedQty/totalTime을 그대로 반환한다")
    void restore_정적팩토리_DB_재구성() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 2);
        Order order = new Order("O001", sample, "홍길동", 5);

        ProductionJob job = ProductionJob.restore("JOB-0001", order, 5, 3, 150);

        assertAll(
            () -> assertEquals("JOB-0001", job.getJobId()),
            () -> assertEquals(5,           job.getTargetQty()),
            () -> assertEquals(3,           job.getProducedQty()),
            () -> assertEquals(150,         job.getTotalTime())
        );
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
