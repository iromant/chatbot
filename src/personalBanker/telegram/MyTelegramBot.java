package personalBanker.telegram;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import personalBanker.dialog.manager.DialogManager;
import personalBanker.dialog.manager.UserSessionManager;
import personalBanker.messageprovider.CategoriesMessage;

public class MyTelegramBot extends TelegramLongPollingBot {
    private final DialogManager dialogManager = new DialogManager(new UserSessionManager(), new CategoriesMessage());

    public String getBotUsername() {
        return "PersonB_bot";
    }

    public String getBotToken() {
        return "8409370981:AAFHr-mgozzH1sOEcP8yc0oDhXopeGNvp1Q";
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            System.out.println("Получено сообщение: " + messageText + " от " + chatId);
            String response;
            if (!"/start".equals(messageText) && !"Старт".equalsIgnoreCase(messageText)) {
                if (!"/back".equals(messageText) && !"Назад".equalsIgnoreCase(messageText)) {
                    if (!"/menu".equals(messageText) && !"Менюшечка".equalsIgnoreCase(messageText)) {
                        response = this.dialogManager.processUserInput(chatId, messageText);
                    } else {
                        response = this.dialogManager.handleUserMenu(chatId);
                    }
                } else {
                    response = this.dialogManager.goBack(chatId);
                }
            } else {
                response = this.dialogManager.handleUserStart(chatId);
            }

            this.sendMessageWithButtons(chatId, response);
        }

    }

    private void sendMessageWithButtons(Long chatId, String text) {
        if (text != null && !text.trim().isEmpty()) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(text);
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            keyboard.setResizeKeyboard(true);
            keyboard.setOneTimeKeyboard(false);
            List<KeyboardRow> rows = new ArrayList();
            KeyboardRow row1 = new KeyboardRow();
            row1.add(new KeyboardButton("Старт"));
            row1.add(new KeyboardButton("Назад"));
            row1.add(new KeyboardButton("Менюшечка"));
            rows.add(row1);
            keyboard.setKeyboard(rows);
            message.setReplyMarkup(keyboard);

            try {
                this.execute(message);
                System.out.println("Сообщение с кнопками отправлено: " + text);
            } catch (TelegramApiException e) {
                System.err.println("Ошибка отправки сообщения с кнопками: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }
}