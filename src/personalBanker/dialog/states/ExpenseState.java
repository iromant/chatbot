package personalBanker.dialog.states;

public class ExpenseState extends FinanceState {

    @Override
    protected void initializeCategories() {
        categories.put("Еда", 0.0);
        categories.put("Транспорт", 0.0);
        categories.put("Жилье", 0.0);
        categories.put("Досуг", 0.0);
        categories.put("Здоровье", 0.0);
        categories.put("Другое", 0.0);
    }

    @Override
    protected String getMenuMessageKey() {
        return "finance.expense.menu"; // Меняем на expense.menu
    }

    @Override
    protected String getTypeName() {
        return "расходов"; // Меняем на расходов
    }
}
