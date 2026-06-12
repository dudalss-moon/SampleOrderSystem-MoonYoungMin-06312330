package ssemi.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Sample;
import ssemi.order.repository.inmemory.InMemorySampleRepository;
import ssemi.order.service.SampleService;
import ssemi.order.ui.InputHandler;
import ssemi.order.ui.SampleUI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class SampleUITest {

    private InMemorySampleRepository sampleRepo;
    private ByteArrayOutputStream out;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        sampleRepo = new InMemorySampleRepository();
        out = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(out));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private InputHandler inputOf(String... lines) {
        String joined = String.join(System.lineSeparator(), lines) + System.lineSeparator();
        ByteArrayInputStream in = new ByteArrayInputStream(joined.getBytes());
        return new InputHandler(in, new PrintStream(out));
    }

    private SampleUI uiWith(InputHandler input) {
        return new SampleUI(new SampleService(sampleRepo), input);
    }

    @Test
    @DisplayName("시료 등록 성공 시 시료명을 포함한 확인 메시지가 출력된다")
    void 시료_등록_성공() {
        InputHandler input = inputOf("1", "S01", "Alpha", "30", "0.9", "0");
        SampleUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("Alpha"), "출력: " + output);
    }

    @Test
    @DisplayName("이미 등록된 ID로 재등록 시도 시 '오류:' 포함 메시지가 출력된다")
    void 시료_등록_중복_오류() {
        sampleRepo.save(new Sample("S01", "Alpha", 30, 0.9, 0));
        InputHandler input = inputOf("1", "S01", "AlphaDup", "30", "0.9", "0");
        SampleUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("오류:"), "출력: " + output);
    }

    @Test
    @DisplayName("시료가 없을 때 목록 조회 시 '0개 등록됨'이 출력된다")
    void 시료_목록_빈_경우() {
        InputHandler input = inputOf("2", "0");
        SampleUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("0개 등록됨"), "출력: " + output);
    }

    @Test
    @DisplayName("시료가 있을 때 목록 조회 시 시료명이 출력된다")
    void 시료_목록_데이터_있음() {
        sampleRepo.save(new Sample("S001", "알파센서", 30, 0.9, 10));
        InputHandler input = inputOf("2", "0");
        SampleUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("알파센서"), "출력: " + output);
    }

    @Test
    @DisplayName("시료 검색 시 일치하는 이름의 행이 출력된다")
    void 시료_검색_결과_있음() {
        sampleRepo.save(new Sample("S001", "Alpha", 30, 0.9, 10));
        InputHandler input = inputOf("3", "Alpha", "0");
        SampleUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("Alpha"), "출력: " + output);
    }

    @Test
    @DisplayName("검색 결과가 없을 때 '검색 결과가 없습니다.'가 출력된다")
    void 시료_검색_결과_없음() {
        InputHandler input = inputOf("3", "ZZZ", "0");
        SampleUI ui = uiWith(input);

        ui.show();

        String output = out.toString();
        assertTrue(output.contains("검색 결과가 없습니다."), "출력: " + output);
    }
}
