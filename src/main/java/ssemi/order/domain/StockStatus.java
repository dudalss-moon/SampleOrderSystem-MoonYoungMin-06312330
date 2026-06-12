package ssemi.order.domain;

public enum StockStatus {
    PLENTY, SHORTAGE, DEPLETED;

    public static StockStatus of(int stock, int pendingQty) {
        if (stock == 0) return DEPLETED;
        if (stock < pendingQty) return SHORTAGE;
        return PLENTY;
    }
}
