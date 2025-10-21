// HelpMessage.java
package personalBanker.messageprovider;

public class HelpMessage extends AbstractMessageProvider {
    public HelpMessage() {
        messages.put("help.main", """
            Справка по командам

            Управление финансами:
            /позже появятся категории

            Планирование:
            /позже появятся категории

            Помощь:
            /help - Показать эту справку
            /menu - Главное меню
            /start - Перезапуск бота
            /back - возвращение в предыдущее состояние

            Просто введите команду или выберите действие из меню!""");
    }

    @Override
    public String getCategoryName() {
        return "Help Messages";
    }
}