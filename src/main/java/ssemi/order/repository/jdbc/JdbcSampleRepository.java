package ssemi.order.repository.jdbc;

import ssemi.order.domain.Sample;
import ssemi.order.repository.SampleRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcSampleRepository implements SampleRepository {

    private final Connection conn;

    public JdbcSampleRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void save(Sample sample) {
        String sql = """
            MERGE INTO samples (id, name, avg_production_time, yield, stock)
            KEY (id) VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sample.getId());
            ps.setString(2, sample.getName());
            ps.setInt(3, sample.getAvgProductionTime());
            ps.setDouble(4, sample.getYield());
            ps.setInt(5, sample.getStock());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("시료 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Sample> findById(String id) {
        String sql = "SELECT * FROM samples WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("시료 조회 실패: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Sample> findByName(String keyword) {
        String sql = "SELECT * FROM samples WHERE name LIKE ?";
        List<Sample> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("시료 검색 실패: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Sample> findAll() {
        String sql = "SELECT * FROM samples";
        List<Sample> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("시료 전체 조회 실패: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT COUNT(*) FROM samples WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("시료 존재 확인 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM samples WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("시료 이름 존재 확인 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM samples";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("시료 카운트 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public int totalStock() {
        String sql = "SELECT COALESCE(SUM(stock), 0) FROM samples";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("총 재고 조회 실패: " + e.getMessage(), e);
        }
    }

    private Sample mapRow(ResultSet rs) throws SQLException {
        return new Sample(
            rs.getString("id"),
            rs.getString("name"),
            rs.getInt("avg_production_time"),
            rs.getDouble("yield"),
            rs.getInt("stock")
        );
    }
}
