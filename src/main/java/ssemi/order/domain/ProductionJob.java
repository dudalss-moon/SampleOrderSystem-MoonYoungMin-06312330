package ssemi.order.domain;

import java.time.Instant;

public final class ProductionJob {

    private final String jobId;
    private final Order order;
    private final int targetQty;
    private int producedQty;
    private final int totalTime;
    private Instant startTime;

    public static ProductionJob restore(String jobId, Order order, int targetQty, int producedQty, int totalTime) {
        return new ProductionJob(jobId, order, targetQty, producedQty, totalTime);
    }

    private ProductionJob(String jobId, Order order, int targetQty, int producedQty, int totalTime) {
        this.jobId = jobId;
        this.order = order;
        this.targetQty = targetQty;
        this.producedQty = producedQty;
        this.totalTime = totalTime;
    }

    public ProductionJob(String jobId, Order order, int shortage) {
        this.jobId = jobId;
        this.order = order;
        this.targetQty = calcTargetQty(shortage, order.getSample().getYield());
        this.producedQty = 0;
        this.totalTime = order.getSample().getAvgProductionTime() * this.targetQty;
    }

    private static int calcTargetQty(int shortage, double yield) {
        return (int) Math.ceil(shortage / (yield * 0.9));
    }

    public String getJobId() { return jobId; }
    public Order getOrder() { return order; }
    public int getTargetQty() { return targetQty; }
    public int getProducedQty() { return producedQty; }
    public int getTotalTime() { return totalTime; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public void produce(int qty) { this.producedQty += qty; }

    public int calcProducedByElapsed(long elapsedSeconds) {
        int avgTime = order.getSample().getAvgProductionTime();
        return Math.min(targetQty, (int) (elapsedSeconds / avgTime));
    }

    public boolean isCompleted() { return producedQty >= targetQty; }
}
