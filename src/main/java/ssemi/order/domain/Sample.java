package ssemi.order.domain;

public final class Sample {

    private final String id;
    private final String name;
    private final int avgProductionTime;
    private final double yield;
    private int stock;

    public Sample(String id, String name, int avgProductionTime, double yield, int stock) {
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
    public void setStock(int stock) { this.stock = stock; }
}
