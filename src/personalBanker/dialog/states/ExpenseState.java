package personalBanker.dialog.states;

import java.util.*;

public class ExpenseState extends FinanceState {

    public ExpenseState(Long userId) {
        super(userId);
    }

    @Override
    protected void initializeCategories() {
        categories.put("Еда", 0.0);
        categories.put("Транспорт", 0.0);
        categories.put("Жилье", 0.0);
        categories.put("Досуг", 0.0);
        categories.put("Здоровье", 0.0);
    }

    @Override
    protected String getMenuMessageKey() {
        return "finance.expense.menu";
    }

    @Override
    public String getTypeName() {
        return "расходов";
    }

    @Override
    public Set<String> getBaseCategories() {
        return new HashSet<>(Arrays.asList("Еда", "Транспорт", "Жилье", "Досуг", "Здоровье"));
    }

    @Override
    public Map<String, Double> getCategoriesMap() {
        return new LinkedHashMap<>(categories);
    }
}