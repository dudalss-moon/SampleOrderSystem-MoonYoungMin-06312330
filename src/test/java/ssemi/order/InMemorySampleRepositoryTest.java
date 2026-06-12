package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Sample;
import ssemi.order.repository.inmemory.InMemorySampleRepository;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySampleRepositoryTest {

    private InMemorySampleRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemorySampleRepository();
    }

    private Sample sample(String id, String name, int stock) {
        return new Sample(id, name, 30, 0.9, stock);
    }

    @Test
    @DisplayName("미등록 ID에 대해 existsById는 false를 반환한다")
    void existsById_없는_ID() {
        assertFalse(repo.existsById("NONE"));
    }

    @Test
    @DisplayName("미등록 이름에 대해 existsByName은 false를 반환한다")
    void existsByName_없는_이름() {
        assertFalse(repo.existsByName("없는이름"));
    }

    @Test
    @DisplayName("3개 저장 후 count()는 3을 반환한다")
    void count_여러_시료() {
        repo.save(sample("S001", "알파", 10));
        repo.save(sample("S002", "베타", 20));
        repo.save(sample("S003", "감마", 30));

        assertEquals(3, repo.count());
    }

    @Test
    @DisplayName("stock이 각각 10·20·30인 3개 저장 후 totalStock()은 60을 반환한다")
    void totalStock_합산() {
        repo.save(sample("S001", "알파", 10));
        repo.save(sample("S002", "베타", 20));
        repo.save(sample("S003", "감마", 30));

        assertEquals(60, repo.totalStock());
    }
}
