package personalBanker.dialog.states;

public class ExpenseState extends FinanceState {

    //инициализация категорий пока будет такой, пока не начнем добавление и удаление категорий
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
    protected String getMenuMessageKey() {return "finance.expense.menu";}

    @Override
    protected String getTypeName() {return "расходов";}
}