// MyTelegramBot.java
package personalBanker.telegram;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import personalBanker.dialog.manager.*;
import personalBanker.messageprovider.AggregatorMessage;

public class MyTelegramBot extends TelegramLongPollingBot {
    private final DialogManager dialogManager;
    private final AggregatorMessage messageProvider;
    private static final Dotenv dotenv = Dotenv.load();

    public MyTelegramBot() {
        this.messageProvider = new AggregatorMessage();
        this.dialogManager = new DialogManager(
                new UserSessionManager(),
                this.messageProvider
        );
    }

    @Override
    public String getBotUsername() {
        return dotenv.get("BOT_NAME");
    }

    @Override
    public String getBotToken() {
        return dotenv.get("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTextMessage(Update update) {
        String userInput = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        System.out.println("Текст: " + userInput + " от " + chatId);

        // Обрабатываем ввод
        String response = dialogManager.processUserInput(chatId, userInput);

        // Определяем inline клавиатуру
        InlineKeyboardMarkup inlineKeyboard = getInlineKeyboard(userInput, response);

        // Отправляем ответ с inline кнопками
        sendMessage(chatId, response, inlineKeyboard);
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        System.out.println("Callback: " + callbackData + " от " + chatId);

        // Обрабатываем callback
        String response = dialogManager.processUserInput(chatId, callbackData);

        // Определяем inline клавиатуру для callback
        InlineKeyboardMarkup inlineKeyboard = getInlineKeyboardForCallback(callbackData, response);

        // Отправляем новый ответ с обновленными кнопками
        sendMessage(chatId, response, inlineKeyboard);
    }

    private InlineKeyboardMarkup getInlineKeyboard(String userInput, String response) {
        String input = userInput.toLowerCase();

        // Для команды /start используем стартовую клавиатуру
        if (input.equals("/start") || input.equals("старт")) {
            return KeyboardManager.getStartMenuKeyboard();
        }

        // Определяем по команде
        switch (input) {
            case "меню":
                return KeyboardManager.getMainMenuKeyboard();

            case "доходы":
                return KeyboardManager.getIncomeMenuKeyboard();
            case "расходы":
                return KeyboardManager.getExpenseMenuKeyboard();

            case "справка":
                return KeyboardManager.getHelpKeyboard();
        }

        // Определяем по контексту ответа
        return getKeyboardByResponseContext(response);
    }

    private InlineKeyboardMarkup getInlineKeyboardForCallback(String callbackData, String response) {
        // Определяем клавиатуру по callback данным
        switch (callbackData) {
            case "MAIN_MENU":
                return KeyboardManager.getMainMenuKeyboard();

            case "INCOME_MENU":
            case "INCOME_STATS":
                return KeyboardManager.getIncomeMenuKeyboard();

            case "INCOME_ADD":
            case "INCOME_REMOVE":
                return KeyboardManager.getIncomeCategoriesKeyboard();

            case "EXPENSE_MENU":
            case "EXPENSE_STATS":
                return KeyboardManager.getExpenseMenuKeyboard();

            case "EXPENSE_ADD":
            case "EXPENSE_REMOVE":
                return KeyboardManager.getExpenseCategoriesKeyboard();

            case "HELP":
                return KeyboardManager.getHelpKeyboard();

            case "BACK":
                return getKeyboardByResponseContext(response);

            default:
                if (callbackData.startsWith("CATEGORY_")) {
                    return KeyboardManager.getAmountInputKeyboard();
                }
        }

        return getKeyboardByResponseContext(response);
    }

    private InlineKeyboardMarkup getKeyboardByResponseContext(String response) {
        if (response == null || response.trim().isEmpty()) {
            return KeyboardManager.getMainMenuKeyboard();
        }

        String lowerResponse = response.toLowerCase();

        if (lowerResponse.contains("управление доходами") ||
                (lowerResponse.contains("доход") && !lowerResponse.contains("расход"))) {
            return KeyboardManager.getIncomeMenuKeyboard();
        } else if (lowerResponse.contains("управление расходами") ||
                lowerResponse.contains("расход")) {
            return KeyboardManager.getExpenseMenuKeyboard();
        }
        else if (lowerResponse.contains("выберите категорию")) {
            if (lowerResponse.contains("доход")) {
                return KeyboardManager.getIncomeCategoriesKeyboard();
            } else if (lowerResponse.contains("расход")) {
                return KeyboardManager.getExpenseCategoriesKeyboard();
            }
        } else if (lowerResponse.contains("введите сумму")) {
            return KeyboardManager.getAmountInputKeyboard();
        } else if (lowerResponse.contains("главное меню") ||
                lowerResponse.contains("меню personal banker")) {
            return KeyboardManager.getMainMenuKeyboard();
        }

        return KeyboardManager.getResponseContextKeyboard();
    }

    private void sendMessage(Long chatId, String text, InlineKeyboardMarkup inlineKeyboard) {
        if (text == null || text.trim().isEmpty()) {
            text = "Произошла ошибка";
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        // Устанавливаем inline клавиатуру
        message.setReplyMarkup(inlineKeyboard);

        try {
            execute(message);
            System.out.println("Отправлено сообщение с inline кнопками для " + chatId);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки: " + e.getMessage());
            e.printStackTrace();
        }
    }
}