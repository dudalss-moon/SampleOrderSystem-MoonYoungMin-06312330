package ssemi.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.Sample;

import java.time.Instant;

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

    @Test
    @DisplayName("새로 생성된 ProductionJob의 startTime은 null이다")
    void startTime_기본값_null() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 0);
        Order order = new Order("ORD-0001", sample, "홍길동", 5);
        ProductionJob job = new ProductionJob("JOB-0001", order, 5);
        assertNull(job.getStartTime());
    }

    @Test
    @DisplayName("setStartTime 호출 후 getStartTime이 동일 Instant를 반환한다")
    void startTime_설정_후_조회() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 0);
        Order order = new Order("ORD-0001", sample, "홍길동", 5);
        ProductionJob job = new ProductionJob("JOB-0001", order, 5);
        Instant now = Instant.now();
        job.setStartTime(now);
        assertEquals(now, job.getStartTime());
    }

    @Test
    @DisplayName("avgTime=30, 경과60초 → 생산량 2개")
    void 경과시간_기반_생산량_계산() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 0);
        Order order = new Order("ORD-0001", sample, "홍길동", 10);
        ProductionJob job = new ProductionJob("JOB-0001", order, 10);
        assertEquals(2, job.calcProducedByElapsed(60));
    }

    @Test
    @DisplayName("avgTime=30, 경과10초 → 생산량 0개")
    void 경과시간_부족_생산량_0() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 0);
        Order order = new Order("ORD-0001", sample, "홍길동", 10);
        ProductionJob job = new ProductionJob("JOB-0001", order, 10);
        assertEquals(0, job.calcProducedByElapsed(10));
    }

    @Test
    @DisplayName("충분한 경과 시간이어도 targetQty를 초과하지 않는다")
    void 경과시간_초과_시_targetQty로_보정() {
        Sample sample = new Sample("S001", "알파센서", 1, 0.9, 0);
        Order order = new Order("ORD-0001", sample, "홍길동", 4);
        ProductionJob job = new ProductionJob("JOB-0001", order, 4);
        assertEquals(job.getTargetQty(), job.calcProducedByElapsed(9999));
    }
}
