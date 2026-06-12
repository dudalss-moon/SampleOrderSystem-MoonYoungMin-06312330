package ssemi.order.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public final class InputHandler {

    private final Scanner scanner;
    private final PrintStream out;

    public InputHandler(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in);
        this.out = out;
    }

    public InputHandler() {
        this(System.in, System.out);
    }

    public int readInt(String prompt) {
        while (true) {
            out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                out.println("숫자를 입력해주세요.");
            }
        }
    }

    public String readString(String prompt) {
        out.print(prompt);
        return scanner.nextLine().trim();
    }

    public double readDouble(String prompt) {
        while (true) {
            out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                out.println("숫자를 입력해주세요.");
            }
        }
    }
}
