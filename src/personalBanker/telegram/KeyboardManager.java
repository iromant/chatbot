package personalBanker.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyboardManager {

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
        row2.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);

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

    // Клавиатура для выбора категории при добавлении/удалении дохода/расхода (ТОЛЬКО категории + Назад)
    public static InlineKeyboardMarkup getIncomeCategoriesSelectionKeyboard() {
        List<String> categories = Arrays.asList(
                "Работа", "Пассивный доход", "Инвестиции", "Подарки"
        );

        return getSelectionCategoriesKeyboard(categories, "INCOME");
    }

    public static InlineKeyboardMarkup getExpenseCategoriesSelectionKeyboard() {
        List<String> categories = Arrays.asList(
                "Еда", "Транспорт", "Жилье", "Досуг", "Здоровье"
        );

        return getSelectionCategoriesKeyboard(categories, "EXPENSE");
    }

    // отдельное сеню для управления категориями(ибо нефиг все в одно)
    public static InlineKeyboardMarkup getIncomeCategoriesManagementKeyboard() {
        List<String> categories = Arrays.asList(
                "Работа", "Пассивный доход", "Инвестиции", "Подарки"
        );

        return getManagementCategoriesKeyboard(categories, "INCOME");
    }

    public static InlineKeyboardMarkup getExpenseCategoriesManagementKeyboard() {
        List<String> categories = Arrays.asList(
                "Еда", "Транспорт", "Жилье", "Досуг", "Здоровье"
        );

        return getManagementCategoriesKeyboard(categories, "EXPENSE");
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
        row1.add(createInlineButton("Добавить категорию", "ADD_CATEGORY"));
        row1.add(createInlineButton("Удалить категорию", "REMOVE_CATEGORY"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Назад", "BACK"));
        row2.add(createInlineButton("Меню", "MAIN_MENU"));

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

    public static InlineKeyboardMarkup getResponseContextKeyboard(String currentState, String subState, String responseText) {
        if (responseText.contains("Управление категориями") ||
                responseText.contains("успешно добавлена") ||
                responseText.contains("успешно удалена")) {
            return getCategoryManagementKeyboard();
        }

        if (responseText.contains("Введите сумму") || responseText.contains("введите сумму")) {
            return getAmountInputKeyboard();
        }

        if (responseText.contains("Выберите категорию") ||
                responseText.contains("Доступные категории") ||
                (responseText.contains("категорию") &&
                        !responseText.contains("Управление категориями") &&
                        !responseText.contains("успешно"))) {
            if ("IncomeState".equals(currentState)) {
                return getIncomeCategoriesSelectionKeyboard();
            } else if ("ExpenseState".equals(currentState)) {
                return getExpenseCategoriesSelectionKeyboard();
            }
        }

        if (responseText.contains("Введите название") ||
                responseText.contains("введите название")) {
            return getBackOnlyKeyboard();
        }

        // Стандартная логика по состоянию и подсостоянию
        return getResponseContextKeyboard(currentState, subState);
    }

    public static InlineKeyboardMarkup getResponseContextKeyboard(String currentState, String subState) {
        // Если состояние FinanceState и подсостояние CATEGORY_MANAGEMENT
        if (("ExpenseState".equals(currentState) || "IncomeState".equals(currentState))
                && "CATEGORY_MANAGEMENT".equals(subState)) {
            return getCategoryManagementKeyboard();
        }

        // Если состояние FinanceState и подсостояние ADD_CATEGORY или REMOVE_CATEGORY
        if (("ExpenseState".equals(currentState) || "IncomeState".equals(currentState))
                && ("ADD_CATEGORY".equals(subState) || "REMOVE_CATEGORY".equals(subState))) {
            return getBackOnlyKeyboard();
        }

        // Если состояние FinanceState и подсостояние CATEGORY_SELECTION
        if (("ExpenseState".equals(currentState) || "IncomeState".equals(currentState))
                && "CATEGORY_SELECTION".equals(subState)) {
            // Для выбора категории при добавлении/удалении показываем ТОЛЬКО категории
            return "IncomeState".equals(currentState)
                    ? getIncomeCategoriesSelectionKeyboard()
                    : getExpenseCategoriesSelectionKeyboard();
        }

        // Если состояние FinanceState и подсостояние AMOUNT_INPUT
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

    // Клавиатура для ВЫБОРА категорий (при добавлении/удалении дохода/расхода) - ТОЛЬКО КАТЕГОРИИ(ДА КОСТЫЛИ А КОМУ НОГИ НЕ ЛОМАЛИ) + Назад
    private static InlineKeyboardMarkup getSelectionCategoriesKeyboard(List<String> categories, String type) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            String callbackData = "CATEGORY_" + type + "_" + category;

            InlineKeyboardButton button = createInlineButton(category, callbackData);
            currentRow.add(button);

            if (currentRow.size() == 2 || i == categories.size() - 1) {
                keyboard.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(createInlineButton("Назад", "BACK"));
        keyboard.add(backRow);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup getManagementCategoriesKeyboard(List<String> categories, String type) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            String callbackData = "CATEGORY_" + type + "_" + category;

            InlineKeyboardButton button = createInlineButton(category, callbackData);
            currentRow.add(button);

            if (currentRow.size() == 2 || i == categories.size() - 1) {
                keyboard.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }

        // Кнопки управления категориями
        List<InlineKeyboardButton> manageRow1 = new ArrayList<>();
        manageRow1.add(createInlineButton("Добавить категорию", "ADD_CATEGORY"));
        manageRow1.add(createInlineButton("Удалить категорию", "REMOVE_CATEGORY"));

        List<InlineKeyboardButton> manageRow2 = new ArrayList<>();
        manageRow2.add(createInlineButton("Назад", "BACK"));
        manageRow2.add(createInlineButton("Меню", "MAIN_MENU"));

        keyboard.add(manageRow1);
        keyboard.add(manageRow2);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardButton createInlineButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}