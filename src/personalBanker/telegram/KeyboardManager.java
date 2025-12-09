package personalBanker.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import personalBanker.dialog.storage.UserCategoryStorage;

import java.util.*;

public class KeyboardManager {

    public static List<String> getUserCategories(Long userId, String type) {
        Map<String, Double> categoriesMap = UserCategoryStorage.loadUserCategories(userId, type);
        List<String> userCategories = new ArrayList<>();

        List<String> baseCategories = getBaseCategoriesForType(type);

        for (String category : categoriesMap.keySet()) {
            if (!baseCategories.contains(category)) {
                userCategories.add(category);
            }
        }

        return userCategories;
    }

    public static List<String> getAllCategories(Long userId, String type) {
        Map<String, Double> categoriesMap = UserCategoryStorage.loadUserCategories(userId, type);
        return new ArrayList<>(categoriesMap.keySet());
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
        row2.add(createInlineButton("Срок учета", "PERIOD_MENU"));
        row2.add(createInlineButton("Справка", "HELP"));

        keyboard.add(row1);
        keyboard.add(row2);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getIncomeMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Добавить доход", "INCOME_ADD"));
        row1.add(createInlineButton("Удалить доход", "INCOME_REMOVE"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Статистика", "INCOME_STATS"));
        row2.add(createInlineButton("Категории", "MANAGE_CATEGORIES"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton("Справка", "HELP"));
        row3.add(createInlineButton("Главное меню", "MAIN_MENU"));

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getExpenseMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Добавить расход", "EXPENSE_ADD"));
        row1.add(createInlineButton("Удалить расход", "EXPENSE_REMOVE"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Статистика", "EXPENSE_STATS"));
        row2.add(createInlineButton("Категории", "MANAGE_CATEGORIES"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton("Справка", "HELP"));
        row3.add(createInlineButton("Главное меню", "MAIN_MENU"));

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

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
        return getBackOnlyKeyboard();
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
        row2.add(createInlineButton("Установка целей/лимитов", "SET_LIMIT_GOAL"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getAddNewCategoryKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Да", "YES"));
        row1.add(createInlineButton("Нет", "NO"));
        keyboard.add(row1);
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getBackOnlyKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row);
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getPeriodMenuKeyboard(boolean isEnabled) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (isEnabled) {
            // Когда период включен - показываем полное меню
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createInlineButton("Изменить период", "PERIOD_SET"));
            row1.add(createInlineButton("Информация", "PERIOD_INFO"));

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(createInlineButton("Сбросить сейчас", "PERIOD_RESET_NOW"));

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            row3.add(createInlineButton("Назад", "BACK"));

            keyboard.add(row1);
            keyboard.add(row2);
            keyboard.add(row3);
        } else {
            // Когда период выключен - показываем только кнопку "Задать период"
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(createInlineButton("Задать период", "PERIOD_SET"));

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(createInlineButton("Назад", "BACK"));

            keyboard.add(row1);
            keyboard.add(row2);
        }

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getPeriodSelectionKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("День", "PERIOD_DAY"));
        row1.add(createInlineButton("Неделя", "PERIOD_WEEK"));
        row1.add(createInlineButton("Месяц", "PERIOD_MONTH"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Назад", "BACK"));

        keyboard.add(row1);
        keyboard.add(row2);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getPeriodConfirmResetKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("Да, сбросить", "PERIOD_CONFIRM_RESET"));
        row1.add(createInlineButton("Нет, отменить", "PERIOD_CANCEL_RESET"));

        keyboard.add(row1);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getResponseContextKeyboard(String currentState,
                                                                  String subState,
                                                                  String responseText,
                                                                  Long userId) {

        // 1. Обработка периодов
        if ("PeriodState".equals(currentState)) {
            return getPeriodStateKeyboard(subState, responseText, userId);
        }

        // 2. Обработка по тексту ответа (специфические случаи)
        if (responseText != null && !responseText.isEmpty()) {
            InlineKeyboardMarkup keyboardByText = getKeyboardByResponseText(currentState, subState, responseText, userId);
            if (keyboardByText != null) {
                return keyboardByText;
            }
        }

        // 3. Обработка по состоянию и подсостоянию (общая логика)
        return getKeyboardByStateAndSubState(currentState, subState, userId);
    }

    // Метод для обработки по тексту ответа
    private static InlineKeyboardMarkup getKeyboardByResponseText(String currentState,
                                                                  String subState,
                                                                  String responseText,
                                                                  Long userId) {

        // 2. Обработка установки целей/лимитов
        if (responseText.contains("Установка целей/лимитов") ||
                (responseText.contains("Установка") && responseText.contains("Выберите категорию"))) {

            boolean isIncome = "IncomeState".equals(currentState) ||
                    (responseText.contains("доходов") || responseText.contains("доходы"));

            return getSetLimitGoalKeyboard(userId, isIncome);
        }

        // 3. Обработка ввода суммы лимита/цели
        if (responseText.contains("Установка") && responseText.contains("для категории")) {
            return getBackOnlyKeyboard();
        }

        // 4. Обработка управления категориями
        if (responseText.contains("не найдена")) {
            return getAddNewCategoryKeyboard();
        } else if (responseText.contains("Управление категориями") ||
                responseText.contains("успешно добавлена") ||
                responseText.contains("успешно удалена") ||
                responseText.contains("является базовой") ||
                responseText.contains("Категория \"")) {

            return getCategoryManagementKeyboard();
        }

        // 5. Обработка выбора категории
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

        // 6. Обработка ввода суммы
        if (responseText.contains("Введите сумму") || responseText.contains("введите сумму")) {
            return getAmountInputKeyboard();
        }

        // 7. Обработка статистики
        if (responseText.contains("Статистика") && responseText.contains("Итого:")) {
            if ("IncomeState".equals(currentState)) {
                return getIncomeMenuKeyboard();
            } else if ("ExpenseState".equals(currentState)) {
                return getExpenseMenuKeyboard();
            }
        }

        // 8. Обработка неизвестной команды
        if (responseText.contains("Неизвестная команда")) {
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            List<InlineKeyboardButton> backRow = new ArrayList<>();
            backRow.add(createInlineButton("Назад", "BACK"));
            backRow.add(createInlineButton("Справка", "HELP"));
            keyboard.add(backRow);

            return new InlineKeyboardMarkup(keyboard);
        }

        return null; // Если не нашли совпадение по тексту
    }

    // Метод для обработки по состоянию и подсостоянию
    private static InlineKeyboardMarkup getKeyboardByStateAndSubState(String currentState,
                                                                      String subState,
                                                                      Long userId) {

        // Обработка PeriodState с учетом подсостояний
        if ("PeriodState".equals(currentState)) {
            Map<String, Object> periodInfo = UserCategoryStorage.getPeriodInfo(userId);
            boolean isEnabled = (Boolean) periodInfo.get("enabled");

            // Учитываем подсостояния PeriodState
            switch (subState) {
                case "SET_PERIOD":
                    return getPeriodSelectionKeyboard();

                case "VIEW_INFO":
                case "MANUAL_RESET":
                case "MAIN":
                default:
                    return getPeriodMenuKeyboard(isEnabled);
            }
        }

        // Обработка FinanceState с учетом подсостояний
        if ("IncomeState".equals(currentState) || "ExpenseState".equals(currentState)) {
            switch (subState) {
                case "CATEGORY_MANAGEMENT":
                    return getCategoryManagementKeyboard();

                case "ADD_CATEGORY":
                case "REMOVE_CATEGORY":
                case "CONFIRM_LIMIT_GOAL":
                case "SET_LIMIT_GOAL":
                    return getBackOnlyKeyboard();

                case "CATEGORY_SELECTION":
                    if ("IncomeState".equals(currentState)) {
                        return getDynamicIncomeCategoriesKeyboard(userId);
                    } else {
                        return getDynamicExpenseCategoriesKeyboard(userId);
                    }

                case "AMOUNT_INPUT":
                    return getAmountInputKeyboard();

                case "MAIN_MENU":
                default:
                    if ("IncomeState".equals(currentState)) {
                        return getIncomeMenuKeyboard();
                    } else {
                        return getExpenseMenuKeyboard();
                    }
            }
        }

        // Обработка остальных состояний
        switch (currentState) {
            case "StartState":
                return getStartMenuKeyboard();

            case "HelpState":
                return getHelpKeyboard();

            case "MainState":
                return getMainMenuKeyboard();

            case "ExpenseState":
                // Сюда попадаем только если не IncomeState и не обработали подсостояния
                return getExpenseMenuKeyboard();

            case "IncomeState":
                // Сюда попадаем только если не ExpenseState и не обработали подсостояния
                return getIncomeMenuKeyboard();

            case "PeriodState":
                // Сюда не должны попадать (обработано выше)
                Map<String, Object> periodInfo = UserCategoryStorage.getPeriodInfo(userId);
                boolean isEnabled = (Boolean) periodInfo.get("enabled");
                return getPeriodMenuKeyboard(isEnabled);

            default:
                return getMainMenuKeyboard();
        }
    }

    private static InlineKeyboardMarkup getPeriodStateKeyboard(String subState,
                                                               String responseText,
                                                               Long userId) {

        Map<String, Object> periodInfo = UserCategoryStorage.getPeriodInfo(userId);
        boolean isEnabled = (Boolean) periodInfo.get("enabled");

        // После установки периода показываем меню с включенным периодом
        if (responseText.contains("Периодический сброс установлен") ||
                responseText.contains("Суммы успешно сброшены")) {
            // Период точно включен после этих действий
            return getPeriodMenuKeyboard(true);
        }

        if (responseText.contains("Выберите период для автоматического сброса") ||
                "SET_PERIOD".equals(subState)) {
            return getPeriodSelectionKeyboard();
        }

        if (responseText.contains("Вы уверены, что хотите принудительно сбросить суммы") ||
                "MANUAL_RESET".equals(subState)) {
            return getPeriodConfirmResetKeyboard();
        }

        if (responseText.contains("Периодический сброс выключен") ||
                responseText.contains("Не удалось сбросить суммы") ||
                responseText.contains("Информация о периоде") ||
                "MAIN".equals(subState) ||
                "VIEW_INFO".equals(subState)) {

            return getPeriodMenuKeyboard(isEnabled);
        }

        // По умолчанию - показываем меню с текущим статусом периода
        return getPeriodMenuKeyboard(isEnabled);
    }

    private static InlineKeyboardMarkup getSelectionCategoriesKeyboard(List<String> baseCategories,
                                                                       List<String> userCategories,
                                                                       String type) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<String> allCategories = new ArrayList<>(baseCategories);
        if (userCategories != null) {
            allCategories.addAll(userCategories);
        }

        // Создаем кнопки категорий (2 в строке)
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

        // Добавляем кнопку "Назад"
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

    public static InlineKeyboardMarkup getSetLimitGoalKeyboard(Long userId, boolean isIncome) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        String type = isIncome ? "income" : "expense";
        List<String> allCategories = getAllCategories(userId, type);

        if (allCategories.isEmpty()) {
            List<InlineKeyboardButton> backRow = new ArrayList<>();
            backRow.add(createInlineButton("Назад", "BACK"));
            keyboard.add(backRow);
            return new InlineKeyboardMarkup(keyboard);
        }

        // Создаем кнопки категорий (по 2 в строке)
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (int i = 0; i < allCategories.size(); i++) {
            String category = allCategories.get(i);
            String callbackData = "SET_LIMIT_FOR_" + category;

            InlineKeyboardButton button = createInlineButton(category, callbackData);
            currentRow.add(button);

            if (currentRow.size() == 2 || i == allCategories.size() - 1) {
                keyboard.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }

        // Добавляем кнопки управления
        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(createInlineButton("Назад", "BACK"));
        keyboard.add(backRow);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static List<String> getBaseCategoriesForType(String type) {
        if ("income".equals(type)) {
            return Arrays.asList("Работа", "Пассивный доход", "Инвестиции", "Подарки");
        } else if ("expense".equals(type)) {
            return Arrays.asList("Еда", "Транспорт", "Жилье", "Досуг", "Здоровье");
        }
        return new ArrayList<>();
    }
}