package ssemi.order.service;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.ProductionJob;
import ssemi.order.domain.ProductionResult;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.ProductionQueueRepository;
import ssemi.order.repository.SampleRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ProductionService {

    private final ProductionQueueRepository productionQueueRepo;
    private final OrderRepository orderRepository;
    private final SampleRepository sampleRepository;
    private final ScheduledExecutorService scheduler;

    public ProductionService(ProductionQueueRepository productionQueueRepo, OrderRepository orderRepository,
                             SampleRepository sampleRepository) {
        this.productionQueueRepo = productionQueueRepo;
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
        this.scheduler = null;
    }

    public ProductionService(ProductionQueueRepository productionQueueRepo, OrderRepository orderRepository,
                             SampleRepository sampleRepository, long periodMillis) {
        this.productionQueueRepo = productionQueueRepo;
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(
            this::processAutoProduction,
            0, periodMillis, TimeUnit.MILLISECONDS
        );
    }

    public void shutdown() {
        if (scheduler != null) scheduler.shutdown();
    }

    public boolean isSchedulerShutdown() {
        return scheduler == null || scheduler.isShutdown();
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
        Order order = job.getOrder();
        order.getSample().addStock(job.getTargetQty());
        sampleRepository.save(order.getSample());
        order.changeStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        productionQueueRepo.dequeue();
    }

    public void processAutoProduction() {
        Optional<ProductionJob> currentOpt = productionQueueRepo.peek();
        if (currentOpt.isEmpty()) return;

        ProductionJob job = currentOpt.get();

        if (job.getStartTime() == null) {
            job.setStartTime(Instant.now());
            productionQueueRepo.enqueue(job);  // startTime DB 반영
            return;
        }

        long elapsedSeconds = Duration.between(job.getStartTime(), Instant.now()).toSeconds();
        int newProduced = job.calcProducedByElapsed(elapsedSeconds);

        if (newProduced > job.getProducedQty()) {
            job.produce(newProduced - job.getProducedQty());
            productionQueueRepo.enqueue(job);  // producedQty DB 반영
        }

        if (job.isCompleted()) {
            completeJob(job);
            productionQueueRepo.peek().ifPresent(next -> {
                next.setStartTime(Instant.now());
                productionQueueRepo.enqueue(next);  // 다음 작업 startTime DB 반영
            });
        }
    }
}
