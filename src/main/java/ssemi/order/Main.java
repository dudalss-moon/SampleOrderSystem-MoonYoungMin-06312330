package ssemi.order;

import ssemi.order.repository.SampleRepository;
import ssemi.order.ui.ConsoleMenu;
import ssemi.order.ui.InputHandler;

public final class Main {

    public static void main(String[] args) {
        SampleRepository sampleRepository = new SampleRepository();
        InputHandler inputHandler = new InputHandler();
        ConsoleMenu menu = new ConsoleMenu(inputHandler, sampleRepository);
        menu.run();
    }
}
