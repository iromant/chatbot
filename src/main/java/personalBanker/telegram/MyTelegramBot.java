// MyTelegramBot.java
package personalBanker.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import personalBanker.dialog.manager.DialogManager;
import personalBanker.dialog.manager.UserSessionManager;
import personalBanker.messageprovider.CategoriesMessage;

public class MyTelegramBot extends TelegramLongPollingBot {
    private final DialogManager dialogManager;

    public MyTelegramBot() {
        this.dialogManager = new DialogManager(
                new UserSessionManager(),
                new CategoriesMessage()
        );
    }

    @Override
    public String getBotUsername() {
        return "PersonB_bot";
    }

    @Override
    public String getBotToken() {
        return "8409370981:AAFHr-mgozzH1sOEcP8yc0oDhXopeGNvp1Q";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            System.out.println("Получено сообщение: " + messageText + " от " + chatId);

            String response;

            if ("/start".equals(messageText)) {
                response = dialogManager.handleUserStart(chatId);
            } else if ("/back".equals(messageText) || "назад".equals(messageText)) {
                response = dialogManager.goBack(chatId);
            } else {
                response = dialogManager.processUserInput(chatId, messageText);
            }

            sendMessage(chatId, response);
        }
    }

    private void sendMessage(Long chatId, String text) {
        if (text == null || text.trim().isEmpty()) return;

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
            System.out.println("Сообщение отправлено: " + text);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}