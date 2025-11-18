package personalBanker.messageprovider;

public class FinanceMessage extends MessageProvider {

    public FinanceMessage() {
        // Меню
        messages.put("finance.income.menu", """
УПРАВЛЕНИЕ ДОХОДАМИ

Выберите действие:
1. Добавить доход
2. Удалить доход
3. Просмотр статистики доходов""");

        messages.put("finance.expense.menu", """
УПРАВЛЕНИЕ РАСХОДАМИ

Выберите действие:
1. Добавить расход
2. Удалить расход
3. Просмотр статистики расходов""");

        // Операции
        messages.put("finance.operation.add", "Добавить");
        messages.put("finance.operation.remove", "Удалить");
        messages.put("finance.operation.view", "Просмотр статистики");

        // Сообщения выбора категорий
        messages.put("finance.category.selection.header", "Выберите категорию для {0}:");
        messages.put("finance.category.prompt", "Введите номер категории:");

        // Сообщения операций
        messages.put("finance.operation.amount.prompt", "Введите сумму для {0} в категорию \"{1}\":");
        messages.put("finance.operation.added", "✅ Добавлено {0} руб. в категорию \"{1}\"");
        messages.put("finance.operation.removed", "✅ Удалено {0} руб. из категории \"{1}\"");
        messages.put("finance.operation.insufficient", "❌ Недостаточно средств. Доступно: {0} руб.");
        messages.put("finance.operation.cancelled", "❌ Операция отменена");

        // Сообщения ошибок
        messages.put("finance.error.unknown", "❌ Неизвестная команда");
        messages.put("finance.error.invalid.category", "❌ Неверный номер категории. Попробуйте снова:");
        messages.put("finance.error.invalid.number", "❌ Введите номер категории. Попробуйте снова:");
        messages.put("finance.error.positive.sum", "❌ Сумма должна быть положительной. Введите сумму:");
        messages.put("finance.error.invalid.sum", "❌ Неверный формат суммы. Введите число:");

        // Статистика
        messages.put("finance.statistics.empty", "📊 Статистика пуста");
        messages.put("finance.statistics.income", """
    Статистика доходов

{0}

💎 Общий доход: {1} руб.""");

        messages.put("finance.statistics.expense", """
    Статистика доходов

{0}

💎 Общий расход: {1} руб.""");

        messages.put("finance.statistics.item", "• {0}: {1} руб.");
    }
}