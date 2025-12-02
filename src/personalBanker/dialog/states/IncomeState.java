package personalBanker.dialog.states;

import java.util.HashSet;
import java.util.Set;

public class IncomeState extends FinanceState {

    public IncomeState(Long userId) {
        super(userId);
    }

    @Override
    protected void initializeCategories() {
        categories.put("Работа", 0.0);
        categories.put("Пассивный доход", 0.0);
        categories.put("Инвестиции", 0.0);
        categories.put("Подарки", 0.0);
    }

    @Override
    protected String getMenuMessageKey() {
        return "finance.income.menu";
    }

    @Override
    protected String getTypeName() {
        return "доходов";
    }

    @Override
    protected Set<String> getBaseCategories() {
        return Set.of("Работа", "Пассивный доход", "Инвестиции", "Подарки");
    }
}