package personalBanker.messageprovider;

public class FinanceMessage extends AbstractMessageProvider {

    public FinanceMessage() {
        messages.put("finance.income.menu", """
💵 *Управление доходами*

Выберите действие:
1. Добавить доход
2. Удалить доход
3. Просмотр статистики доходов
4. Назад в главное меню""");

        messages.put("finance.expense.menu", """
💸 *Управление расходами*

Выберите действие:
1. Добавить расход
2. Удалить расход
3. Просмотр статистики расходов
4. Назад в главное меню""");

        messages.put("finance.operation.add", "Добавить");
        messages.put("finance.operation.remove", "Удалить");
        messages.put("finance.operation.view", "Просмотр статистики");

        // Категории доходов
        messages.put("finance.income.category.salary", "Работа");
        messages.put("finance.income.category.passive", "Пассивный доход");
        messages.put("finance.income.category.mom", "Мама подкинула)");
        messages.put("finance.income.category.gifts", "Подарки");
        messages.put("finance.income.category.other", "Другое");

        // Категории расходов
        messages.put("finance.expense.category.food", "Еда");
        messages.put("finance.expense.category.transport", "Транспорт");
        messages.put("finance.expense.category.home", "Жилье");
        messages.put("finance.expense.category.freeTime", "Досуг");
        messages.put("finance.expense.category.health", "Здоровье");
        messages.put("finance.expense.category.other", "Другое");

        // Статистика
        messages.put("finance.statistics.income", """
*Статистика доходов*

{0}

Общий доход: {1} руб.""");

        messages.put("finance.statistics.expense", """
*Статистика расходов*

{0}

Общий расход: {1} руб.""");

        messages.put("finance.statistics.item", "• {0}: {1} руб.");
    }

    @Override
    public String getCategoryName() {
        return "Finance Messages";
    }
}
