package personalBanker.console;

public class ConsoleOutput {
    public ConsoleOutput() {
        System.out.println("Консольный вывод инициализирован");
    }

    public void showMessage(String message) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(message);
        System.out.println("=".repeat(50));
    }

    public void showInfo(String message) {
        System.out.println("\nℹ️  ИНФО: " + message);
    }

    public void showError(String message) {
        System.out.println("\n❌ ОШИБКА: " + message);
    }

    public void showSuccess(String message) {
        System.out.println("\n✅ УСПЕХ: " + message);
    }

    public void showWarning(String message) {
        System.out.println("\n⚠️  ВНИМАНИЕ: " + message);
    }

    public void showWelcome() {
        System.out.println("\n" + "⭐".repeat(60));
        System.out.println("⭐                 ФИНАНСОВЫЙ ПОМОЩНИК                    ⭐");
        System.out.println("⭐".repeat(60));
    }

    public void showGoodbye() {
        System.out.println("\n" + "👋".repeat(30));
        System.out.println("Спасибо за использование финансового помощника!");
        System.out.println("До встречи! 👋");
        System.out.println("👋".repeat(30));
    }
}