package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.AggregatorMessage;
import personalBanker.dialog.chart.ChartGenerator;
import personalBanker.dialog.storage.UserCategoryStorage;

import java.util.*;
import java.text.MessageFormat;

public abstract class FinanceState implements DialogState {
    protected final AggregatorMessage messageProvider;
    protected final Map<String, Double> categories;
    protected String currentOperation;
    protected String selectedCategory;
    protected final Long userId;

    protected enum SubState {
        MAIN_MENU,
        CATEGORY_SELECTION,
        AMOUNT_INPUT,
        CATEGORY_MANAGEMENT,
        ADD_CATEGORY,
        REMOVE_CATEGORY
    }

    protected SubState currentSubState;
    protected String tempCategoryName;

    public FinanceState(Long userId) {
        this.messageProvider = new AggregatorMessage();
        this.categories = new LinkedHashMap<>();
        this.currentOperation = null;
        this.selectedCategory = null;
        this.currentSubState = SubState.MAIN_MENU;
        this.userId = userId;
        this.tempCategoryName = null;
        initializeCategories();
        loadUserCategories();
    }

    protected abstract void initializeCategories();
    protected abstract String getMenuMessageKey();
    protected abstract String getTypeName();
    protected abstract Set<String> getBaseCategories();

    protected void loadUserCategories() {
        Map<String, Double> userCategories = UserCategoryStorage.loadUserCategories(
                userId,
                getTypeName().equals("доходов") ? "income" : "expense"
        );

        for (Map.Entry<String, Double> entry : userCategories.entrySet()) {
            categories.put(entry.getKey(), entry.getValue());
        }
    }

    protected void saveUserCategories() {
        Map<String, Double> userCategories = new HashMap<>();
        Set<String> baseCategories = getBaseCategories();

        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            if (!baseCategories.contains(entry.getKey())) {
                userCategories.put(entry.getKey(), entry.getValue());
            }
        }

