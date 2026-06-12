package ssemi.order.repository.jdbc;

import ssemi.order.domain.Order;
import ssemi.order.domain.OrderStatus;
import ssemi.order.domain.Sample;
import ssemi.order.repository.OrderRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcOrderRepository implements OrderRepository {

    private final Connection conn;

    public JdbcOrderRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void save(Order order) {
        String sql = """
            MERGE INTO orders (order_id, sample_id, customer_name, quantity, status, stock_deducted)
            KEY (order_id) VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getOrderId());
            ps.setString(2, order.getSample().getId());
            ps.setString(3, order.getCustomerName());
            ps.setInt(4, order.getQuantity());
            ps.setString(5, order.getStatus().name());
            ps.setBoolean(6, order.isStockDeducted());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("주문 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Order> findById(String orderId) {
        String sql = """
            SELECT o.order_id, o.customer_name, o.quantity, o.status, o.stock_deducted,
                   s.id AS sid, s.name AS sname, s.avg_production_time, s.yield, s.stock
            FROM orders o
            JOIN samples s ON o.sample_id = s.id
            WHERE o.order_id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("주문 조회 실패: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        String sql = """
            SELECT o.order_id, o.customer_name, o.quantity, o.status, o.stock_deducted,
                   s.id AS sid, s.name AS sname, s.avg_production_time, s.yield, s.stock
            FROM orders o
            JOIN samples s ON o.sample_id = s.id
            WHERE o.status = ?
            """;
        List<Order> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("주문 상태 조회 실패: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Order> findAll() {
        String sql = """
            SELECT o.order_id, o.customer_name, o.quantity, o.status, o.stock_deducted,
                   s.id AS sid, s.name AS sname, s.avg_production_time, s.yield, s.stock
            FROM orders o
            JOIN samples s ON o.sample_id = s.id
            """;
        List<Order> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("주문 전체 조회 실패: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public String generateId() {
        try {
            int next;
            try (PreparedStatement sel = conn.prepareStatement(
                    "SELECT next_val FROM sequences WHERE name = 'order'");
                 ResultSet rs = sel.executeQuery()) {
                rs.next();
                next = rs.getInt(1);
            }
            try (PreparedStatement upd = conn.prepareStatement(
                    "UPDATE sequences SET next_val = next_val + 1 WHERE name = 'order'")) {
                upd.executeUpdate();
            }
            return String.format("ORD-%04d", next);
        } catch (SQLException e) {
            throw new IllegalStateException("주문 ID 생성 실패: " + e.getMessage(), e);
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Sample sample = new Sample(
            rs.getString("sid"),
            rs.getString("sname"),
            rs.getInt("avg_production_time"),
            rs.getDouble("yield"),
            rs.getInt("stock")
        );
        Order order = new Order(
            rs.getString("order_id"),
            sample,
            rs.getString("customer_name"),
            rs.getInt("quantity")
        );
        OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
        while (order.getStatus() != status) {
            order.changeStatus(nextStatus(order.getStatus(), status));
        }
        if (rs.getBoolean("stock_deducted")) {
            order.markStockDeducted();
        }
        return order;
    }

    private OrderStatus nextStatus(OrderStatus current, OrderStatus target) {
        return switch (current) {
            case RESERVED  -> (target == OrderStatus.REJECTED) ? OrderStatus.REJECTED
                              : (target == OrderStatus.PRODUCING) ? OrderStatus.PRODUCING
                              : OrderStatus.CONFIRMED;
            case PRODUCING -> OrderStatus.CONFIRMED;
            case CONFIRMED -> OrderStatus.RELEASE;
            default -> throw new IllegalStateException("상태 복원 불가: " + current + " → " + target);
        };
    }
}
