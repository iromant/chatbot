package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.AggregatorMessage;
import personalBanker.dialog.storage.UserCategoryStorage;

import java.util.*;
import java.text.MessageFormat;

public abstract class FinanceState implements DialogState {
    protected final AggregatorMessage messageProvider;
    protected final Map<String, Double> categories;
    protected final Map<String, Double> limitsGoals;
    protected String currentOperation;
    protected String selectedCategory;
    protected final Long userId;

    protected enum SubState {
        MAIN_MENU,
        CATEGORY_SELECTION,
        AMOUNT_INPUT,
        CATEGORY_MANAGEMENT,
        ADD_CATEGORY,
        REMOVE_CATEGORY,
        SET_LIMIT_GOAL,
        CONFIRM_LIMIT_GOAL
    }

    protected SubState currentSubState;
    protected String tempCategoryName;
    protected Double tempAmount;

    public FinanceState(Long userId) {
        this.messageProvider = new AggregatorMessage();
        this.categories = new LinkedHashMap<>();
        this.limitsGoals = new HashMap<>();
        this.currentOperation = null;
        this.selectedCategory = null;
        this.currentSubState = SubState.MAIN_MENU;
        this.userId = userId;
        this.tempCategoryName = null;
        this.tempAmount = null;
        initializeCategories();
        loadUserData();
    }

