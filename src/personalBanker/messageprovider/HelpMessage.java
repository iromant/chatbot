package personalBanker.messageprovider;

public class HelpMessage extends AbstractMessageProvider {
    public HelpMessage() {
        messages.put("help.main", """
📖 *Справка по командам*


ОСНОВНЫЕ РАЗДЕЛЫ:
• Доходы - управление доходами, добавление, удаление, статистика
• Расходы - управление расходами, анализ, категории

НАВИГАЦИЯ:
• /help - Показать эту справку
• /menu - Главное меню
• /start - Перезапуск бота
• /back - возвращение в предыдущее состояние

БЫСТРЫЕ КОМАНДЫ
• "доходы" или 1 - раздел доходов
• "расходы" или 2 - раздел расходов

Просто введите команду или выберите действие из меню!""");
    }

    @Override
    public String getCategoryName() {
        return "Help Messages";
    }
}