package ssemi.order.repository.jdbc;

import ssemi.order.domain.Order;
import ssemi.order.domain.ProductionJob;
import ssemi.order.repository.OrderRepository;
import ssemi.order.repository.ProductionQueueRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcProductionQueueRepository implements ProductionQueueRepository {

    private final Connection conn;
    private final OrderRepository orderRepository;

    public JdbcProductionQueueRepository(Connection conn, OrderRepository orderRepository) {
        this.conn = conn;
        this.orderRepository = orderRepository;
    }

    @Override
    public void enqueue(ProductionJob job) {
        String sql = """
            MERGE INTO production_jobs (job_id, order_id, target_qty, produced_qty, total_time, start_time)
            KEY (job_id) VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, job.getJobId());
            ps.setString(2, job.getOrder().getOrderId());
            ps.setInt(3, job.getTargetQty());
            ps.setInt(4, job.getProducedQty());
            ps.setInt(5, job.getTotalTime());
            ps.setTimestamp(6, job.getStartTime() != null ? Timestamp.from(job.getStartTime()) : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("작업 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ProductionJob> peek() {
        List<ProductionJob> jobs = loadAll();
        return jobs.isEmpty() ? Optional.empty() : Optional.of(jobs.get(0));
    }

    @Override
    public Optional<ProductionJob> dequeue() {
        Optional<ProductionJob> first = peek();
        first.ifPresent(job -> {
            String sql = "DELETE FROM production_jobs WHERE job_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, job.getJobId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("작업 제거 실패: " + e.getMessage(), e);
            }
        });
        return first;
    }

    @Override
    public List<ProductionJob> getQueue() {
        return loadAll();
    }

    @Override
    public String generateJobId() {
        try {
            int next;
            try (PreparedStatement sel = conn.prepareStatement(
                    "SELECT next_val FROM sequences WHERE name = 'job'");
                 ResultSet rs = sel.executeQuery()) {
                rs.next();
                next = rs.getInt(1);
            }
            try (PreparedStatement upd = conn.prepareStatement(
                    "UPDATE sequences SET next_val = next_val + 1 WHERE name = 'job'")) {
                upd.executeUpdate();
            }
            return String.format("JOB-%04d", next);
        } catch (SQLException e) {
            throw new IllegalStateException("작업 ID 생성 실패: " + e.getMessage(), e);
        }
    }

    private List<ProductionJob> loadAll() {
        String sql = "SELECT job_id, order_id, target_qty, produced_qty, total_time, start_time FROM production_jobs ORDER BY enqueue_order";
        List<ProductionJob> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String orderId = rs.getString("order_id");
                Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalStateException("주문 없음: " + orderId));
                ProductionJob job = ProductionJob.restore(
                    rs.getString("job_id"),
                    order,
                    rs.getInt("target_qty"),
                    rs.getInt("produced_qty"),
                    rs.getInt("total_time")
                );
                Timestamp startTime = rs.getTimestamp("start_time");
                if (startTime != null) {
                    job.setStartTime(startTime.toInstant());
                }
                result.add(job);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("작업 목록 조회 실패: " + e.getMessage(), e);
        }
        return result;
    }
}
