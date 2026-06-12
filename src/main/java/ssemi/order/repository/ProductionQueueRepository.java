package ssemi.order.repository;

import ssemi.order.domain.ProductionJob;

import java.util.List;
import java.util.Optional;

public interface ProductionQueueRepository {
    void enqueue(ProductionJob job);
    Optional<ProductionJob> peek();
    Optional<ProductionJob> dequeue();
    List<ProductionJob> getQueue();
    String generateJobId();
}
