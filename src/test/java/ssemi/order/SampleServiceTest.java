package ssemi.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Sample;
import ssemi.order.repository.SampleRepository;
import ssemi.order.service.SampleService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SampleServiceTest {

    private SampleService service;

    @BeforeEach
    void setUp() {
        service = new SampleService(new SampleRepository());
    }

    @Test
    @DisplayName("register 호출 시 시료가 저장되고 반환된다")
    void 시료_등록_정상() {
        Sample sample = service.register("S001", "알파센서", 30, 0.9);

        assertAll(
            () -> assertEquals("S001", sample.getId()),
            () -> assertEquals("알파센서", sample.getName()),
            () -> assertEquals(0, sample.getStock())
        );
    }

    @Test
    @DisplayName("이미 존재하는 ID로 등록하면 IllegalArgumentException이 발생한다")
    void 중복_ID_등록_예외() {
        service.register("S001", "알파센서", 30, 0.9);

        assertThrows(IllegalArgumentException.class,
            () -> service.register("S001", "베타칩", 45, 0.85));
    }

    @Test
    @DisplayName("이미 존재하는 이름으로 등록하면 IllegalArgumentException이 발생한다")
    void 중복_이름_등록_예외() {
        service.register("S001", "알파센서", 30, 0.9);

        assertThrows(IllegalArgumentException.class,
            () -> service.register("S002", "알파센서", 45, 0.85));
    }

    @Test
    @DisplayName("매칭되는 시료가 없으면 빈 리스트가 반환된다")
    void 검색_결과_없음_빈리스트() {
        service.register("S001", "알파센서", 30, 0.9);

        List<Sample> result = service.search("없는시료");

        assertTrue(result.isEmpty());
    }
}
