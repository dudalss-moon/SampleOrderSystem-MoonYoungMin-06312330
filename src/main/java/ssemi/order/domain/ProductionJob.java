package ssemi.order.domain;

public final class ProductionJob {

    private final String jobId;
    private final Order order;
    private final int targetQty;
    private int producedQty;
    private final int totalTime;

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
    public void produce(int qty) { this.producedQty += qty; }

    public boolean isCompleted() { return producedQty >= targetQty; }
}
