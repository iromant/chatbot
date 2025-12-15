package personalBanker.telegram;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import personalBanker.dialog.manager.*;
import personalBanker.dialog.chart.ChartGenerator;
import personalBanker.dialog.states.DialogState;
import personalBanker.dialog.states.FinanceState;
import personalBanker.dialog.storage.UserCategoryStorage;
import personalBanker.messageprovider.AggregatorMessage;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MyTelegramBot extends TelegramLongPollingBot {
    private final DialogManager dialogManager;
    private static final Dotenv dotenv = Dotenv.load();

    private final Map<Long, Long> lastMessageTime = new HashMap<>();
    private static final long CLEAR_DIALOG_TIMEOUT = 5 * 60 * 1000L;

    private final AggregatorMessage messageProvider = new AggregatorMessage();

    public MyTelegramBot() {
        this.dialogManager = new DialogManager(
                new UserSessionManager()
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
            Long userId = null;

            if (update.hasMessage()) {
                userId = update.getMessage().getChatId();
            } else if (update.hasCallbackQuery()) {
                userId = update.getCallbackQuery().getMessage().getChatId();
            } else if (update.hasMyChatMember()) {
                userId = update.getMyChatMember().getFrom().getId();
            }

            if (userId != null) {

                if (update.hasMyChatMember()) {
                    ChatMemberUpdated chatMember = update.getMyChatMember();
                    org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember newChatMember = chatMember.getNewChatMember();

                    //а надо ли оно, как будто нет
                    if ("kicked".equals(newChatMember.getStatus())) {
                        deleteUserData(userId);
                        return;
                    }
                }
            }
            String notification = UserCategoryStorage.getPendingNotification(userId);
            if (notification != null && !notification.isEmpty()) {
                // Отправляем уведомление с кнопкой "Назад"
                sendMessage(userId, notification, KeyboardManager.getStartMenuKeyboard());
                return;
            }

            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);

                lastMessageTime.put(userId, System.currentTimeMillis());

            } else if (update.hasCallbackQuery()) {

                handleCallbackQuery(update);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTextMessage(Update update) {
        String userInput = update.getMessage().getText();
        Long userId = update.getMessage().getChatId();

        if (userInput.equalsIgnoreCase("статистика") ||
                userInput.equalsIgnoreCase("stats") ||
                userInput.equalsIgnoreCase("statistics")) {
            handleStatisticsWithChart(userId);
            return;
        }

        handleUserInput(userId, userInput);

        if (userInput.equalsIgnoreCase("старт") ||
                userInput.equalsIgnoreCase("/start")) {
            deleteUserData(userId);
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long userId = update.getCallbackQuery().getMessage().getChatId();

        lastMessageTime.put(userId, System.currentTimeMillis());

        if ("CLEAR_MY_DATA".equals(callbackData)) {
            InlineKeyboardMarkup keyboard = KeyboardManager.getClearDataConfirmationKeyboard();
            sendMessage(userId, messageProvider.getMessage("finance.clear.data.confirm"), keyboard);
            return;
        }

        if ("CONFIRM_CLEAR_DATA".equals(callbackData)) {
            deleteUserData(userId);
            sendMessage(userId, "✅ Все ваши данные успешно удалены!\n" +
                            "Бот сброшен к начальному состоянию\n\n\n" +
                    messageProvider.getMessage("welcome"),
                    KeyboardManager.getStartMenuKeyboard());
            return;
        }

        if ("CANCEL_CLEAR_DATA".equals(callbackData)) {
            sendMessage(userId, "❌ Удаление данных отменено\n\n\n"
                            + messageProvider.getMessage("menu.main") ,
                    KeyboardManager.getMainMenuKeyboard());
            return;
        }

        // Обработка статистики
        if (callbackData.equals("INCOME_STATS") || callbackData.equals("EXPENSE_STATS")) {
            handleStatisticsWithChart(userId);
            return;
        }

        handleUserInput(userId, callbackData);

    }

    //просто вынесла стандартную обработку DialogManager
    private void handleUserInput(Long userId, String userInput) {

        String response = dialogManager.processUserInput(userId, userInput);

        String currentState = dialogManager.getCurrentState(userId).getClass().getSimpleName();
        String subState = dialogManager.getCurrentSubState(userId);
        InlineKeyboardMarkup inlineKeyboard = KeyboardManager.getResponseContextKeyboard(
                currentState, subState, response, userId);

        sendMessage(userId, response, inlineKeyboard);
    }

    private void handleStatisticsWithChart(Long userId) {
        try {
            DialogManager.ChartResponse chartResponse = dialogManager.processStatistics(userId);
            String statsText = chartResponse.getStatistics();

            String currentState = dialogManager.getCurrentState(userId).getClass().getSimpleName();
            String subState = dialogManager.getCurrentSubState(userId);

            InlineKeyboardMarkup keyboard = KeyboardManager.getResponseContextKeyboard(
                    currentState, subState, statsText, userId);

            sendMessage(userId, statsText, keyboard);
            sendChart(userId);

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(userId, "Ошибка при генерации статистики", null);
        }
    }

    //ничего не меняю
    private void sendChart(Long userId) {
        try {
            DialogState state = dialogManager.getCurrentState(userId);
            if (!(state instanceof FinanceState)) {
                return;
            }

            FinanceState financeState = (FinanceState) state;

            Map<String, Double> chartData = financeState.getChartData();

            if (chartData.isEmpty()) {
                return;
            }

            String chartTitle = financeState.getClass().getSimpleName().contains("Income") ?
                    "Диаграмма доходов" : "Диаграмма расходов";

            InputStream chartStream = ChartGenerator.generatePieChart(chartData, chartTitle);

            if (chartStream != null) {
                Thread.sleep(300);

                SendPhoto photo = new SendPhoto();
                photo.setChatId(userId.toString());

                InputFile inputFile = new InputFile(chartStream, "chart.png");
                photo.setPhoto(inputFile);
                photo.setCaption(chartTitle);

                execute(photo);
                chartStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteUserData(Long userId) {
        try {
            UserCategoryStorage.deleteUserData(userId);

            UserSessionManager sessionManager = new UserSessionManager();
            sessionManager.clearUserSession(userId);

            lastMessageTime.remove(userId);

            System.out.println("Данные пользователя " + userId + " полностью удалены");

        } catch (Exception e) {
            System.err.println("Ошибка при удалении данных пользователя " + userId);
            e.printStackTrace();
        }
    }

    private void sendMessage(Long userId, String text, InlineKeyboardMarkup inlineKeyboard) {
        if (text == null || text.trim().isEmpty()) {
            text = "Произошла ошибка";
        }

        SendMessage message = new SendMessage();
        message.setChatId(userId.toString());
        message.setText(text);

        if (inlineKeyboard != null) {
            message.setReplyMarkup(inlineKeyboard);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}