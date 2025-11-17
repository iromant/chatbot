package personalBanker.dialog.states;

public class IncomeState extends FinanceState {

    //инициализация категорий пока будет такой, пока не начнем добавление и удаление категорий
    @Override
    protected void initializeCategories() {
        categories.put("Работа", 0.0);
        categories.put("Пассивный доход", 0.0);
        categories.put("Мама подкинула)", 0.0);
        categories.put("Подарки", 0.0);
        categories.put("Другое", 0.0);
    }

    @Override
    protected String getMenuMessageKey() {
        return "finance.income.menu";
    }

    @Override
    protected String getTypeName() {
        return "доходов";
    }
}