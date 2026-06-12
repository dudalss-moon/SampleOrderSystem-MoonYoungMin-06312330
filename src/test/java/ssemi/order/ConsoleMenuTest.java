package ssemi.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.domain.Sample;
import ssemi.order.repository.inmemory.InMemoryOrderRepository;
import ssemi.order.repository.inmemory.InMemorySampleRepository;
import ssemi.order.ui.ConsoleMenu;
import ssemi.order.ui.InputHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleMenuTest {

    private InMemorySampleRepository sampleRepo;
    private InMemoryOrderRepository orderRepo;
    private ByteArrayOutputStream out;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        sampleRepo = new InMemorySampleRepository();
        orderRepo = new InMemoryOrderRepository();
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

    private ConsoleMenu menuWith(InputHandler input) {
        return new ConsoleMenu(input, sampleRepo, orderRepo);
    }

    @Test
    @DisplayName("시료가 없을 때 displaySummary는 '0개' 포함 문자열을 출력한다")
    void displaySummary_시료_없음() {
        ConsoleMenu menu = menuWith(inputOf("0"));

        menu.displaySummary();

        String output = out.toString();
        assertTrue(output.contains("0개"), "출력: " + output);
    }

    @Test
    @DisplayName("displayMainMenu는 'S-Semi'와 '1. 시료 관리'를 포함하여 출력한다")
    void displayMainMenu_출력() {
        ConsoleMenu menu = menuWith(inputOf("0"));

        menu.displayMainMenu();

        String output = out.toString();
        assertTrue(output.contains("S-Semi"), "출력: " + output);
        assertTrue(output.contains("1. 시료 관리"), "출력: " + output);
    }

    @Test
    @DisplayName("0 입력 시 run이 '시스템을 종료합니다.' 출력 후 반환된다")
    void run_종료_선택() {
        ConsoleMenu menu = menuWith(inputOf("0"));

        menu.run();

        String output = out.toString();
        assertTrue(output.contains("시스템을 종료합니다."), "출력: " + output);
    }

    @Test
    @DisplayName("잘못된 메뉴 선택 후 0 입력 시 오류 메시지 출력 후 종료된다")
    void run_잘못된_선택_후_종료() {
        ConsoleMenu menu = menuWith(inputOf("9", "0"));

        menu.run();

        String output = out.toString();
        assertTrue(output.contains("올바른 메뉴를 선택해주세요."), "출력: " + output);
    }

    @Test
    @DisplayName("시료가 등록된 경우 displaySummary에 해당 수·재고 합산이 출력된다")
    void displaySummary_시료_있음() {
        sampleRepo.save(new Sample("S001", "알파", 30, 0.9, 10));
        sampleRepo.save(new Sample("S002", "베타", 20, 0.8, 20));
        ConsoleMenu menu = menuWith(inputOf("0"));

        menu.displaySummary();

        String output = out.toString();
        assertTrue(output.contains("2개"), "출력: " + output);
        assertTrue(output.contains("30개"), "출력: " + output);
    }
}
