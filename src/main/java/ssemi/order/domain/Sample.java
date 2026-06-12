package ssemi.order.domain;

public final class Sample {

    private final String id;
    private final String name;
    private final int avgProductionTime;
    private final double yield;
    private int stock;

    public Sample(String id, String name, int avgProductionTime, double yield, int stock) {
        if (avgProductionTime <= 0) throw new IllegalArgumentException("평균생산시간은 0보다 커야 합니다.");
        if (yield <= 0.0 || yield > 1.0) throw new IllegalArgumentException("수율은 0 초과 1.0 이하여야 합니다.");
        this.id = id;
        this.name = name;
        this.avgProductionTime = avgProductionTime;
        this.yield = yield;
        this.stock = stock;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAvgProductionTime() { return avgProductionTime; }
    public double getYield() { return yield; }
    public int getStock() { return stock; }

    public void addStock(int qty) {
        this.stock += qty;
    }

    public void deductStock(int qty) {
        if (qty > this.stock) throw new IllegalStateException("재고가 부족합니다.");
        this.stock -= qty;
    }

    public boolean hasEnoughStock(int qty) {
        return this.stock >= qty;
    }
}