        UserCategoryStorage.saveUserCategories(
                userId,
                getTypeName().equals("доходов") ? "income" : "expense",
                userCategories
        );
    }

    @Override
    public String onEnter() {
        resetOperation();
        return messageProvider.getMessage(getMenuMessageKey());
    }

    @Override
    public DialogState goNextState(DialogContext context) {
        return context.hasNextState() ? context.getNextState() : this;
    }

    @Override
    public String userRequest(DialogContext context) {
        String input = context.getUserInput();

        if (input.equals("MANAGE_CATEGORIES")) {
            currentSubState = SubState.CATEGORY_MANAGEMENT;
            return showCategoryManagement();
        }

        if (input.equals("ADD_CATEGORY")) {
            currentSubState = SubState.ADD_CATEGORY;
            return "Введите название новой категории:";
        }

        if (input.equals("REMOVE_CATEGORY")) {
            currentSubState = SubState.REMOVE_CATEGORY;
            return showCategorySelection() + "\n\nВведите название категории для удаления:";
        }

        if ((input.equalsIgnoreCase("назад") || input.equals("BACK")) &&
                currentSubState != SubState.MAIN_MENU) {
            return handleBackButton();
        }

        // Сначала обрабатываем универсальные команды
        Optional<String> universalResult = UniversalCommand.executeCommand(input, context);
        if (universalResult.isPresent()) {
            String result = universalResult.get();
            if (!result.isEmpty()) {
                return result;
            }
            return getCurrentStateMessage();
        }

        // Затем обрабатываем финансовые операции
        return handleFinancialInput(input);
    }

    @Override
    public String getCurrentSubState() {
        return currentSubState.name();
    }

    private String handleFinancialInput(String input) {
        // Обработка callback данных для финансовых операций
        if (input.startsWith("INCOME_") || input.startsWith("EXPENSE_")
                || input.startsWith("CATEGORY_")) {
            return handleCallback(input);
        }

        return handleTextInput(input.toLowerCase().trim());
    }
    private String showCategorySelectionForRemoval() {
        StringBuilder sb = new StringBuilder();
        sb.append("➖ Удаление категории\n\n");
        sb.append("Выберите категорию для удаления:\n\n");

        int i = 1;
        for (String category : categories.keySet()) {
            double amount = categories.getOrDefault(category, 0.0);
            String baseMarker = getBaseCategories().contains(category) ? " (базовая)" : "";
            String canDelete = amount == 0 && !getBaseCategories().contains(category) ? " ✅" : " ❌";

            sb.append(i).append(". ").append(category).append(baseMarker).append(canDelete)
                    .append(": ").append(String.format("%.2f", amount)).append(" руб\n");
            i++;
        }

        sb.append("\n❌ - нельзя удалить (базовая или есть баланс)");
        sb.append("\n✅ - можно удалить");
        sb.append("\n\nМожно ввести название категории текстом");

        return sb.toString();
    }    private String handleCallback(String callbackData) {
        switch (callbackData) {
            case "INCOME_ADD":
            case "EXPENSE_ADD":
                currentOperation = "add";
                currentSubState = SubState.CATEGORY_SELECTION;
                return showCategorySelectionWithInstructions();

            case "INCOME_REMOVE":
            case "EXPENSE_REMOVE":
                currentOperation = "remove";
                currentSubState = SubState.CATEGORY_SELECTION;
                return showCategorySelectionWithInstructions();

            case "INCOME_STATS":
            case "EXPENSE_STATS":
                currentSubState = SubState.MAIN_MENU;
                return showStatistics();

            case "MANAGE_CATEGORIES":
                currentSubState = SubState.CATEGORY_MANAGEMENT;
                return showCategoryManagement();

            case "ADD_CATEGORY":
                currentSubState = SubState.ADD_CATEGORY;
                return "➕ Добавление новой категории\n\n" +
                        "Введите название новой категории:\n\n" +
                        "Новые категории начинаются с баланса 0 руб";

            case "REMOVE_CATEGORY":
                currentSubState = SubState.REMOVE_CATEGORY;
                return showCategorySelectionForRemoval();

            default:
                if (callbackData.startsWith("CATEGORY_")) {
                    String[] parts = callbackData.split("_", 3);
                    if (parts.length >= 3) {
                        String categoryName = parts[2];

                        if (currentSubState == SubState.REMOVE_CATEGORY) {
                            return removeCategory(categoryName);
                        } else {
                            selectedCategory = categoryName;
                            currentSubState = SubState.AMOUNT_INPUT;
                            String operationType = "add".equals(currentOperation) ? "добавления" : "удаления";
                            double currentBalance = categories.getOrDefault(selectedCategory, 0.0);

                            return MessageFormat.format(
                                    "Ввод суммы\n\n" +
                                            "Категория: \"{0}\"\n" +
                                            "Текущий баланс: {1} руб\n\n" +
                                            "Введите сумму для {2}:",
                                    selectedCategory,
                                    String.format("%.2f", currentBalance),
                                    operationType
                            );
                        }
                    }
                }
                return messageProvider.getMessage("finance.error.unknown");
        }
    }

    private String showCategoryHelp() {
        return "Справка по управлению категориями\n\n" +
                "Добавление категории:\n" +
                "• Создавайте категории для группировки доходов/расходов\n" +
                "• Название должно быть понятным (максимум 30 символов)\n" +
                "• Новые категории начинаются с баланса 0 руб\n\n" +
                "Удаление категории:\n" +
                "• Удалять можно только категории с нулевым балансом\n" +
                "• Базовые категории удалить нельзя\n" +
                "• Перед удалением обнулите баланс\n\n" +
                "Совет: Регулярно проверяйте категории и удаляйте неиспользуемые";
    }

    private String showAddCategoryHelp() {
        return "Как добавить категорию:\n\n" +
                "1. Нажмите \"Добавить категорию\"\n" +
                "2. Введите название новой категории\n" +
                "3. Категория будет создана с балансом 0 руб\n\n" +
                "Примеры хороших названий:\n" +
                "• Для доходов: Фриланс, Дивиденды, Сдача в аренду\n" +
                "• Для расходов: Развлечения, Образование, Красота\n\n" +
                "Чего избегать:\n" +
                "• Слишком общих названий (Другое, Прочее)\n" +
                "• Длинных названий (более 30 символов)\n" +
                "• Дублирования существующих категорий";
    }

    private String showRemoveCategoryHelp() {
        return "Как удалить категорию:\n\n" +
                "Предварительные шаги:\n" +
                "1. Убедитесь, что баланс категории равен 0 руб\n" +
                "2. Для обнуления баланса перейдите в меню \"" + getTypeName() + "\" → \"Удалить\"\n" +
                "3. Удалите всю сумму из категории\n\n" +
                "Удаление категории:\n" +
                "1. Нажмите \"Удалить категорию\"\n" +
                "2. Введите название категории\n" +
                "3. Если баланс = 0, категория будет удалена\n\n" +
                "Почему нельзя удалить категорию с балансом:\n" +
                "• Это исказит статистику\n" +
                "• Нельзя будет восстановить данные\n" +
                "• Нарушится целостность учета\n\n" +
                "Альтернатива: Можно просто не использовать категорию вместо удаления";
    }

    private String handleTextInput(String input) {
        if (currentSubState == SubState.ADD_CATEGORY) {//состояние добавления категории
            return addCategory(input);
        }
        if (currentSubState == SubState.REMOVE_CATEGORY) { // состояние удаления категории
            return removeCategory(input);
        }
        if (currentSubState == SubState.AMOUNT_INPUT && selectedCategory != null && currentOperation != null) {
            return processAmount(input);
        }
        if (currentSubState == SubState.CATEGORY_SELECTION) {
            String categoryName = input.trim();
            if (categories.containsKey(categoryName)) {
                selectedCategory = categoryName;
                currentSubState = SubState.AMOUNT_INPUT;
                String operationType = "add".equals(currentOperation) ? "добавления" : "удаления";
                return MessageFormat.format(
                        "Введите сумму для {0} в категории \"{1}\":",
                        operationType, selectedCategory
                );
            } else {
                return "Категория \"" + categoryName + "\" не найдена.\n" +
                        "Хотите создать новую категорию?\n" +
                        "1. Да - создать категорию \"" + categoryName + "\"\n" +
                        "2. Нет - вернуться к выбору категории\n\n" +
                        showCategorySelection();
            }
        }
        switch (input) {
            case "добавить":
            case "add":
                currentOperation = "add";
                currentSubState = SubState.CATEGORY_SELECTION;
                return showCategorySelectionWithInstructions();
            case "удалить":
            case "remove":
                currentOperation = "remove";
                currentSubState = SubState.CATEGORY_SELECTION;
                return showCategorySelectionWithInstructions();
            case "статистика":
            case "stats":
            case "statistics":
                currentSubState = SubState.MAIN_MENU;
                return showStatistics();
            case "категории":
            case "categories":
            case "управление категориями":
                currentSubState = SubState.CATEGORY_MANAGEMENT;
                return showCategoryManagement();
            case "меню":
            case "menu":
                currentSubState = SubState.MAIN_MENU;
                return onEnter();
            case "да":
            case "yes":

                if (tempCategoryName != null && !tempCategoryName.isEmpty()) {// создание новой категории(спасите дядя) из состояния CATEGORY_SELECTION
                    String result = addCategory(tempCategoryName);
                    tempCategoryName = null;
                    return result;
                }
                break;

            case "нет":
            case "no":
                currentSubState = SubState.CATEGORY_SELECTION;// выбор категории
                tempCategoryName = null;
                return showCategorySelection();

            default:

                if (categories.containsKey(input)) {// проверка на адекватность,если категорию назвали кнопкой из меню
                    currentOperation = "add";
                    currentSubState = SubState.CATEGORY_SELECTION;
                    selectedCategory = input;
                    currentSubState = SubState.AMOUNT_INPUT;
                    return MessageFormat.format(
                            "Введите сумму для добавления в категорию \"{0}\":",
                            selectedCategory
                    );
                }

                return messageProvider.getMessage("finance.error.unknown") + "\n\n" + getCurrentStateMessage();
        }

        return getCurrentStateMessage();
    }

    private String showCategorySelectionWithInstructions() {
        StringBuilder sb = new StringBuilder();
        String operationType = "add".equals(currentOperation) ? "добавления" : "удаления";
        String typeName = getTypeName();

        sb.append(" ").append(operationType.substring(0, 1).toUpperCase())
                .append(operationType.substring(1)).append(" ").append(typeName).append("\n\n");

        sb.append("Выберите категорию:\n\n");

        int i = 1;
        for (String category : categories.keySet()) {
            double amount = categories.getOrDefault(category, 0.0);
            sb.append(i).append(". ").append(category)
                    .append(": ").append(String.format("%.2f", amount)).append(" руб\n");
            i++;
        }

        sb.append("\n Введите название новой категории текстом");

        if ("remove".equals(currentOperation)) {
            sb.append("\n Можно удалить только доступную сумму из категории");
        }

        return sb.toString();
    }
    private String showCategoryList() {
        StringBuilder list = new StringBuilder();
        int i = 1;
        for (String category : categories.keySet()) {
            list.append(i).append(". ").append(category).append("\n");
            i++;
        }
        return list.toString();
    }

    private String handleBackButton() {
        switch (currentSubState) {
            case ADD_CATEGORY:
            case REMOVE_CATEGORY:
                currentSubState = SubState.CATEGORY_MANAGEMENT;
                return showCategoryManagement();

            case CATEGORY_MANAGEMENT:
                currentSubState = SubState.MAIN_MENU;
                return onEnter();

            case AMOUNT_INPUT:
                currentSubState = SubState.CATEGORY_SELECTION;
                selectedCategory = null;
                return showCategorySelection();

            case CATEGORY_SELECTION:
                currentSubState = SubState.MAIN_MENU;
                currentOperation = null;
                selectedCategory = null;
                return onEnter();

            case MAIN_MENU:
            default:
                return onEnter();
        }
    }

    private String getCurrentStateMessage() {
        switch (currentSubState) {
            case MAIN_MENU:
                return onEnter();
            case CATEGORY_SELECTION:
                return showCategorySelectionWithInstructions();
            case AMOUNT_INPUT:
                String operationType = "add".equals(currentOperation) ? "добавления" : "удаления";
                return MessageFormat.format(
                        "Введите сумму для {0} в категории \"{1}\":",
                        operationType, selectedCategory
                );
            case CATEGORY_MANAGEMENT:
                return showCategoryManagement();
            case ADD_CATEGORY:
                return "Введите название новой категории:";
            case REMOVE_CATEGORY:
                return showCategorySelection() + "\n\nВведите название категории для удаления:";
            default:
                return onEnter();
        }
    }

    private String showCategoryManagement() {
        StringBuilder sb = new StringBuilder();
        sb.append("Управление категориями ").append(getTypeName()).append("\n\n");

        sb.append("Здесь вы можете:\n");
        sb.append("• Добавить новые категории\n");
        sb.append("• Удалить ненужные категории\n");
        sb.append("• Просмотреть список всех категорий\n\n");
        sb.append("\nВАЖНЫЕ ПРАВИЛА:\n");
        sb.append("1. НЕЛЬЗЯ удалить категорию с деньгами(помечены ⚠️ )\n");
        sb.append("2. НЕЛЬЗЯ удалить базовые категории\n");
        sb.append("3. МОЖНО удалить только пустые пользовательские категории\n\n");
        sb.append("Как удалить категорию:\n");
        sb.append("1. Перейдите в меню \"").append(getTypeName()).append("\" → \"Удалить\"\n");
        sb.append("2. Удалите ВСЮ сумму из категории\n");
        sb.append("3. Вернитесь сюда и удалите пустую категорию\n\n");
        sb.append("Текущие категории:\n");

        int i = 1;
        boolean hasNonZeroCategories = false;

        for (String category : categories.keySet()) {
            double amount = categories.getOrDefault(category, 0.0);
            String baseMarker = getBaseCategories().contains(category) ? " (базовая)" : "";
            String balanceMarker = amount > 0 ? " ⚠️" : "";

            if (amount > 0 && !getBaseCategories().contains(category)) {
                hasNonZeroCategories = true;
            }

            sb.append(i).append(". ").append(category).append(baseMarker).append(balanceMarker)
                    .append(": ").append(String.format("%.2f", amount)).append(" руб\n");
            i++;
        }

        if (hasNonZeroCategories) {
            sb.append("\n⚠️ Категории с пометкой ⚠️ имеют ненулевой баланс.\n");
            sb.append("Перед удалением необходимо обнулить баланс через меню \"")
                    .append(getTypeName()).append("\" → \"Удалить\"");
        }

        return sb.toString();
    }
    private String showCategorySelection() {
        StringBuilder categoriesMessage = new StringBuilder();
        String operationType = getTypeName().equals("доходов") ? "доходов" : "расходов";

        categoriesMessage.append("Доступные категории ").append(operationType).append(":\n\n");

        int i = 1;
        for (String category : categories.keySet()) {
            double amount = categories.getOrDefault(category, 0.0);
            categoriesMessage.append(i).append(". ").append(category)
                    .append(": ").append(String.format("%.2f", amount)).append(" руб\n");
            i++;
        }

        return categoriesMessage.toString();
    }

    private String addCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "Название категории не может быть пустым";
        }

        String trimmedName = categoryName.trim();

        if (categories.containsKey(trimmedName)) {
            return MessageFormat.format(
                    "Категория \"{0}\" уже существует\n\n" +
                            "Используйте существующую категорию или придумайте другое название",
                    trimmedName
            );
        }

        if (trimmedName.length() > 30) {
            return "Название категории не должно превышать 30 символов";
        }

        categories.put(trimmedName, 0.0);
        saveUserCategories();

        currentSubState = SubState.CATEGORY_MANAGEMENT;

        return MessageFormat.format(
                "Категория \"{0}\" успешно добавлена!\n\n" +
                        "Теперь вы можете:\n" +
                        "• Добавлять {1} в эту категорию\n" +
                        "• Просматривать статистику по этой категории\n" +
                        "• Удалить категорию (если баланс равен 0)\n\n",
                trimmedName, getTypeName()
        ) + showCategoryManagement();
    }

    private String removeCategory(String categoryName) {
        String trimmedName = categoryName.trim();

        if (!categories.containsKey(trimmedName)) {
            return MessageFormat.format(
                    "Категория \"{0}\" не найдена",
                    trimmedName
            );
        }

        if (getBaseCategories().contains(trimmedName)) {
            return MessageFormat.format(
                    "Категория \"{0}\" является базовой и не может быть удалена\n\n" +
                            "Базовые категории: {1}",
                    trimmedName, String.join(", ", getBaseCategories())
            );
        }

        double currentBalance = categories.get(trimmedName);
        if (currentBalance > 0) {
            return MessageFormat.format(
                    "Нельзя удалить категорию \"{0}\" с балансом {1} руб.\n\n" +
                            "Сначала необходимо обнулить баланс одним из способов:\n" +
                            "1. Перейдите в меню \"{2}\" → \"Удалить\" и удалите {3} руб из категории \"{0}\"\n" +
                            "2. Добавьте расход/доход в другую категорию на сумму {3} руб, чтобы скомпенсировать баланс\n\n" +
                            "Предупреждение: Удаление категории с ненулевым балансом может исказить статистику!",
                    trimmedName,
                    String.format("%.2f", currentBalance),
                    getTypeName(),
                    String.format("%.2f", currentBalance)
            );
        }

        categories.remove(trimmedName);
        saveUserCategories();

        currentSubState = SubState.CATEGORY_MANAGEMENT;

        return MessageFormat.format(
                "Категория \"{0}\" успешно удалена\n\n",
                trimmedName
        ) + showCategoryManagement();
    }

    private String processAmount(String input) {
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                return "Сумма должна быть положительной\n\n" + getCurrentStateMessage();
            }
            return executeFinancialOperation(amount);
        } catch (NumberFormatException e) {
            return "Неверный формат суммы\n\n" + getCurrentStateMessage();
        }
    }

    private String executeFinancialOperation(double amount) {
        String result;
        double current = categories.getOrDefault(selectedCategory, 0.0);

        if ("add".equals(currentOperation)) {
            categories.put(selectedCategory, current + amount);
            result = MessageFormat.format(
                    "Добавлено {0} руб в категорию \"{1}\"\n" +
                            "Новый баланс: {2} руб",
                    String.format("%.2f", amount),
                    selectedCategory,
                    String.format("%.2f", current + amount)
            );
        } else {
            if (amount > current) {
                result = MessageFormat.format(
                        "Недостаточно средств для удаления\n" +
                                "Доступно для удаления: {0} руб\n" +
                                "Текущий баланс категории \"{1}\": {2} руб",
                        String.format("%.2f", current),
                        selectedCategory,
                        String.format("%.2f", current)
                );
            } else {
                double newBalance = current - amount;
                categories.put(selectedCategory, newBalance);

                String categoryManagementHint = "";
                if (newBalance == 0 && !getBaseCategories().contains(selectedCategory)) {
                    categoryManagementHint = "\n\nТеперь категория \"" + selectedCategory +
                            "\" имеет нулевой баланс и может быть удалена в меню \"Управление категориями\"";
                }

                result = MessageFormat.format(
                        "Удалено {0} руб из категории \"{1}\"\n" +
                                "Новый баланс: {2} руб{3}",
                        String.format("%.2f", amount),
                        selectedCategory,
                        String.format("%.2f", newBalance),
                        categoryManagementHint
                );
            }
        }

        saveUserCategories();
        resetOperation();
        return result + "\n\n" + onEnter();
    }

    private String showStatistics() {
        if (categories.values().stream().allMatch(amount -> amount == 0)) {
            return "Нет данных для отображения статистики\n\n" + onEnter();
        }

        StringBuilder stats = new StringBuilder();

        stats.append("Статистика ").append(getTypeName()).append("\n\n");

        List<Map.Entry<String, Double>> nonZeroEntries = new ArrayList<>();// Собираем и сортируем ненулевые категории
        double total = 0;

        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            if (entry.getValue() > 0) {
                nonZeroEntries.add(entry);
                total += entry.getValue();
            }
        }


        nonZeroEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));


        for (Map.Entry<String, Double> entry : nonZeroEntries) {
            double percentage = total > 0 ? (entry.getValue() / total) * 100 : 100;
            stats.append("• ").append(entry.getKey())
                    .append(": ").append(String.format("%.2f", entry.getValue()))
                    .append(" руб (").append(String.format("%.1f", percentage)).append("%)\n");
        }

        stats.append("\nИтого: ").append(String.format("%.2f", total)).append(" руб");

        return stats.toString();
    }

    public String getChartPath() {
        try {
            Map<String, Double> chartData = new HashMap<>();
            for (Map.Entry<String, Double> entry : categories.entrySet()) {
                if (entry.getValue() > 0) {
                    chartData.put(entry.getKey(), entry.getValue());
                }
            }

            if (chartData.isEmpty()) {
                return null;
            }

            String title = getTypeName().equals("доходов") ?
                    "Диаграмма доходов" : "Диаграмма расходов";

            String chartPath = ChartGenerator.generatePieChart(chartData, title, userId);

            ChartGenerator.cleanupOldCharts(userId);

            return chartPath;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void resetOperation() {
        currentOperation = null;
        selectedCategory = null;
        tempCategoryName = null;
        currentSubState = SubState.MAIN_MENU;
    }
}