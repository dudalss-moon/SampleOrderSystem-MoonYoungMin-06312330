package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Sample;
import ssemi.order.repository.SampleRepository;
import ssemi.order.repository.inmemory.InMemorySampleRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SampleRepositoryTest {

    private SampleRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySampleRepository();
    }

    @Test
    @DisplayName("저장한 시료를 ID로 조회하면 동일한 객체가 반환된다")
    void 시료_저장_후_ID로_조회() {
        Sample sample = new Sample("S001", "알파센서", 30, 0.9, 0);
        repository.save(sample);

        Optional<Sample> found = repository.findById("S001");

        assertTrue(found.isPresent());
        assertEquals("S001", found.get().getId());
    }

    @Test
    @DisplayName("findAll은 저장된 모든 시료를 등록 순서대로 반환한다")
    void 전체_시료_조회() {
        repository.save(new Sample("S001", "알파센서", 30, 0.9, 0));
        repository.save(new Sample("S002", "베타칩", 45, 0.85, 0));

        List<Sample> all = repository.findAll();

        assertEquals(2, all.size());
        assertEquals("S001", all.get(0).getId());
        assertEquals("S002", all.get(1).getId());
    }

    @Test
    @DisplayName("findByName은 이름에 키워드가 포함된 시료를 모두 반환한다")
    void 이름으로_검색_부분일치() {
        repository.save(new Sample("S001", "알파센서", 30, 0.9, 0));
        repository.save(new Sample("S002", "베타칩", 45, 0.85, 0));

        List<Sample> result = repository.findByName("알파");

        assertEquals(1, result.size());
        assertEquals("알파센서", result.get(0).getName());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 Optional.empty()가 반환된다")
    void 존재하지않는_ID_조회_빈값() {
        Optional<Sample> found = repository.findById("NONE");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("existsById는 저장된 ID에 대해 true를 반환한다")
    void 중복_ID_존재여부_확인() {
        repository.save(new Sample("S001", "알파센서", 30, 0.9, 0));

        assertTrue(repository.existsById("S001"));
        assertFalse(repository.existsById("S999"));
    }
}
