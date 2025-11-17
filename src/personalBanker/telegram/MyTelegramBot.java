// MyTelegramBot.java
package personalBanker.telegram;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import personalBanker.dialog.manager.*;
import personalBanker.messageprovider.AggregatorMessage;
import java.util.List;
import java.util.ArrayList;

public class MyTelegramBot extends TelegramLongPollingBot {
    private final DialogManager dialogManager;
    private static final Dotenv dotenv = Dotenv.load();
    public MyTelegramBot() {
        this.dialogManager = new DialogManager(
                new UserSessionManager(),
                new AggregatorMessage()
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            System.out.println("Получено сообщение: " + messageText + " от " + chatId);

            String response = this.dialogManager.processUserInput(chatId, messageText);

            this.sendMessageWithButtons(chatId, response);
        }

    }
    //это вообще что... Апумпеть, конечно...
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
            row1.add(new KeyboardButton("Меню"));
            row1.add(new KeyboardButton("Справка"));
            rows.add(row1);
            keyboard.setKeyboard(rows);
            message.setReplyMarkup(keyboard);

            try {
                this.execute(message);
            } catch (TelegramApiException e) {
                System.err.println("Ошибка отправки сообщения с кнопками: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }
}