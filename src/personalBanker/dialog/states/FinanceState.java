package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.CategoriesMessage;
import personalBanker.messageprovider.MessageProvider;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public abstract class FinanceState implements DialogState {
    protected final MessageProvider messageProvider;
    protected final Map<String, Double> categories;
    protected String currentOperation; // "add", "remove", или null
    protected String selectedCategory;

    public FinanceState() {
        this.messageProvider = new CategoriesMessage();
        this.categories = new HashMap<>();
        this.currentOperation = null;
        this.selectedCategory = null;
        initializeCategories();
    }

    protected abstract void initializeCategories();
    protected abstract String getMenuMessageKey();
    protected abstract String getTypeName();

    @Override
    public String onEnter() {
        resetOperation();
        return buildMainMenu();
    }

    @Override
    public String userRequest(DialogContext context) {
        String input = context.getUserInput().toLowerCase().trim();

        // Если выбрана категория, обрабатываем сумму
        if (selectedCategory != null && currentOperation != null) {
            return processAmount(input);
        }

        // Если выбрана операция, показываем категории
        if (currentOperation != null) {
            return processCategorySelection(input);
        }

        // Основное меню операций
        return processOperationMenu(input, context);
    }

    @Override
    public DialogState goNextState(DialogContext context) {
        return context.hasNextState() ? context.getNextState() : this;
    }

    private String buildMainMenu() {
        return messageProvider.getMessage(getMenuMessageKey());
    }

    private String processOperationMenu(String input, DialogContext context) {
        switch (input) {
            case "1":
            case "добавить":
                currentOperation = "add";
                return showCategorySelection();
            case "2":
            case "удалить":
                currentOperation = "remove";
                return showCategorySelection();
            case "3":
            case "статистика":
            case "просмотр":
                return showStatistics();
            case "4":
            case "назад":
                context.setNextState(new MainState());
                return "↩️ Возврат в главное меню...";
            default:
                return "❌ Неизвестная команда\n\n" + buildMainMenu();
        }
    }

    private String showCategorySelection() {
        StringBuilder sb = new StringBuilder();
        String operationName = "add".equals(currentOperation) ? "доход" : "расход";
        sb.append("Выберите категорию для ").append(operationName).append(":\n\n");

        List<String> categoryList = new ArrayList<>(categories.keySet());
        for (int i = 0; i < categoryList.size(); i++) {
            String category = categoryList.get(i);
            double amount = categories.getOrDefault(category, 0.0);
            sb.append(i + 1).append(". ").append(category);
            sb.append(" (").append(amount).append(" руб.)\n");
        }

        sb.append("\n").append(categoryList.size() + 1).append(". Отмена");
        sb.append("\n\nВведите номер категории:");
        return sb.toString();
    }

    private String processCategorySelection(String input) {
        try {
            int choice = Integer.parseInt(input);
            List<String> categoryList = new ArrayList<>(categories.keySet());

            if (choice == categoryList.size() + 1) {
                resetOperation();
                return "❌ Операция отменена\n\n" + buildMainMenu();
            }

            if (choice >= 1 && choice <= categoryList.size()) {
                selectedCategory = categoryList.get(choice - 1);
                String operationName = "add".equals(currentOperation) ? "добавления" : "удаления";
                return "Введите сумму для " + operationName + " в категорию \"" + selectedCategory + "\":";
            } else {
                return "❌ Неверный номер категории. Попробуйте снова:\n\n" + showCategorySelection();
            }
        } catch (NumberFormatException e) {
            return "❌ Введите номер категории. Попробуйте снова:\n\n" + showCategorySelection();
        }
    }

    private String processAmount(String input) {
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                return "❌ Сумма должна быть положительной. Введите сумму:";
            }

            return executeFinancialOperation(amount);

        } catch (NumberFormatException e) {
            return "❌ Неверный формат суммы. Введите число:";
        }
    }

    private String executeFinancialOperation(double amount) {
        String result;

        if ("add".equals(currentOperation)) {
            result = addSum(selectedCategory, amount);
        } else {
            result = removeSum(selectedCategory, amount);
        }

        resetOperation();
        return result + "\n\n" + buildMainMenu();
    }

    private String showStatistics() {
        if (categories.isEmpty() || getTotalAmount() == 0) {
            return "📊 Статистика пуста\n\n" + buildMainMenu();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📊 Статистика ").append(getTypeName()).append(":\n\n");

        double total = 0;
        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            if (entry.getValue() > 0) {
                sb.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" руб.\n");
                total += entry.getValue();
            }
        }

        sb.append("\n💎 Итого: ").append(total).append(" руб.\n\n");
        sb.append(buildMainMenu());
        return sb.toString();
    }

    private void resetOperation() {
        currentOperation = null;
        selectedCategory = null;
    }

    // Общие методы для работы с финансами
    public String addSum(String category, double sum) {
        double current = categories.getOrDefault(category, 0.0);
        categories.put(category, current + sum);
        return "✅ Добавлено " + sum + " руб. в категорию \"" + category + "\"";
    }

    public String removeSum(String category, double sum) {
        double current = categories.getOrDefault(category, 0.0);
        if (sum > current) {
            return "❌ Недостаточно средств. Доступно: " + current + " руб.";
        }
        categories.put(category, current - sum);
        return "✅ Удалено " + sum + " руб. из категории \"" + category + "\"";
    }

    private double getTotalAmount() {
        return categories.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
