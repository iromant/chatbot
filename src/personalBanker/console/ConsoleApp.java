package personalBanker.console;
import personalBanker.dialog.manager.*;
import personalBanker.messageprovider.*;

public class ConsoleApp {
    private final ConsoleInput input;
    private final ConsoleOutput output;
    private final DialogManager dialogManager;
    private final MessageProvider messageProvider;
    private boolean isRunning;

    public ConsoleApp() {
        this.input = new ConsoleInput();
        this.output = new ConsoleOutput();
        this.messageProvider = new HelpMessage();
        this.dialogManager = new DialogManager(new UserSessionManager(), messageProvider);
        this.isRunning = false;
    }

    public void start() {
        isRunning = true;
        output.showWelcome();
        output.showInfo("Приложение запущено. Введите /help для справки, /menu для выходы в главное меню");


        while (isRunning) {
            try {
                Long userId = 1L;

                String userInput = input.getUserInput();

                if (input.isExitCommand(userInput)) {
                    stop();
                    continue;
                }

                String response = dialogManager.processUserInput(userId, userInput);

                output.showMessage(response);

            } catch (Exception e) {
                output.showError("Произошла ошибка: " + e.getMessage());
            }
        }
    }
    public void stop() {
        isRunning = false;
        output.showGoodbye();
        input.close();
    }

    static void main(String[] args) {
        ConsoleApp app = new ConsoleApp();
        app.start();
    }
}
