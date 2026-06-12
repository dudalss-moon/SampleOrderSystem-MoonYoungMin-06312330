package ssemi.order.repository.inmemory;

import ssemi.order.domain.ProductionJob;
import ssemi.order.repository.ProductionQueueRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public final class InMemoryProductionQueueRepository implements ProductionQueueRepository {

    private final Queue<ProductionJob> queue = new LinkedList<>();
    private int sequence = 1;

    @Override
    public void enqueue(ProductionJob job) {
        boolean alreadyQueued = queue.stream().anyMatch(j -> j.getJobId().equals(job.getJobId()));
        if (!alreadyQueued) {
            queue.add(job);
        }
    }

    @Override
    public Optional<ProductionJob> peek() {
        return Optional.ofNullable(queue.peek());
    }

    @Override
    public Optional<ProductionJob> dequeue() {
        return Optional.ofNullable(queue.poll());
    }

    @Override
    public List<ProductionJob> getQueue() {
        return List.copyOf(queue);
    }

    @Override
    public String generateJobId() {
        return String.format("JOB-%04d", sequence++);
    }
}
