// KeyboardManager.java
package personalBanker.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class KeyboardManager {

    // Стартовое меню: "Меню" и "Справка"
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

    // Главное меню: "Доходы", "Расходы", "Справка", "Назад"
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

    // Меню доходов: "Добавить", "Удалить", "Статистика", "Справка", "Назад"
    public static InlineKeyboardMarkup getIncomeMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Добавить", "INCOME_ADD"));
        row1.add(createInlineButton("Удалить", "INCOME_REMOVE"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Статистика", "INCOME_STATS"));

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

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton("Справка", "HELP"));
        row3.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return new InlineKeyboardMarkup(keyboard);
    }

    // Категории доходов: список категорий + "Справка" + "Назад"
    public static InlineKeyboardMarkup getIncomeCategoriesKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Работа", "CATEGORY_INCOME_Работа"));
        row1.add(createInlineButton("Другое", "CATEGORY_INCOME_Другое"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Мама подкинула)", "CATEGORY_INCOME_Мама"));
        row2.add(createInlineButton("Подарки", "CATEGORY_INCOME_Подарки"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton("Пассивный доход", "CATEGORY_INCOME_Пассивный доход"));

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        return new InlineKeyboardMarkup(keyboard);
    }

    // Категории расходов: список категорий + "Справка" + "Назад"
    public static InlineKeyboardMarkup getExpenseCategoriesKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Досуг", "CATEGORY_EXPENSE_Досуг"));
        row1.add(createInlineButton("Транспорт", "CATEGORY_EXPENSE_Транспорт"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Другое", "CATEGORY_EXPENSE_Другое"));
        row2.add(createInlineButton("Здоровье", "CATEGORY_EXPENSE_Здоровье"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton("Жилье", "CATEGORY_EXPENSE_Жилье"));
        row3.add(createInlineButton("Еда", "CATEGORY_EXPENSE_Еда"));

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        return new InlineKeyboardMarkup(keyboard);
    }

    // Клавиатура для ввода суммы: только "Назад"
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

    public static InlineKeyboardMarkup getResponseContextKeyboard(String currentState, String subState) {
        switch (currentState) {
            case "StartState":
                return getStartMenuKeyboard();
            case "HelpState":
                return getHelpKeyboard();
            case "MainState":
                return getMainMenuKeyboard();
            case "ExpenseState":
            case "IncomeState":
                if(subState.equals("CATEGORY_SELECTION")) {
                    return "IncomeState".equals(currentState)
                            ? getIncomeCategoriesKeyboard()
                            : getExpenseCategoriesKeyboard();
                }
                return "IncomeState".equals(currentState)
                        ? getIncomeMenuKeyboard()
                        : getExpenseMenuKeyboard();
            default:
                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(createInlineButton("Справка", "HELP"));
                row.add(createInlineButton("Назад", "BACK"));

                keyboard.add(row);
                return new InlineKeyboardMarkup(keyboard);
        }
    }

    private static InlineKeyboardButton createInlineButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}