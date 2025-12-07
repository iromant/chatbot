package personalBanker.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

public class KeyboardManager {

    private static final Map<Long, Map<String, List<String>>> userCategoriesCache = new HashMap<>();

    public static void updateUserCategories(Long userId, String type, List<String> categories) {
        userCategoriesCache
                .computeIfAbsent(userId, k -> new HashMap<>())
                .put(type, categories);
    }

    public static List<String> getUserCategories(Long userId, String type) {
        return userCategoriesCache
                .getOrDefault(userId, new HashMap<>())
                .getOrDefault(type, new ArrayList<>());
    }

    public static InlineKeyboardMarkup getStartMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Меню", "MAIN_MENU"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Справка", "HELP"));

        keyboard.add(row1);
        keyboard.add(row2);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Доходы", "INCOME_MENU"));
        row1.add(createInlineButton("Расходы", "EXPENSE_MENU"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Справка", "HELP"));
        row2.add(createInlineButton("Удалить данные", "CLEAR_MY_DATA"));

        keyboard.add(row1);
        keyboard.add(row2);

        return new InlineKeyboardMarkup(keyboard);
    }
    public static InlineKeyboardMarkup getClearDataConfirmationKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Да, удалить всё", "CONFIRM_CLEAR_DATA"));
        row1.add(createInlineButton("Нет, отменить", "CANCEL_CLEAR_DATA"));

        keyboard.add(row1);
        return new InlineKeyboardMarkup(keyboard);
    }
    public static InlineKeyboardMarkup getIncomeMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Добавить", "INCOME_ADD"));
        row1.add(createInlineButton("Удалить", "INCOME_REMOVE"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Статистика", "INCOME_STATS"));
        row2.add(createInlineButton("Категории", "MANAGE_CATEGORIES"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton("Справка", "HELP"));
        row3.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getExpenseMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Добавить", "EXPENSE_ADD"));
        row1.add(createInlineButton("Удалить", "EXPENSE_REMOVE"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Статистика", "EXPENSE_STATS"));
        row2.add(createInlineButton("Категории", "MANAGE_CATEGORIES"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton("Справка", "HELP"));
        row3.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getDynamicIncomeCategoriesKeyboard(Long userId) {
        List<String> baseCategories = Arrays.asList("Работа", "Пассивный доход", "Инвестиции", "Подарки");
        List<String> userCategories = getUserCategories(userId, "income");

        return getSelectionCategoriesKeyboard(baseCategories, userCategories, "INCOME");
    }

    public static InlineKeyboardMarkup getDynamicExpenseCategoriesKeyboard(Long userId) {
        List<String> baseCategories = Arrays.asList("Еда", "Транспорт", "Жилье", "Досуг", "Здоровье");
        List<String> userCategories = getUserCategories(userId, "expense");

        return getSelectionCategoriesKeyboard(baseCategories, userCategories, "EXPENSE");
    }

    public static InlineKeyboardMarkup getAmountInputKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row);
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getHelpKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton("Назад", "BACK"));
        row.add(createInlineButton("Меню", "MAIN_MENU"));

        keyboard.add(row);
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getCategoryManagementKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Добавить", "ADD_CATEGORY"));
        row1.add(createInlineButton("Удалить", "REMOVE_CATEGORY"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getBackOnlyKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row);
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getYesNoKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton("Да", "YES"));
        row.add(createInlineButton("Нет", "NO"));

        keyboard.add(row);
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getResponseContextKeyboard(String currentState,
                                                                  String subState,
                                                                  String responseText,
                                                                  Long userId) {

        if (responseText.contains("Хотите создать новую категорию?")) {
            return getYesNoKeyboard();
        }

        if (responseText.contains("ВНИМАНИЕ") && responseText.contains("удалить ВСЕ ваши данные")) {
            return getClearDataConfirmationKeyboard();
        }

        if (responseText.contains("Управление категориями") ||
                responseText.contains("успешно добавлена") ||
                responseText.contains("успешно удалена")) {

            return getCategoryManagementKeyboard();
        }

        if (responseText.contains("Введите сумму") || responseText.contains("введите сумму")) {
            return getAmountInputKeyboard();
        }

        if ((responseText.contains("Выберите категорию") ||
                responseText.contains("Доступные категории") ||
                responseText.contains("Добавление") ||
                responseText.contains("Удаление")) &&
                !responseText.contains("Управление категориями")) {

            if ("IncomeState".equals(currentState)) {
                return getDynamicIncomeCategoriesKeyboard(userId);
            } else if ("ExpenseState".equals(currentState)) {
                return getDynamicExpenseCategoriesKeyboard(userId);
            }
        }

        if (responseText.contains("Введите название") ||
                responseText.contains("введите название")) {
            return getBackOnlyKeyboard();
        }

        return getResponseContextKeyboard(currentState, subState, userId);
    }

    public static InlineKeyboardMarkup getResponseContextKeyboard(String currentState,
                                                                  String subState,
                                                                  Long userId) {


        if (("ExpenseState".equals(currentState) || "IncomeState".equals(currentState))
                && "CATEGORY_MANAGEMENT".equals(subState)) {
            return getCategoryManagementKeyboard();
        }

        if (("ExpenseState".equals(currentState) || "IncomeState".equals(currentState))
                && ("ADD_CATEGORY".equals(subState) || "REMOVE_CATEGORY".equals(subState))) {
            return getBackOnlyKeyboard();
        }

        if (("ExpenseState".equals(currentState) || "IncomeState".equals(currentState))
                && "CATEGORY_SELECTION".equals(subState)) {

            if ("IncomeState".equals(currentState)) {
                return getDynamicIncomeCategoriesKeyboard(userId);
            } else {
                return getDynamicExpenseCategoriesKeyboard(userId);
            }
        }

        if (("ExpenseState".equals(currentState) || "IncomeState".equals(currentState))
                && "AMOUNT_INPUT".equals(subState)) {
            return getAmountInputKeyboard();
        }

        switch (currentState) {
            case "StartState":
                return getStartMenuKeyboard();
            case "HelpState":
                return getHelpKeyboard();
            case "MainState":
                return getMainMenuKeyboard();
            case "ExpenseState":
                return getExpenseMenuKeyboard();
            case "IncomeState":
                return getIncomeMenuKeyboard();
            default:
                return getMainMenuKeyboard();
        }
    }

    private static InlineKeyboardMarkup getSelectionCategoriesKeyboard(List<String> baseCategories,
                                                                       List<String> userCategories,
                                                                       String type) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<String> allCategories = new ArrayList<>(baseCategories);
        if (userCategories != null) {
            allCategories.addAll(userCategories);
        }

        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (int i = 0; i < allCategories.size(); i++) {
            String category = allCategories.get(i);
            String callbackData = "CATEGORY_" + type + "_" + category;

            InlineKeyboardButton button = createInlineButton(category, callbackData);
            currentRow.add(button);

            if (currentRow.size() == 2 || i == allCategories.size() - 1) {
                keyboard.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(createInlineButton("Назад", "BACK"));
        keyboard.add(backRow);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardButton createInlineButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public static void updateCategoriesFromFinanceState(Long userId, personalBanker.dialog.states.FinanceState financeState) {
        if (financeState == null) return;

        try {
            Map<String, Double> allCategories = financeState.getCategoriesMap();
            Set<String> baseCategories = financeState.getBaseCategories();

            List<String> userCategoryNames = new ArrayList<>();

            for (String category : allCategories.keySet()) {
                if (!baseCategories.contains(category)) {
                    userCategoryNames.add(category);
                }
            }

            String type = financeState.getClass().getSimpleName().contains("Income") ? "income" : "expense";
            updateUserCategories(userId, type, userCategoryNames);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}