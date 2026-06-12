package ssemi.order.service;

import ssemi.order.domain.Order;
import ssemi.order.domain.ProductionJob;
import ssemi.order.repository.ProductionQueueRepository;

import java.util.List;
import java.util.Optional;

public final class ProductionService {

    private final ProductionQueueRepository productionQueueRepo;

    public ProductionService(ProductionQueueRepository productionQueueRepo) {
        this.productionQueueRepo = productionQueueRepo;
    }

    public ProductionJob createJob(Order order) {
        int shortage = order.getQuantity() - order.getSample().getStock();
        String jobId = productionQueueRepo.generateJobId();
        ProductionJob job = new ProductionJob(jobId, order, shortage);
        productionQueueRepo.enqueue(job);
        return job;
    }

    public Optional<ProductionJob> getCurrentJob() {
        return productionQueueRepo.peek();
    }

    public List<ProductionJob> getQueueList() {
        return productionQueueRepo.getQueue();
    }
}
