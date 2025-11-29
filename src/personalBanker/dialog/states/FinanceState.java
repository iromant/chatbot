package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.AggregatorMessage;

import java.util.*;
import java.text.MessageFormat;

public abstract class FinanceState implements DialogState {
    protected final AggregatorMessage messageProvider;
    protected final Map<String, Double> categories;
    protected String currentOperation;
    protected String selectedCategory;

    // Enum для подсостояний
    protected enum SubState {
        MAIN_MENU,          // Главное меню доходов/расходов
        CATEGORY_SELECTION, // Выбор категории
        AMOUNT_INPUT        // Ввод суммы
    }

    protected SubState currentSubState;

    public FinanceState() {
        this.messageProvider = new AggregatorMessage();
        this.categories = new HashMap<>();
        this.currentOperation = null;
        this.selectedCategory = null;
        this.currentSubState = SubState.MAIN_MENU;
        initializeCategories();
    }

    protected abstract void initializeCategories();
    protected abstract String getMenuMessageKey();
    protected abstract String getTypeName();

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

        // Обработка кнопки "Назад"
        if ((input.toLowerCase().equals("назад") || input.equals("BACK")) &&
                currentSubState != SubState.MAIN_MENU) {
            return handleBackButton();
        } else if (input.toLowerCase().startsWith("доход") ||
                input.toLowerCase().startsWith("расход")) {
            return onEnter();
        }

        // Сначала обрабатываем универсальные команды
        Optional<String> universalResult = UniversalCommand.executeCommand(input, context);
        if (universalResult.isPresent()) {
            String result = universalResult.get();
            if (!result.isEmpty()) {
                return result;
            }
            // Если результат пустой - команда обработана, но нужно остаться в текущем состоянии
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

        // Обработка текстового ввода для финансовых операций
        return handleTextInput(input.toLowerCase().trim());
    }

    private String handleCallback(String callbackData) {
        switch (callbackData) {
            case "INCOME_ADD":
            case "EXPENSE_ADD":
                currentOperation = "add";
                currentSubState = SubState.CATEGORY_SELECTION;
                return showCategorySelection();

            case "INCOME_REMOVE":
            case "EXPENSE_REMOVE":
                currentOperation = "remove";
                currentSubState = SubState.CATEGORY_SELECTION;
                return showCategorySelection();

            case "INCOME_STATS":
            case "EXPENSE_STATS":
                currentSubState = SubState.MAIN_MENU;
                return showStatistics();

            default:
                if (callbackData.startsWith("CATEGORY_")) {
                    String category = callbackData.replace("CATEGORY_", "")
                            .replace("INCOME_", "")
                            .replace("EXPENSE_", "");
                    selectedCategory = category;
                    currentSubState = SubState.AMOUNT_INPUT;
                    String operationType = "add".equals(currentOperation) ? "добавления" : "удаления";
                    return MessageFormat.format(
                            messageProvider.getMessage("finance.operation.amount.prompt"),
                            operationType, selectedCategory
                    );
                }
                return messageProvider.getMessage("finance.error.unknown");
        }
    }

    private String handleTextInput(String input) {
        // Если находимся в состоянии ввода суммы - обрабатываем сумму
        if (currentSubState == SubState.AMOUNT_INPUT && selectedCategory != null && currentOperation != null) {
            return processAmount(input);
        }

        // Обработка операций в главном меню
        switch (input) {
            case "добавить":
            case "add":
                currentOperation = "add";
                currentSubState = SubState.CATEGORY_SELECTION;
                return showCategorySelection();

            case "удалить":
            case "remove":
                currentOperation = "remove";
                currentSubState = SubState.CATEGORY_SELECTION;
                return showCategorySelection();

            case "статистика":
            case "stats":
            case "statistics":
                currentSubState = SubState.MAIN_MENU;
                return showStatistics();

            case "меню":
            case "menu":
                currentSubState = SubState.MAIN_MENU;
                return onEnter();

            default:
                return messageProvider.getMessage("finance.error.unknown") + "\n\n" + getCurrentStateMessage();
        }
    }

