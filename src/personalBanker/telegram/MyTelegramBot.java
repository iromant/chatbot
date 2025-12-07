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
    private final AggregatorMessage messageProvider;
    private static final Dotenv dotenv = Dotenv.load();
    private static final Long ADMIN_ID = 123456789L; //затычка для админ-чистки,если надо будет добавлю а так в целом не осбо нужная функция

    private final Map<Long, Long> lastMessageTime = new HashMap<>();
    private static final long CLEAR_DIALOG_TIMEOUT = 5 * 60 * 1000L;

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
            Long userId = null;

            if (update.hasMessage()) {
                userId = update.getMessage().getChatId();
            } else if (update.hasCallbackQuery()) {
                userId = update.getCallbackQuery().getMessage().getChatId();
            } else if (update.hasMyChatMember()) {
                userId = update.getMyChatMember().getFrom().getId();
            }

            if (update.hasMyChatMember() && userId != null) {
                ChatMemberUpdated chatMember = update.getMyChatMember();
                org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember newChatMember = chatMember.getNewChatMember();

                if ("kicked".equals(newChatMember.getStatus())) {
                    deleteUserData(userId);
                    System.out.println("Пользователь " + userId + " удалил бота. Данные очищены.");
                    return;
                }
            }

            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                userId = update.getMessage().getChatId();

                lastMessageTime.put(userId, System.currentTimeMillis());

                if ("/delete_my_data".equals(text) || "удалить мои данные".equalsIgnoreCase(text)) {
                    deleteUserData(userId);
                    sendMessage(userId, "Все ваши данные успешно удалены!", null);
                    return;
                }

                if ("/storage_stats".equals(text) && ADMIN_ID.equals(userId)) {
                    UserCategoryStorage.StorageStats stats = UserCategoryStorage.getStorageStats();
                    sendMessage(userId, "Статистика хранилища:\n" + stats.toString(), null);
                    return;
                }

                if ("CLEAR_MY_DATA".equals(text)) {
                    handleClearDataConfirmation(userId);
                    return;
                }

                if ("CONFIRM_CLEAR_DATA".equals(text)) {
                    deleteUserData(userId);
                    sendMessage(userId, "Все ваши данные успешно удалены!\nБот сброшен к начальному состоянию.",
                            KeyboardManager.getStartMenuKeyboard());
                    return;
                }

                if ("CANCEL_CLEAR_DATA".equals(text)) {
                    sendMessage(userId, "Удаление данных отменено.",
                            KeyboardManager.getMainMenuKeyboard());
                    return;
                }
            }

            if (userId != null) {
                checkForClearedDialog(userId);
            }

            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkForClearedDialog(Long userId) {
        Long lastTime = lastMessageTime.get(userId);
        if (lastTime != null) {
            long timeSinceLastMessage = System.currentTimeMillis() - lastTime;

            if (timeSinceLastMessage > CLEAR_DIALOG_TIMEOUT) {
                System.out.println("Возможно пользователь " + userId + " очистил диалог. " +
                        "Время с последнего сообщения: " + (timeSinceLastMessage/1000) + " сек");
                // место для логики очистки данных,если надо напишу, пока не особо хочу ломать себе ноги,ОЛечка,прошу простить
            }
        }
    }

    private void handleClearDataConfirmation(Long userId) {
        String message = "ВНИМАНИЕ!\n\n" +
                "Вы собираетесь удалить ВСЕ ваши данные:\n" +
                "• Все категории\n" +
                "• Все доходы и расходы\n" +
                "• Всю статистику\n\n" +
                "Это действие НЕОБРАТИМО!\n\n" +
                "Вы уверены, что хотите продолжить?";

        InlineKeyboardMarkup keyboard = KeyboardManager.getClearDataConfirmationKeyboard();
        sendMessage(userId, message, keyboard);
    }

    private void handleTextMessage(Update update) {
        String userInput = update.getMessage().getText();
        Long userId = update.getMessage().getChatId();

        String response = dialogManager.processUserInput(userId, userInput);

        if (userInput.equalsIgnoreCase("статистика") ||
                userInput.equalsIgnoreCase("stats") ||
                userInput.equalsIgnoreCase("statistics")) {
            handleStatisticsWithChart(userId);
            return;
        }

        DialogState currentState = dialogManager.getCurrentState(userId);
        if (currentState instanceof FinanceState) {
            updateCategoriesCache(userId, (FinanceState) currentState);
        }

        String currentStateName = currentState.getClass().getSimpleName();
        String subState = dialogManager.getCurrentSubState(userId);

        InlineKeyboardMarkup inlineKeyboard = KeyboardManager.getResponseContextKeyboard(
                currentStateName, subState, response, userId
        );

        sendMessage(userId, response, inlineKeyboard);
    }

    private void updateCategoriesCache(Long userId, FinanceState financeState) {
        try {
            Map<String, Double> allCategories = financeState.getCategoriesMap();
            Map<String, Double> baseCategoriesMap = new HashMap<>();

            if (financeState.getClass().getSimpleName().contains("Income")) {
                baseCategoriesMap.put("Работа", 0.0);
                baseCategoriesMap.put("Пассивный доход", 0.0);
                baseCategoriesMap.put("Инвестиции", 0.0);
                baseCategoriesMap.put("Подарки", 0.0);
            } else {
                baseCategoriesMap.put("Еда", 0.0);
                baseCategoriesMap.put("Транспорт", 0.0);
                baseCategoriesMap.put("Жилье", 0.0);
                baseCategoriesMap.put("Досуг", 0.0);
                baseCategoriesMap.put("Здоровье", 0.0);
            }

            java.util.List<String> userCategoryNames = new java.util.ArrayList<>();

            for (String category : allCategories.keySet()) {
                if (!baseCategoriesMap.containsKey(category)) {
                    userCategoryNames.add(category);
                }
            }

            String type = financeState.getClass().getSimpleName().contains("Income") ? "income" : "expense";
            KeyboardManager.updateUserCategories(userId, type, userCategoryNames);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long userId = update.getCallbackQuery().getMessage().getChatId();

        lastMessageTime.put(userId, System.currentTimeMillis());

        if ("CLEAR_MY_DATA".equals(callbackData)) {
            handleClearDataConfirmation(userId);
            return;
        }

        if ("CONFIRM_CLEAR_DATA".equals(callbackData)) {
            deleteUserData(userId);
            sendMessage(userId, "Все ваши данные успешно удалены!\nБот сброшен к начальному состоянию.",
                    KeyboardManager.getStartMenuKeyboard());
            return;
        }

        if ("CANCEL_CLEAR_DATA".equals(callbackData)) {
            sendMessage(userId, "Удаление данных отменено.",
                    KeyboardManager.getMainMenuKeyboard());
            return;
        }

        if (callbackData.equals("INCOME_STATS") || callbackData.equals("EXPENSE_STATS")) {
            handleStatisticsWithChart(userId);
            return;
        }

        String response = dialogManager.processUserInput(userId, callbackData);

        DialogState currentState = dialogManager.getCurrentState(userId);
        if (currentState instanceof FinanceState) {
            updateCategoriesCache(userId, (FinanceState) currentState);
        }

        String currentStateName = currentState.getClass().getSimpleName();
        String subState = dialogManager.getCurrentSubState(userId);

        InlineKeyboardMarkup inlineKeyboard = KeyboardManager.getResponseContextKeyboard(
                currentStateName, subState, response, userId
        );

        sendMessage(userId, response, inlineKeyboard);
    }

    private void handleStatisticsWithChart(Long userId) {
        try {
            DialogManager.ChartResponse chartResponse = dialogManager.processStatistics(userId);
            String statsText = chartResponse.getStatistics();

            String currentState = dialogManager.getCurrentState(userId).getClass().getSimpleName();
            String subState = dialogManager.getCurrentSubState(userId);

            InlineKeyboardMarkup keyboard = KeyboardManager.getResponseContextKeyboard(
                    currentState, subState, statsText, userId
            );

            sendMessage(userId, statsText, keyboard);

            sendChart(userId);

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(userId, "Ошибка при генерации статистики", null);
        }
    }

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
            System.out.println("Удаление данных пользователя: " + userId);


            UserCategoryStorage.deleteUserData(userId);


            java.lang.reflect.Field sessionManagerField = DialogManager.class.getDeclaredField("sessionManager");
            sessionManagerField.setAccessible(true);
            UserSessionManager sessionManager = (UserSessionManager) sessionManagerField.get(dialogManager);


            java.lang.reflect.Field sessionsField = UserSessionManager.class.getDeclaredField("sessions");
            sessionsField.setAccessible(true);
            java.util.Map<Long, Object> sessions = (java.util.Map<Long, Object>) sessionsField.get(sessionManager);
            sessions.remove(userId);

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