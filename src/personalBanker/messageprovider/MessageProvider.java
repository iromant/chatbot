package personalBanker.messageprovider;

import java.util.Map;
import java.util.HashMap;

public class MessageProvider {
    protected Map<String, String> messages;

    public MessageProvider() {
        this.messages = new HashMap<>();
    }

    public String getMessage(String key) {
        if (messages.containsKey(key)){
            return messages.get(key);
        } else {
            return "Сообщение не найдено: " + key;
        }
    }
}
