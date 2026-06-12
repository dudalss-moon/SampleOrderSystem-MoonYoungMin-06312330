package ssemi.order.service;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.ProductionResult;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.ProductionQueueRepository;

import java.util.List;
import java.util.Optional;

public final class ProductionService {

    private final ProductionQueueRepository productionQueueRepo;
    private final OrderRepository orderRepository;

    public ProductionService(ProductionQueueRepository productionQueueRepo, OrderRepository orderRepository) {
        this.productionQueueRepo = productionQueueRepo;
        this.orderRepository = orderRepository;
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

    public ProductionResult processProduction(int qty) {
        ProductionJob job = productionQueueRepo.peek()
            .orElseThrow(() -> new IllegalStateException("현재 생산 중인 작업이 없습니다."));
        int actualQty = Math.min(qty, job.getTargetQty() - job.getProducedQty());
        job.produce(actualQty);
        if (job.isCompleted()) {
            completeJob(job);
            return new ProductionResult(true, job);
        }
        return new ProductionResult(false, job);
    }

    public void completeJob(ProductionJob job) {
        job.getOrder().getSample().addStock(job.getTargetQty());
        job.getOrder().changeStatus(OrderStatus.CONFIRMED);
        productionQueueRepo.dequeue();
    }
}
