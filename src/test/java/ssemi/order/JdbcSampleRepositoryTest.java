package ssemi.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.db.DatabaseConfig;
import ssemi.order.db.SchemaInitializer;
import ssemi.order.domain.Sample;
import ssemi.order.repository.jdbc.JdbcSampleRepository;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcSampleRepositoryTest {

    private DatabaseConfig config;
    private JdbcSampleRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        config = new DatabaseConfig("jdbc:h2:mem:samplerepotest;DB_CLOSE_DELAY=-1");
        Connection conn = config.getConnection();
        SchemaInitializer.initialize(conn);
        conn.createStatement().execute("DELETE FROM production_jobs");
        conn.createStatement().execute("DELETE FROM orders");
        conn.createStatement().execute("DELETE FROM samples");
        repository = new JdbcSampleRepository(conn);
    }

    @AfterEach
    void tearDown() {
        config.close();
    }

    @Test
    @DisplayName("findByName은 키워드를 포함한 시료를 모두 반환한다")
    void 이름_부분일치_검색() {
        repository.save(new Sample("S001", "알파센서", 30, 0.9, 5));
        repository.save(new Sample("S002", "베타칩",  20, 0.8, 3));

        var result = repository.findByName("알파");

        assertEquals(1, result.size());
        assertEquals("알파센서", result.get(0).getName());
    }

    @Test
    @DisplayName("count()와 totalStock()이 DB에 저장된 값 기준으로 정확히 반환된다")
    void count_totalStock_정확성() {
        repository.save(new Sample("S001", "알파센서", 30, 0.9, 5));
        repository.save(new Sample("S002", "베타칩",  20, 0.8, 3));

        assertAll(
            () -> assertEquals(2,  repository.count()),
            () -> assertEquals(8,  repository.totalStock())
        );
    }

    @Test
    @DisplayName("findAll은 저장 순서대로 반환된다")
    void 전체_시료_조회_등록순_보장() {
        repository.save(new Sample("S001", "알파센서", 30, 0.9, 5));
        repository.save(new Sample("S002", "베타칩",  20, 0.8, 3));

        var all = repository.findAll();

        assertEquals(2, all.size());
        assertEquals("S001", all.get(0).getId());
        assertEquals("S002", all.get(1).getId());
    }

    @Test
    @DisplayName("addStock 후 save하면 DB의 stock 값이 갱신된다")
    void 재고_변경_DB_반영() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 5);
        repository.save(sample);

        sample.addStock(3);
        repository.save(sample);

        Sample found = repository.findById("S001").orElseThrow();
        assertEquals(8, found.getStock());
    }

    @Test
    @DisplayName("save 후 findById로 동일한 시료가 반환된다")
    void 시료_저장_후_ID로_조회() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 10);

        repository.save(sample);
        Optional<Sample> found = repository.findById("S001");

        assertTrue(found.isPresent());
        assertAll(
            () -> assertEquals("S001",    found.get().getId()),
            () -> assertEquals("알파센서", found.get().getName()),
            () -> assertEquals(30,         found.get().getAvgProductionTime()),
            () -> assertEquals(0.9,        found.get().getYield()),
            () -> assertEquals(10,         found.get().getStock())
        );
    }
}