    private String handleBackButton() {
        switch (currentSubState) {
            case AMOUNT_INPUT:
                // Если вводим сумму - возвращаемся к выбору категории
                currentSubState = SubState.CATEGORY_SELECTION;
                selectedCategory = null;
                return showCategorySelection();

            case CATEGORY_SELECTION:
                // Если выбираем категорию - возвращаемся в главное меню доходов/расходов
                currentSubState = SubState.MAIN_MENU;
                currentOperation = null;
                selectedCategory = null;
                return onEnter();

            case MAIN_MENU:
            default:
                // Если уже в главном меню - остаемся здесь
                return onEnter();
        }
    }

    private String getCurrentStateMessage() {
        switch (currentSubState) {
            case MAIN_MENU:
                return onEnter();
            case CATEGORY_SELECTION:
                return showCategorySelection();
            case AMOUNT_INPUT:
                String operationType = "add".equals(currentOperation) ? "добавления" : "удаления";
                return MessageFormat.format(
                        messageProvider.getMessage("finance.operation.amount.prompt"),
                        operationType, selectedCategory
                );
            default:
                return onEnter();
        }
    }

    private String showCategorySelection() {
        StringBuilder categoriesMessage = new StringBuilder();
        String operationType = getTypeName().equals("доходов") ? "доходов" : "расходов";

        categoriesMessage.append(MessageFormat.format(
                messageProvider.getMessage("finance.category.selection.header"),
                operationType
        )).append("\n\n");

        for (String category : categories.keySet()) {
            double amount = categories.getOrDefault(category, 0.0);
            categoriesMessage.append("• ").append(category).append(": ").append(amount).append(" руб\n");
        }

        return categoriesMessage.toString();
    }

    private String processAmount(String input) {
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                return messageProvider.getMessage("finance.error.positive.sum") + "\n\n" + getCurrentStateMessage();
            }
            return executeFinancialOperation(amount);
        } catch (NumberFormatException e) {
            return messageProvider.getMessage("finance.error.invalid.sum") + "\n\n" + getCurrentStateMessage();
        }
    }

    private String executeFinancialOperation(double amount) {
        String result;
        double current = categories.getOrDefault(selectedCategory, 0.0);

        if ("add".equals(currentOperation)) {
            categories.put(selectedCategory, current + amount);
            result = MessageFormat.format(
                    messageProvider.getMessage("finance.operation.added"),
                    amount, selectedCategory
            );
        } else {
            if (amount > current) {
                result = MessageFormat.format(
                        messageProvider.getMessage("finance.operation.insufficient"),
                        current
                );
            } else {
                categories.put(selectedCategory, current - amount);
                result = MessageFormat.format(
                        messageProvider.getMessage("finance.operation.removed"),
                        amount, selectedCategory
                );
            }
        }

        // После операции возвращаемся в главное меню
        resetOperation();
        return result + "\n\n" + onEnter();
    }

    private String showStatistics() {
        if (categories.values().stream().allMatch(amount -> amount == 0)) {
            return messageProvider.getMessage("finance.statistics.empty") + "\n\n" + onEnter();
        }

        StringBuilder stats = new StringBuilder();
        double total = 0;

        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            if (entry.getValue() > 0) {
                String item = MessageFormat.format(
                        messageProvider.getMessage("finance.statistics.item"),
                        entry.getKey(), entry.getValue()
                );
                stats.append(item).append("\n");
                total += entry.getValue();
            }
        }

        String templateKey = getTypeName().equals("доходов") ?
                "finance.statistics.income" : "finance.statistics.expense";

        return MessageFormat.format(
                messageProvider.getMessage(templateKey),
                stats.toString(), total
        ) + "\n\n" + onEnter();
    }

    private void resetOperation() {
        currentOperation = null;
        selectedCategory = null;
        currentSubState = SubState.MAIN_MENU;
    }

}