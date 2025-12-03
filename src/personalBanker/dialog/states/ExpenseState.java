package personalBanker.dialog.states;

import java.util.HashSet;
import java.util.Set;

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
    protected String getTypeName() {
        return "расходов";
    }

    @Override
    protected Set<String> getBaseCategories() {
        return Set.of("Еда", "Транспорт", "Жилье", "Досуг", "Здоровье");
    }
}