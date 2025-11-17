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

    public FinanceState() {
        this.messageProvider = new AggregatorMessage();
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
    public DialogState goNextState(DialogContext context) {
        return context.hasNextState() ? context.getNextState() : this;
    }
    @Override
    public String userRequest(DialogContext context) {
        String input = context.getUserInput().toLowerCase().trim();

        // Сначала проверяем универсальные команды
        Optional<String> universalResult = UniversalCommand.executeCommand(input, context, messageProvider);
        if (universalResult.isPresent()) {
            return universalResult.get();
        }

        // 1. Если выбрана категория - обрабатываем сумму
        if (selectedCategory != null && currentOperation != null) {
            return processAmount(input);
        }

        // 2. Если выбрана операция - показываем категории
        if (currentOperation != null) {
            return processCategorySelection(input);
        }

        // 3. Обрабатываем выбор операции из меню
        return FinanceOperation.fromInput(input)
                .map(operation -> operation.execute(this, context))
                .orElse(messageProvider.getMessage("finance.error.unknown") + "\n\n" + buildMainMenu());
    }

    private enum FinanceOperation {
        ADD("1", "добавить") {
            @Override
            public String execute(FinanceState state, DialogContext context) {
                state.currentOperation = "add";
                return state.showCategorySelection();
            }
        },

        REMOVE("2", "удалить") {
            @Override
            public String execute(FinanceState state, DialogContext context) {
                state.currentOperation = "remove";
                return state.showCategorySelection();
            }
        },

        STATISTICS("3", "статистика", "просмотр") {
            @Override
            public String execute(FinanceState state, DialogContext context) {
                return state.showStatistics();
            }
        },

        BACK("4", "назад") {
            @Override
            public String execute(FinanceState state, DialogContext context) {
                context.setNextState(new MainState());
                return "";
            }
        };

        private final Set<String> aliases;

        FinanceOperation(String... aliases) {
            this.aliases = Set.of(aliases);
        }

        public abstract String execute(FinanceState state, DialogContext context);

        public static Optional<FinanceOperation> fromInput(String input) {
            return Arrays.stream(values())
                    .filter(operation -> operation.aliases.contains(input))
                    .findFirst();
        }
    }

    // Остальные методы остаются без изменений
    private String buildMainMenu() {
        return messageProvider.getMessage(getMenuMessageKey());
    }

    private String showCategorySelection() {
        StringBuilder categoriesMessage = new StringBuilder();

        String operationType = "add".equals(currentOperation) ? "дохода" : "расхода";
        String header = MessageFormat.format(
                messageProvider.getMessage("finance.category.selection.header"),
                operationType
        );
        categoriesMessage.append(header).append("\n\n");

        List<String> categoryList = new ArrayList<>(categories.keySet());
        for (int i = 0; i < categoryList.size(); i++) {
            String category = categoryList.get(i);
            double amount = categories.getOrDefault(category, 0.0);
            categoriesMessage.append(i + 1).append(". ").append(category);
            categoriesMessage.append(" (").append(amount).append(" руб.)\n");
        }

        categoriesMessage.append("\n").append(categoryList.size() + 1).append(". ")
                .append(messageProvider.getMessage("finance.category.cancel"));
        categoriesMessage.append("\n\n").append(messageProvider.getMessage("finance.category.prompt"));

        return categoriesMessage.toString();
    }

    private String processCategorySelection(String input) {
        try {
            int choice = Integer.parseInt(input);
            List<String> categoryList = new ArrayList<>(categories.keySet());

            if (choice == categoryList.size() + 1) {
                resetOperation();
                return messageProvider.getMessage("finance.operation.cancelled") + "\n\n" + buildMainMenu();
            }

            if (choice >= 1 && choice <= categoryList.size()) {
                selectedCategory = categoryList.get(choice - 1);
                String operationType = "add".equals(currentOperation) ? "добавления" : "удаления";

                return MessageFormat.format(
                        messageProvider.getMessage("finance.operation.amount.prompt"),
                        operationType,
                        selectedCategory
                );
            } else {
                return messageProvider.getMessage("finance.error.invalid.category") + "\n\n" + showCategorySelection();
            }
        } catch (NumberFormatException e) {
            return messageProvider.getMessage("finance.error.invalid.number") + "\n\n" + showCategorySelection();
        }
    }

    private String processAmount(String input) {
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                return messageProvider.getMessage("finance.error.positive.sum");
            }

            return executeFinancialOperation(amount);

        } catch (NumberFormatException e) {
            return messageProvider.getMessage("finance.error.invalid.sum");
        }
    }

    private String executeFinancialOperation(double amount) {
        String result;

        if ("add".equals(currentOperation)) {
            result = MessageFormat.format(
                    messageProvider.getMessage("finance.operation.added"),
                    amount,
                    selectedCategory
            );
            categories.put(selectedCategory, categories.getOrDefault(selectedCategory, 0.0) + amount);
        } else {
            double current = categories.getOrDefault(selectedCategory, 0.0);
            if (amount > current) {
                result = MessageFormat.format(
                        messageProvider.getMessage("finance.operation.insufficient"),
                        current
                );
            } else {
                result = MessageFormat.format(
                        messageProvider.getMessage("finance.operation.removed"),
                        amount,
                        selectedCategory
                );
                categories.put(selectedCategory, current - amount);
            }
        }

        resetOperation();
        return result + "\n\n" + buildMainMenu();
    }

    private String showStatistics() {
        if (categories.isEmpty()) {
            return messageProvider.getMessage("finance.statistics.empty") + "\n\n" + buildMainMenu();
        }
        List<String> categoryItems = new ArrayList<>();
        double total = 0;

        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            if (entry.getValue() > 0) {
                String itemTemplate = messageProvider.getMessage("finance.statistics.item");
                String categoryItem = MessageFormat.format(itemTemplate, entry.getKey(), entry.getValue());
                categoryItems.add(categoryItem);
                total += entry.getValue();
            }
        }

        if (categoryItems.isEmpty()) {
            return messageProvider.getMessage("finance.statistics.empty") + "\n\n" + buildMainMenu();
        }

        String categoriesText = String.join("\n", categoryItems);
        String statisticsTemplate = messageProvider.getMessage(
                getTypeName().equals("доходов") ? "finance.statistics.income" : "finance.statistics.expense"
        );
        String statisticsMessage = MessageFormat.format(statisticsTemplate, categoriesText, total);

        return statisticsMessage + "\n\n" + buildMainMenu();
    }

    private void resetOperation() {
        currentOperation = null;
        selectedCategory = null;
    }
}