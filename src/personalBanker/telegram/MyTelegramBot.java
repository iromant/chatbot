package personalBanker.telegram;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import personalBanker.dialog.manager.*;
import personalBanker.messageprovider.AggregatorMessage;

import java.io.File;

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
        Long userId = update.getMessage().getChatId();

        String response = dialogManager.processUserInput(userId, userInput);

        if (userInput.equalsIgnoreCase("статистика") ||
                userInput.equalsIgnoreCase("stats") ||
                userInput.equalsIgnoreCase("statistics")) {
            handleStatisticsWithChart(userId);
            return;
        }

        String currentState = dialogManager.getCurrentState(userId).getClass().getSimpleName();
        String subState = dialogManager.getCurrentSubState(userId);

        InlineKeyboardMarkup inlineKeyboard = KeyboardManager.getResponseContextKeyboard(currentState, subState, response);

        sendMessage(userId, response, inlineKeyboard);
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long userId = update.getCallbackQuery().getMessage().getChatId();

        if (callbackData.equals("INCOME_STATS") || callbackData.equals("EXPENSE_STATS")) {
            handleStatisticsWithChart(userId);
            return;
        }

        String response = dialogManager.processUserInput(userId, callbackData);

        String currentState = dialogManager.getCurrentState(userId).getClass().getSimpleName();
        String subState = dialogManager.getCurrentSubState(userId);

        InlineKeyboardMarkup inlineKeyboard = KeyboardManager.getResponseContextKeyboard(currentState, subState, response);

        sendMessage(userId, response, inlineKeyboard);
    }

    private void handleStatisticsWithChart(Long userId) {
        try {
            DialogManager.ChartResponse chartResponse = dialogManager.processStatistics(userId);
            String statsText = chartResponse.getStatistics();
            String chartPath = chartResponse.getChartPath();

            String currentState = dialogManager.getCurrentState(userId).getClass().getSimpleName();
            String subState = dialogManager.getCurrentSubState(userId);

            InlineKeyboardMarkup keyboard = KeyboardManager.getResponseContextKeyboard(currentState, subState, statsText);

            sendMessage(userId, statsText, keyboard);

            if (chartPath != null && new File(chartPath).exists()) {
                Thread.sleep(300);

                String chartCaption = "Диаграмма " +
                        (dialogManager.getCurrentState(userId).getClass().getSimpleName().contains("Income") ?
                                "доходов" : "расходов");

                sendPhoto(userId, chartPath, chartCaption);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(userId, "Ошибка при генерации статистики", null);
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

    private void sendPhoto(Long userId, String photoPath, String caption) {
        try {
            SendPhoto photo = new SendPhoto();
            photo.setChatId(userId.toString());

            File photoFile = new File(photoPath);
            if (!photoFile.exists()) {
                return;
            }

            InputFile inputFile = new InputFile(photoFile, "chart.png");
            photo.setPhoto(inputFile);

            if (caption != null && !caption.isEmpty()) {
                photo.setCaption(caption);
            }

            execute(photo);

            photoFile.delete();

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}