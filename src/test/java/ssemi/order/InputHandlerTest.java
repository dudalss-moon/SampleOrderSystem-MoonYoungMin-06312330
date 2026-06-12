package ssemi.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ssemi.order.ui.InputHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class InputHandlerTest {

    private InputHandler inputOf(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        return new InputHandler(in, new PrintStream(out));
    }

    @Test
    @DisplayName("빈 줄 입력 시 readString은 빈 문자열을 반환한다")
    void readString_빈_문자열_반환() {
        InputHandler handler = inputOf("\n");

        String result = handler.readString("입력 > ");

        assertEquals("", result);
    }

    @Test
    @DisplayName("앞뒤 공백이 있는 문자열 입력 시 readString은 trim된 값을 반환한다")
    void readString_앞뒤_공백_trim() {
        InputHandler handler = inputOf("  hello  \n");

        String result = handler.readString("입력 > ");

        assertEquals("hello", result);
    }

    @Test
    @DisplayName("비숫자 입력 후 재입력 시 readInt는 올바른 정수를 반환한다")
    void readInt_비숫자_후_재입력() {
        InputHandler handler = inputOf("abc\n3\n");

        int result = handler.readInt("선택 > ");

        assertEquals(3, result);
    }

    @Test
    @DisplayName("음수 입력 시 readInt는 음수를 그대로 반환한다")
    void readInt_음수_허용() {
        InputHandler handler = inputOf("-5\n");

        int result = handler.readInt("선택 > ");

        assertEquals(-5, result);
    }

    @Test
    @DisplayName("비숫자 입력 후 재입력 시 readDouble은 올바른 소수를 반환한다")
    void readDouble_비숫자_후_재입력() {
        InputHandler handler = inputOf("xyz\n1.5\n");

        double result = handler.readDouble("수율 > ");

        assertEquals(1.5, result);
    }
}
