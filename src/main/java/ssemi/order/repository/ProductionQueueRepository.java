package ssemi.order.repository;

import ssemi.order.domain.ProductionJob;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public final class ProductionQueueRepository {

    private final Queue<ProductionJob> queue = new LinkedList<>();
    private int sequence = 1;

    public void enqueue(ProductionJob job) {
        queue.add(job);
    }

    public Optional<ProductionJob> peek() {
        return Optional.ofNullable(queue.peek());
    }

    public Optional<ProductionJob> dequeue() {
        return Optional.ofNullable(queue.poll());
    }

    public List<ProductionJob> getQueue() {
        return List.copyOf(queue);
    }

    public String generateJobId() {
        return String.format("JOB-%04d", sequence++);
    }
}
