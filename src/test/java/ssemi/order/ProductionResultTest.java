package ssemi.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Order;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.ProductionResult;
import ssemi.order.domain.Sample;

import static org.junit.jupiter.api.Assertions.*;

class ProductionResultTest {

    private Sample sampleWith(int stock) {
        return new Sample("S001", "알파센서", 30, 0.9, stock);
    }

    private Order orderWith(Sample sample, int quantity) {
        return new Order("ORD-0001", sample, "홍길동", quantity);
    }

    private ProductionJob jobWith(Order order, int shortage) {
        return new ProductionJob("JOB-0001", order, shortage);
    }

    @Test
    @DisplayName("isCompleted()가 true인 ProductionResult를 생성하면 true를 반환한다")
    void completed_true_반환() {
        Sample sample = sampleWith(0);
        Order order = orderWith(sample, 5);
        ProductionJob job = jobWith(order, 5);
        ProductionResult result = new ProductionResult(true, job);

        assertTrue(result.isCompleted());
    }

    @Test
    @DisplayName("getJob()은 생성 시 주입한 ProductionJob을 그대로 반환한다")
    void job_참조_반환() {
        Sample sample = sampleWith(0);
        Order order = orderWith(sample, 5);
        ProductionJob job = jobWith(order, 5);
        ProductionResult result = new ProductionResult(false, job);

        assertSame(job, result.getJob());
    }
}
