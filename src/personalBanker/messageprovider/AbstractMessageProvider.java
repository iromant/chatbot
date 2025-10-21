package personalBanker.messageprovider;

import java.util.Map;
import java.util.HashMap;
import java.text.MessageFormat;

public abstract class AbstractMessageProvider implements MessageProvider {
    protected Map<String, String> messages;

    public AbstractMessageProvider() {
        this.messages = new HashMap<>();
    }

    @Override
    public String getMessage(String key) {
        if (messages.containsKey(key)){
            return messages.get(key);
        } else {
            return "Сообщение не найдено: " + key;
        }
    }

    @Override
    public String getMessage(String key, Object... args) {
        String pattern = getMessage(key);
        try {
            return MessageFormat.format(pattern, args);
        } catch (IllegalArgumentException e) {
            return "Ошибка форматирования сообщения: " + key + " с аргументами: ";
        }
    }

    @Override
    public boolean containsKey(String key) {
        return messages.containsKey(key);
    }

    public abstract String getCategoryName();
}
