package personalBanker.dialog.states;

import java.util.*;

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
    public String getTypeName() {
        return "доходов";
    }

    @Override
    public Set<String> getBaseCategories() {
        return new HashSet<>(Arrays.asList("Работа", "Пассивный доход", "Инвестиции", "Подарки"));
    }

    @Override
    public Map<String, Double> getCategoriesMap() {
        return new LinkedHashMap<>(categories);
    }

    @Override
    public boolean isIncome() {
        return true; // Это класс доходов
    }
}