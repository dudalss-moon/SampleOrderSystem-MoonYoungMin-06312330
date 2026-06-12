package ssemi.order.domain;

public final class ProductionResult {

    private final boolean completed;
    private final ProductionJob job;

    public ProductionResult(boolean completed, ProductionJob job) {
        this.completed = completed;
        this.job = job;
    }

    public boolean isCompleted() { return completed; }
    public ProductionJob getJob() { return job; }
}
