package personalBanker.console;

import java.util.Scanner;

public class ConsoleInput {
    private final Scanner scanner;
    public ConsoleInput() {
        this.scanner = new Scanner(System.in);
        System.out.println("Консольный ввод инициализирован");
    }

    public String getUserInput() {
        System.out.print("\n>>> ");
        String input = scanner.nextLine().trim();
        System.out.println("Получен ввод: '" + input + "'");

        return input;
    }

    public boolean isExitCommand(String input) {
        return input.equalsIgnoreCase("/exit") ||
                input.equalsIgnoreCase("/quit") ||
                input.equalsIgnoreCase("выход");
    }

    public void close() {
        if (scanner != null) {
            scanner.close();
            System.out.println("Ресурсы ввода закрыты");
        }
    }
}
