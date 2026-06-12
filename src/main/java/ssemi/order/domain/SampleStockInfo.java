package ssemi.order.domain;

public final class SampleStockInfo {

    private final Sample sample;
    private final int pendingQuantity;
    private final StockStatus stockStatus;

    public SampleStockInfo(Sample sample, int pendingQuantity) {
        this.sample = sample;
        this.pendingQuantity = pendingQuantity;
        this.stockStatus = StockStatus.of(sample.getStock(), pendingQuantity);
    }

    public Sample getSample() { return sample; }
    public int getPendingQuantity() { return pendingQuantity; }
    public StockStatus getStockStatus() { return stockStatus; }
}