    protected abstract void initializeCategories();
    protected abstract String getMenuMessageKey();
    public abstract String getTypeName();
    public abstract Set<String> getBaseCategories();
    public abstract Map<String, Double> getCategoriesMap();
    public abstract boolean isIncome();

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
            return showCategorySelectionForRemoval();
        }

        if (input.equals("GOALS") || input.equals("SET_LIMIT_GOAL")) {
            currentSubState = SubState.SET_LIMIT_GOAL;
            return showCategorySelectionForLimitsGoals();
        }

        // Обработка кнопки назад ВСЕГДА (без условий)
        if (input.equalsIgnoreCase("назад") || input.equals("BACK")) {
            return handleBackButton();
        }

        Optional<String> universalResult = UniversalCommand.executeCommand(input, context);
        if (universalResult.isPresent()) {
            String result = universalResult.get();
            if (!result.isEmpty()) {
                return result;
            }
            return getCurrentStateMessage();
        }

        return handleFinancialInput(input);
    }

    @Override
    public String getCurrentSubState() {
        return currentSubState.name();
    }

    private String handleFinancialInput(String input) {
        if (input.startsWith("INCOME_") || input.startsWith("EXPENSE_")
                || input.startsWith("CATEGORY_") || input.equals("YES") || input.equals("NO")
                || input.startsWith("SET_LIMIT_FOR_")) {
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
            String canDelete = amount == 0 && !getBaseCategories().contains(category) ? " " : "";

            sb.append(i).append(". ").append(category).append(baseMarker).append(canDelete)
                    .append(": ").append(String.format("%.2f", amount)).append(" руб\n");
            i++;
        }

        return sb.toString();
    }

    private String handleCallback(String callbackData) {
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

            case "GOALS":
            case "SET_LIMIT_GOAL":
                currentSubState = SubState.SET_LIMIT_GOAL;
                return showCategorySelectionForLimitsGoals();

            case "ADD_CATEGORY":
                currentSubState = SubState.ADD_CATEGORY;
                return messageProvider.getMessage("finance.add.category");

            case "REMOVE_CATEGORY":
                currentSubState = SubState.REMOVE_CATEGORY;
                return showCategorySelectionForRemoval();

            case "YES":
                if (tempCategoryName != null) {
                    String result = addCategory(tempCategoryName);
                    if (result.contains("успешно добавлена")) {
                        selectedCategory = tempCategoryName;
                        currentSubState = SubState.AMOUNT_INPUT;
                        return result + "\n\nВведите сумму для добавления:";
                    }
                    tempCategoryName = null;
                    return result;
                }
                break;

            case "NO":
                currentSubState = SubState.CATEGORY_SELECTION;
                tempCategoryName = null;
                return showCategorySelectionWithInstructions();

            default:
                if (callbackData.startsWith("SET_LIMIT_FOR_")) {
                    String categoryName = callbackData.substring("SET_LIMIT_FOR_".length());
                    if (categories.containsKey(categoryName)) {
                        selectedCategory = categoryName;
                        currentSubState = SubState.CONFIRM_LIMIT_GOAL;
                        return getLimitGoalInputMessage(categoryName);
                    }
                } else if (callbackData.startsWith("CATEGORY_")) {
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
                            Double limitGoal = limitsGoals.get(selectedCategory);

                            String limitGoalText = "";
                            if (limitGoal != null) {
                                limitGoalText = MessageFormat.format(
                                        "\n{0}: {1} руб",
                                        isIncome() ? "Цель" : "Лимит",
                                        String.format("%.2f", limitGoal)
                                );
                            }

                            return MessageFormat.format(
                                    messageProvider.getMessage("finance.input.amount"),
                                    selectedCategory,
                                    String.format("%.2f", currentBalance),
                                    limitGoalText,
                                    operationType);
                        }
                    }
                }
                return messageProvider.getMessage("finance.error.unknown");
        }
        return getCurrentStateMessage();
    }

    private String handleTextInput(String input) {
        if (currentSubState == SubState.ADD_CATEGORY) {
            return addCategory(input);
        }
        if (currentSubState == SubState.REMOVE_CATEGORY) {
            return removeCategory(input);
        }
        if (currentSubState == SubState.AMOUNT_INPUT && selectedCategory != null && currentOperation != null) {
            return processAmount(input);
        }
        if (currentSubState == SubState.CONFIRM_LIMIT_GOAL && selectedCategory != null) {
            return processLimitGoalInput(input);
        }
        if (currentSubState == SubState.SET_LIMIT_GOAL) {
            selectedCategory = input.trim();
            if (categories.containsKey(selectedCategory)) {
                currentSubState = SubState.CONFIRM_LIMIT_GOAL;
                return getLimitGoalInputMessage(selectedCategory);
            } else {
                return "Категория \"" + selectedCategory + "\" не найдена. Попробуйте еще раз.";
            }
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
                tempCategoryName = categoryName;
                return MessageFormat.format(
                        messageProvider.getMessage("finance.category.not.found"),
                        categoryName);
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
                if (tempCategoryName != null && !tempCategoryName.isEmpty()) {
                    String result = addCategory(tempCategoryName);
                    if (result.contains("успешно добавлена")) {
                        selectedCategory = tempCategoryName;
                        currentSubState = SubState.AMOUNT_INPUT;
                        return result + "\n\nВведите сумму для добавления:";
                    }
                    tempCategoryName = null;
                    return result;
                }
                break;

            case "нет":
            case "no":
                currentSubState = SubState.CATEGORY_SELECTION;
                tempCategoryName = null;
                return showCategorySelectionWithInstructions();

            case "лимиты":
            case "цели":
            case "goals":
                currentSubState = SubState.SET_LIMIT_GOAL;
                return showCategorySelectionForLimitsGoals();

            default:
                if (categories.containsKey(input)) {
                    currentOperation = "add";
                    currentSubState = SubState.CATEGORY_SELECTION;
                    selectedCategory = input;
                    currentSubState = SubState.AMOUNT_INPUT;
                    return MessageFormat.format(
                            "Введите сумму для добавления в категорию \"{0}\":",
                            selectedCategory
                    );
                }

                return messageProvider.getMessage("finance.error.unknown");
        }

        return getCurrentStateMessage();
    }

    private String getLimitGoalInputMessage(String categoryName) {
        String type = isIncome() ? "цель" : "лимит";
        Double currentLimitGoal = limitsGoals.get(categoryName);
        double currentAmount = categories.getOrDefault(categoryName, 0.0);


        return MessageFormat.format(
                messageProvider.getMessage("finance.limit.goal.no"),
                type,
                categoryName);

    }

    private String processLimitGoalInput(String input) {
        if (input.trim().isEmpty()) {
            currentSubState = SubState.CATEGORY_MANAGEMENT;
            return "Отмена установки\n\n" + showCategoryManagement();
        }

        try {
            double amount = Double.parseDouble(input);

            if (amount < 0) {
                return "Сумма не может быть отрицательной. Попробуйте еще раз.";
            }

            String type = isIncome() ? "цель" : "лимит";

            if (amount == 0) {
                limitsGoals.remove(selectedCategory);
                saveUserData();
                currentSubState = SubState.CATEGORY_MANAGEMENT;

                return MessageFormat.format(
                        "✅ {0} для категории \"{1}\" удален\n\n",
                        type, selectedCategory
                ) + showCategoryManagement();
            } else {
                limitsGoals.put(selectedCategory, amount);
                saveUserData();
                currentSubState = SubState.CATEGORY_MANAGEMENT;

                double currentAmount = categories.getOrDefault(selectedCategory, 0.0);
                StringBuilder result = new StringBuilder();

                result.append(MessageFormat.format(
                        "✅ {0} для категории \"{1}\" есть: {2} руб\n\n",
                        type, selectedCategory, String.format("%.2f", amount)));

                if (isIncome()) {
                    if (currentAmount >= amount) {
                        result.append("🎉 ПОЗДРАВЛЯЕМ! ЦЕЛЬ ДОСТИГНУТА!\n");
                    } else {
                        double remaining = amount - currentAmount;
                        double percentage = (currentAmount / amount) * 100;
                        result.append(MessageFormat.format(
                                "📊 Прогресс: {0}% ({1} руб / {2} руб)\n" +
                                        "Осталось до цели: {3} руб\n",
                                String.format("%.1f", percentage),
                                String.format("%.2f", currentAmount),
                                String.format("%.2f", amount),
                                String.format("%.2f", remaining)));
                    }
                } else {
                    if (currentAmount > amount) {
                        double overspend = currentAmount - amount;
                        result.append(MessageFormat.format(
                                "⚠️ ВНИМАНИЕ! ЛИМИТ ПРЕВЫШЕН НА {0} РУБ!\n",
                                String.format("%.2f", overspend)));
                    } else {
                        double remaining = amount - currentAmount;
                        double percentage = (currentAmount / amount) * 100;
                        result.append(MessageFormat.format(
                                "📊 Использовано: {0}% ({1} руб / {2} руб)\n" +
                                        "Осталось в лимите: {3} руб\n",
                                String.format("%.1f", percentage),
                                String.format("%.2f", currentAmount),
                                String.format("%.2f", amount),
                                String.format("%.2f", remaining)));
                    }
                }

                return result + showCategoryManagement();
            }
        } catch (NumberFormatException e) {
            return "Неверный формат суммы. Попробуйте еще раз.";
        }
    }

    private String showCategorySelectionForLimitsGoals() {
        StringBuilder sb = new StringBuilder();
        String type = isIncome() ? "целей" : "лимитов";

        sb.append("🎯 Установка ").append(type).append("\n\n");
        sb.append("Выберите категорию:\n\n");

        int i = 1;
        for (String category : categories.keySet()) {
            double amount = categories.getOrDefault(category, 0.0);
            Double limitGoal = limitsGoals.get(category);
            String limitGoalText = limitGoal != null ?
                    String.format("%.2f", limitGoal) + " руб" : "не установлен";

            String emoji = "";
            if (limitGoal != null) {
                if (isIncome()) {
                    if (amount >= limitGoal) emoji = " 🎯";
                    else if (amount > 0 && (amount / limitGoal) >= 0.8) emoji = " ⏳";
                } else {
                    if (amount > limitGoal) emoji = " ⚠️";
                    else if (amount > 0 && (amount / limitGoal) >= 0.8) emoji = " ⚡";
                }
            }

            sb.append(i).append(". ").append(category)
                    .append(": ").append(String.format("%.2f", amount))
                    .append(" руб / ").append(limitGoalText).append(emoji).append("\n");
            i++;
        }

        return sb.toString();
    }

    private String handleBackButton() {
        switch (currentSubState) {
            case ADD_CATEGORY:
            case REMOVE_CATEGORY:
            case SET_LIMIT_GOAL:
            case CONFIRM_LIMIT_GOAL:
                currentSubState = SubState.CATEGORY_MANAGEMENT;
                return showCategoryManagement();

            case CATEGORY_MANAGEMENT:
                currentSubState = SubState.MAIN_MENU;
                return onEnter();

            case AMOUNT_INPUT:
                currentSubState = SubState.CATEGORY_SELECTION;
                selectedCategory = null;
                return showCategorySelectionWithInstructions();

            case CATEGORY_SELECTION:
                currentSubState = SubState.MAIN_MENU;
                currentOperation = null;
                selectedCategory = null;
                return onEnter();

            case MAIN_MENU:
                return onEnter();

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
                return showCategorySelectionForRemoval();
            case SET_LIMIT_GOAL:
                return showCategorySelectionForLimitsGoals();
            case CONFIRM_LIMIT_GOAL:
                return getLimitGoalInputMessage(selectedCategory);
            default:
                return onEnter();
        }
    }

    private String showCategoryManagement() {
        StringBuilder sb = new StringBuilder();
        sb.append("Управление категориями ").append(getTypeName()).append("\n\n");

        sb.append(messageProvider.getMessage("finance.categories.management"));

        int i = 1;
        boolean hasNonZeroCategories = false;

        for (String category : categories.keySet()) {
            double amount = categories.getOrDefault(category, 0.0);
            Double limitGoal = limitsGoals.get(category);

            String baseMarker = getBaseCategories().contains(category) ? " (базовая)" : "";
            String balanceMarker = amount > 0 ? " " : "";

            sb.append(i).append(". ").append(category).append(baseMarker).append(balanceMarker)
                    .append(": ").append(String.format("%.2f", amount));

            if (limitGoal != null) {
                sb.append(" / ").append(String.format("%.2f", limitGoal)).append(" руб");

                if (isIncome()) {
                    if (amount >= limitGoal) {
                        sb.append(" 🎯");
                    } else if (amount > 0) {
                        double percentage = (amount / limitGoal) * 100;
                        if (percentage >= 80) {
                            sb.append(" ⏳");
                        }
                    }
                } else {
                    if (amount > limitGoal) {
                        sb.append(" ⚠️");
                    } else if (amount > 0) {
                        double percentage = (amount / limitGoal) * 100;
                        if (percentage >= 80) {
                            sb.append(" ⚡");
                        }
                    }
                }
            }

            sb.append("\n");
            i++;

            if (amount > 0 && !getBaseCategories().contains(category)) {
                hasNonZeroCategories = true;
            }
        }

        if (hasNonZeroCategories) {
            sb.append("\nКатегории с пометкой  имеют ненулевой баланс.\n");
            sb.append("Перед удалением необходимо обнулить баланс через меню \"")
                    .append(getTypeName()).append("\" → \"Удалить\"");
        }

        return sb.toString();
    }

    private String showCategorySelectionWithInstructions() {
        StringBuilder sb = new StringBuilder();
        String typeName = getTypeName();

        sb.append("Выберите категорию ").append(typeName).append(":\n\n");

        int i = 1;
        for (String category : categories.keySet()) {
            double amount = categories.getOrDefault(category, 0.0);
            Double limitGoal = limitsGoals.get(category);

            sb.append(i).append(". ").append(category)
                    .append(": ").append(String.format("%.2f", amount));

            if (limitGoal != null) {
                sb.append(" / ").append(String.format("%.2f", limitGoal)).append(" руб");

                if (isIncome()) {
                    if (amount >= limitGoal) {
                        sb.append(" 🎯");
                    } else if (amount > 0 && (amount / limitGoal) >= 0.8) {
                        sb.append(" ⏳");
                    }
                } else {
                    if (amount > limitGoal) {
                        sb.append(" ⚠️");
                    } else if (amount > 0 && (amount / limitGoal) >= 0.8) {
                        sb.append(" ⚡");
                    }
                }
            }

            sb.append("\n");
            i++;
        }

        if ("remove".equals(currentOperation)) {
            sb.append("\nМожно удалить только доступную сумму из категории");
        }

        return sb.toString();
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
        saveUserData();

        currentSubState = SubState.CATEGORY_MANAGEMENT;

        return MessageFormat.format(
                "Категория \"{0}\" успешно добавлена!\n\n",
                trimmedName
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

        limitsGoals.remove(trimmedName);
        categories.remove(trimmedName);
        saveUserData();

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

    public String executeFinancialOperation(double amount) {
        String result = "";
        double current = categories.getOrDefault(selectedCategory, 0.0);

        if ("add".equals(currentOperation)) {
            categories.put(selectedCategory, current + amount);
            result = MessageFormat.format(
                    "✅ Добавлено {0} руб в категорию \"{1}\"\n" +
                            "Новый баланс: {2} руб",
                    String.format("%.2f", amount),
                    selectedCategory,
                    String.format("%.2f", current + amount)
            );

            result += checkLimitGoalAfterOperation(selectedCategory, current + amount);

        } else if ("remove".equals(currentOperation)) {
            if (amount > current) {
                return MessageFormat.format(messageProvider.getMessage("finance.insufficient.funds"),
                        String.format("%.2f", current),
                        selectedCategory,
                        String.format("%.2f", current));
            } else {
                double newBalance = current - amount;
                categories.put(selectedCategory, newBalance);

                result = MessageFormat.format(
                        "✅ Удалено {0} руб из категории \"{1}\"\n" +
                                "Новый баланс: {2} руб",
                        String.format("%.2f", amount),
                        selectedCategory,
                        String.format("%.2f", newBalance)
                );

                result += checkLimitGoalAfterOperation(selectedCategory, newBalance);
            }
        }

        saveUserData();
        resetOperation();
        return result + "\n\n" + onEnter();
    }

    private String checkLimitGoalAfterOperation(String category, double newAmount) {
        Double limitGoal = limitsGoals.get(category);
        if (limitGoal == null) {
            return "";
        }

        StringBuilder message = new StringBuilder("\n\n");

        if (isIncome()) {
            if (newAmount >= limitGoal) {
                message.append("🎉 ПОЗДРАВЛЯЕМ! ЦЕЛЬ ДОСТИГНУТА!\n");
                message.append("Вы собрали ").append(String.format("%.2f", newAmount))
                        .append(" руб при цели ").append(String.format("%.2f", limitGoal))
                        .append(" руб\n");
            } else {
                double percentage = (newAmount / limitGoal) * 100;
                double remaining = limitGoal - newAmount;
                message.append(MessageFormat.format(
                        "📊 Прогресс цели: {0}%\n" +
                                "Осталось до цели: {1} руб\n",
                        String.format("%.1f", percentage),
                        String.format("%.2f", remaining)
                ));

                if (percentage >= 80) {
                    message.append("⚡ Вы близки к достижению цели!\n");
                }
            }
        } else {
            if (newAmount > limitGoal) {
                double overspend = newAmount - limitGoal;
                message.append("⚠️ ВНИМАНИЕ! ЛИМИТ ПРЕВЫШЕН!\n");
                message.append(MessageFormat.format(
                        "Превышение: {0} руб\n" +
                                "Лимит: {1} руб, потрачено: {2} руб\n",
                        String.format("%.2f", overspend),
                        String.format("%.2f", limitGoal),
                        String.format("%.2f", newAmount)
                ));
            } else {
                double percentage = (newAmount / limitGoal) * 100;
                double remaining = limitGoal - newAmount;
                message.append(MessageFormat.format(
                        "📊 Использовано лимита: {0}%\n" +
                                "Осталось в лимите: {1} руб\n",
                        String.format("%.1f", percentage),
                        String.format("%.2f", remaining)
                ));

                if (percentage >= 80) {
                    message.append("⚡ Вы близки к исчерпанию лимита!\n");
                }
            }
        }

        return message.toString();
    }

    private String showStatistics() {
        if (categories.values().stream().allMatch(amount -> amount == 0)) {
            return "Нет данных для отображения статистики\n\n" + onEnter();
        }

        StringBuilder stats = new StringBuilder();

        stats.append("Статистика ").append(getTypeName()).append("\n\n");

        List<Map.Entry<String, Double>> nonZeroEntries = new ArrayList<>();
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
            Double limitGoal = limitsGoals.get(entry.getKey());

            stats.append("• ").append(entry.getKey())
                    .append(": ").append(String.format("%.2f", entry.getValue()))
                    .append(" руб");

            if (limitGoal != null) {
                stats.append(" / ").append(String.format("%.2f", limitGoal)).append(" руб");

                if (isIncome()) {
                    if (entry.getValue() >= limitGoal) {
                        stats.append(" 🎯");
                    }
                } else {
                    if (entry.getValue() > limitGoal) {
                        stats.append(" ⚠️");
                    }
                }
            }

            stats.append(" (").append(String.format("%.1f", percentage)).append("%)\n");
        }

        stats.append("\nИтого: ").append(String.format("%.2f", total)).append(" руб");

        return stats.toString();
    }

    public Map<String, Double> getChartData() {
        Map<String, Double> chartData = new HashMap<>();
        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            if (entry.getValue() > 0) {
                chartData.put(entry.getKey(), entry.getValue());
            }
        }
        return chartData;
    }

    protected void loadUserData() {
        // Загружаем суммы категорий
        Map<String, Double> loadedCategories = UserCategoryStorage.loadUserCategories(
                userId,
                isIncome() ? "income" : "expense"
        );

        for (Map.Entry<String, Double> entry : loadedCategories.entrySet()) {
            categories.put(entry.getKey(), entry.getValue());
        }

        // Загружаем лимиты/цели
        Map<String, Double> loadedLimitsGoals = UserCategoryStorage.loadLimitsGoals(
                userId,
                isIncome() ? "income" : "expense"
        );

        limitsGoals.putAll(loadedLimitsGoals);
    }

    protected void saveUserData() {
        Map<String, Double> allCategories = new HashMap<>(categories);


        UserCategoryStorage.saveUserCategoriesAndLimits(
                userId,
                isIncome() ? "income" : "expense",
                allCategories,
                limitsGoals
        );
    }

    private void resetOperation() {
        currentOperation = null;
        selectedCategory = null;
        tempCategoryName = null;
        tempAmount = null;
        currentSubState = SubState.MAIN_MENU;
    }
}