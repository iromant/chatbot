package personalBanker.messageprovider;

import java.util.Map;
import java.util.HashMap;

public class AggregatorMessage  {
    private final Map<String, String> allMessages = new HashMap<>();

    public AggregatorMessage() {
        loadAllMessages();
    }

    private void loadAllMessages() {
        HelpMessage helpMessage = new HelpMessage();
        MenuMessage menuMessage = new MenuMessage();
        FinanceMessage financeMessage = new FinanceMessage();

        allMessages.putAll(helpMessage.messages);
        allMessages.putAll(menuMessage.messages);
        allMessages.putAll(financeMessage.messages);
    }

    public String getMessage(String key) {
        return allMessages.getOrDefault(key, "Сообщение не найдено: " + key);
    }
}