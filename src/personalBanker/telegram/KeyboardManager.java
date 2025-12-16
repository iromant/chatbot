package personalBanker.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import personalBanker.dialog.storage.UserCategoryStorage;

import java.util.*;

public class KeyboardManager {

    private static final Map<String, InlineKeyboardButton> buttonCache = new HashMap<>();
    private static final Map<String, InlineKeyboardMarkup> keyboardCache = new HashMap<>();

    private static final String KEY_START_MENU = "start_menu";
    private static final String KEY_MAIN_MENU = "main_menu";
    private static final String KEY_HELP = "help";
    private static final String KEY_BACK_ONLY = "back_only";
    private static final String KEY_INCOME_MENU = "income_menu";
    private static final String KEY_EXPENSE_MENU = "expense_menu";
    private static final String KEY_PERIOD_SELECTION = "period_selection";
    private static final String KEY_CATEGORY_MANAGEMENT = "category_management";
    private static final String KEY_ADD_CATEGORY = "add_category";
    private static final String KEY_CLEAR_DATA_CONFIRMATION = "clear_data_confirmation";
    private static final String KEY_PERIOD_CONFIRM_RESET = "period_confirm_reset";

    static {
        initializeButtonCache();
        initializeKeyboardCache();
    }

    private static void initializeButtonCache() {
        buttonCache.put("BACK", createNewInlineButton("Назад", "BACK"));
        buttonCache.put("MAIN_MENU", createNewInlineButton("Меню", "MAIN_MENU"));
        buttonCache.put("HELP", createNewInlineButton("Справка", "HELP"));
        buttonCache.put("YES", createNewInlineButton("Да", "YES"));
        buttonCache.put("NO", createNewInlineButton("Нет", "NO"));

        // Кнопки для основного меню
        buttonCache.put("INCOME_MENU", createNewInlineButton("Доходы", "INCOME_MENU"));
        buttonCache.put("EXPENSE_MENU", createNewInlineButton("Расходы", "EXPENSE_MENU"));
        buttonCache.put("PERIOD_MENU", createNewInlineButton("Срок учета", "PERIOD_MENU"));
        buttonCache.put("CLEAR_MY_DATA", createNewInlineButton("Удалить данные", "CLEAR_MY_DATA"));

        // Кнопки для меню доходов/расходов
        buttonCache.put("INCOME_ADD", createNewInlineButton("Добавить доход", "INCOME_ADD"));
        buttonCache.put("INCOME_REMOVE", createNewInlineButton("Удалить доход", "INCOME_REMOVE"));
        buttonCache.put("INCOME_STATS", createNewInlineButton("Статистика", "INCOME_STATS"));
        buttonCache.put("EXPENSE_ADD", createNewInlineButton("Добавить расход", "EXPENSE_ADD"));
        buttonCache.put("EXPENSE_REMOVE", createNewInlineButton("Удалить расход", "EXPENSE_REMOVE"));
        buttonCache.put("EXPENSE_STATS", createNewInlineButton("Статистика", "EXPENSE_STATS"));
        buttonCache.put("MANAGE_CATEGORIES", createNewInlineButton("Категории", "MANAGE_CATEGORIES"));

        // Кнопки для управления категориями
        buttonCache.put("ADD_CATEGORY", createNewInlineButton("Добавить", "ADD_CATEGORY"));
        buttonCache.put("REMOVE_CATEGORY", createNewInlineButton("Удалить", "REMOVE_CATEGORY"));
        buttonCache.put("SET_LIMIT_GOAL", createNewInlineButton("Установка целей/лимитов", "SET_LIMIT_GOAL"));

        // Кнопки для периодов
        buttonCache.put("PERIOD_SET", createNewInlineButton("Изменить период", "PERIOD_SET"));
        buttonCache.put("PERIOD_INFO", createNewInlineButton("Информация", "PERIOD_INFO"));
        buttonCache.put("PERIOD_RESET_NOW", createNewInlineButton("Сбросить сейчас", "PERIOD_RESET_NOW"));
        buttonCache.put("PERIOD_DAY", createNewInlineButton("День", "PERIOD_DAY"));
        buttonCache.put("PERIOD_WEEK", createNewInlineButton("Неделя", "PERIOD_WEEK"));
        buttonCache.put("PERIOD_MONTH", createNewInlineButton("Месяц", "PERIOD_MONTH"));
        buttonCache.put("PERIOD_CONFIRM_RESET", createNewInlineButton("Да, сбросить", "PERIOD_CONFIRM_RESET"));
        buttonCache.put("PERIOD_CANCEL_RESET", createNewInlineButton("Нет, отменить", "PERIOD_CANCEL_RESET"));
        buttonCache.put("CONFIRM_CLEAR_DATA", createNewInlineButton("Да, удалить всё", "CONFIRM_CLEAR_DATA"));
        buttonCache.put("CANCEL_CLEAR_DATA", createNewInlineButton("Нет, отменить", "CANCEL_CLEAR_DATA"));
    }

    private static void initializeKeyboardCache() {
        keyboardCache.put(KEY_START_MENU, createStartMenuKeyboard());
        keyboardCache.put(KEY_MAIN_MENU, createMainMenuKeyboard());
        keyboardCache.put(KEY_HELP, createHelpKeyboard());
        keyboardCache.put(KEY_BACK_ONLY, createBackOnlyKeyboard());
        keyboardCache.put(KEY_INCOME_MENU, createIncomeMenuKeyboard());
        keyboardCache.put(KEY_EXPENSE_MENU, createExpenseMenuKeyboard());
        keyboardCache.put(KEY_PERIOD_SELECTION, createPeriodSelectionKeyboard());
        keyboardCache.put(KEY_CATEGORY_MANAGEMENT, createCategoryManagementKeyboard());
        keyboardCache.put(KEY_ADD_CATEGORY, createAddNewCategoryKeyboard());
        keyboardCache.put(KEY_CLEAR_DATA_CONFIRMATION, createClearDataConfirmationKeyboard());
        keyboardCache.put(KEY_PERIOD_CONFIRM_RESET, createPeriodConfirmResetKeyboard());
    }


    public static InlineKeyboardMarkup getStartMenuKeyboard() {
        return keyboardCache.get(KEY_START_MENU);
    }

    public static InlineKeyboardMarkup getMainMenuKeyboard() {
        return keyboardCache.get(KEY_MAIN_MENU);
    }

    public static InlineKeyboardMarkup getHelpKeyboard() {
        return keyboardCache.get(KEY_HELP);
    }

    public static InlineKeyboardMarkup getBackOnlyKeyboard() {
        return keyboardCache.get(KEY_BACK_ONLY);
    }

    public static InlineKeyboardMarkup getIncomeMenuKeyboard() {
        return keyboardCache.get(KEY_INCOME_MENU);
    }

    public static InlineKeyboardMarkup getExpenseMenuKeyboard() {
        return keyboardCache.get(KEY_EXPENSE_MENU);
    }

    public static InlineKeyboardMarkup getPeriodSelectionKeyboard() {
        return keyboardCache.get(KEY_PERIOD_SELECTION);
    }

    public static InlineKeyboardMarkup getCategoryManagementKeyboard() {
        return keyboardCache.get(KEY_CATEGORY_MANAGEMENT);
    }

    public static InlineKeyboardMarkup getAddNewCategoryKeyboard() {
        return keyboardCache.get(KEY_ADD_CATEGORY);
    }

    public static InlineKeyboardMarkup getClearDataConfirmationKeyboard() {
        return keyboardCache.get(KEY_CLEAR_DATA_CONFIRMATION);
    }

    public static InlineKeyboardMarkup getPeriodConfirmResetKeyboard() {
        return keyboardCache.get(KEY_PERIOD_CONFIRM_RESET);
    }

    public static InlineKeyboardButton createInlineButton(String text, String callbackData) {
        String cacheKey = callbackData + "|" + text;
        return buttonCache.computeIfAbsent(cacheKey,
                key -> createNewInlineButton(text, callbackData));
    }

    private static InlineKeyboardButton createNewInlineButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private static InlineKeyboardMarkup createStartMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonCache.get("MAIN_MENU"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(buttonCache.get("HELP"));

        keyboard.add(row1);
        keyboard.add(row2);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup createMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonCache.get("INCOME_MENU"));
        row1.add(buttonCache.get("EXPENSE_MENU"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(buttonCache.get("PERIOD_MENU"));
        row2.add(buttonCache.get("HELP"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(buttonCache.get("CLEAR_MY_DATA"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup createHelpKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(buttonCache.get("BACK"));
        row.add(buttonCache.get("MAIN_MENU"));

        keyboard.add(row);
        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup createBackOnlyKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(buttonCache.get("BACK"));

        keyboard.add(row);
        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup createIncomeMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonCache.get("INCOME_ADD"));
        row1.add(buttonCache.get("INCOME_REMOVE"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(buttonCache.get("INCOME_STATS"));
        row2.add(buttonCache.get("MANAGE_CATEGORIES"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(buttonCache.get("HELP"));
        row3.add(buttonCache.get("MAIN_MENU"));

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(buttonCache.get("BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup createExpenseMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonCache.get("EXPENSE_ADD"));
        row1.add(buttonCache.get("EXPENSE_REMOVE"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(buttonCache.get("EXPENSE_STATS"));
        row2.add(buttonCache.get("MANAGE_CATEGORIES"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(buttonCache.get("HELP"));
        row3.add(buttonCache.get("MAIN_MENU"));

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(buttonCache.get("BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup createPeriodSelectionKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonCache.get("PERIOD_DAY"));
        row1.add(buttonCache.get("PERIOD_WEEK"));
        row1.add(buttonCache.get("PERIOD_MONTH"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(buttonCache.get("BACK"));

        keyboard.add(row1);
        keyboard.add(row2);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup createCategoryManagementKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonCache.get("ADD_CATEGORY"));
        row1.add(buttonCache.get("REMOVE_CATEGORY"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(buttonCache.get("SET_LIMIT_GOAL"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(buttonCache.get("BACK"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup createAddNewCategoryKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonCache.get("YES"));
        row1.add(buttonCache.get("NO"));
        keyboard.add(row1);
        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup createClearDataConfirmationKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonCache.get("CONFIRM_CLEAR_DATA"));
        row1.add(buttonCache.get("CANCEL_CLEAR_DATA"));

        keyboard.add(row1);
        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardMarkup createPeriodConfirmResetKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(buttonCache.get("PERIOD_CONFIRM_RESET"));
        row1.add(buttonCache.get("PERIOD_CANCEL_RESET"));

        keyboard.add(row1);
        return new InlineKeyboardMarkup(keyboard);
    }

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

    public static InlineKeyboardMarkup getPeriodMenuKeyboard(boolean isEnabled) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (isEnabled) {
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(buttonCache.get("PERIOD_SET"));
            row1.add(buttonCache.get("PERIOD_INFO"));

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(buttonCache.get("PERIOD_RESET_NOW"));

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            row3.add(buttonCache.get("BACK"));

            keyboard.add(row1);
            keyboard.add(row2);
            keyboard.add(row3);
        } else {
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(buttonCache.get("PERIOD_SET"));

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(buttonCache.get("BACK"));

            keyboard.add(row1);
            keyboard.add(row2);
        }

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getSetLimitGoalKeyboard(Long userId, boolean isIncome) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        String type = isIncome ? "income" : "expense";
        List<String> allCategories = getAllCategories(userId, type);

        if (allCategories.isEmpty()) {
            List<InlineKeyboardButton> backRow = new ArrayList<>();
            backRow.add(buttonCache.get("BACK"));
            keyboard.add(backRow);
            return new InlineKeyboardMarkup(keyboard);
        }

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

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(buttonCache.get("BACK"));
        keyboard.add(backRow);

        return new InlineKeyboardMarkup(keyboard);
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
        backRow.add(buttonCache.get("BACK"));
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

    public static InlineKeyboardMarkup getResponseContextKeyboard(String currentState,
                                                                  String subState,
                                                                  String responseText,
                                                                  Long userId) {

        // 1. Обработка по тексту ответа (специфические случаи)
        if (responseText != null && !responseText.isEmpty()) {
            InlineKeyboardMarkup keyboardByText = getKeyboardByResponseText(currentState, responseText, userId);
            if (keyboardByText != null) {
                return keyboardByText;
            }
        }

        // 2. Обработка по состоянию и подсостоянию (общая логика)
        return getKeyboardByStateAndSubState(currentState, subState, userId);
    }

    private static InlineKeyboardMarkup getKeyboardByResponseText(String currentState,
                                                                  String responseText,
                                                                  Long userId) {

        // 1. Обработка кнопки удаления данных
        if (responseText.contains("ВНИМАНИЕ") && responseText.contains("удалить ВСЕ ваши данные")) {
            return getClearDataConfirmationKeyboard();
        }

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
            backRow.add(buttonCache.get("BACK"));
            backRow.add(buttonCache.get("HELP"));
            keyboard.add(backRow);

            return new InlineKeyboardMarkup(keyboard);
        }

        //9. Обработка просмотра статистики
        if (responseText.contains("Просмотреть статистику")) {
            return getBackOnlyKeyboard();
        }

        //10. Обработка для текста о сбросе периода
        if (responseText.contains("Период сброшен") &&
                responseText.contains("Новый период начат")) {
            return getBackOnlyKeyboard();
        }

        return null; // Если не нашли совпадение по тексту
    }

    private static InlineKeyboardMarkup getKeyboardByStateAndSubState(String currentState,
                                                                      String subState,
                                                                      Long userId) {

        // Обработка PeriodState с учетом подсостояний
        if ("PeriodState".equals(currentState)) {
            Map<String, Object> periodInfo = UserCategoryStorage.getPeriodInfo(userId);
            boolean isEnabled = (Boolean) periodInfo.get("enabled");
            
            switch (subState) {
                case "SET_PERIOD":
                    return getPeriodSelectionKeyboard();
                case "MANUAL_RESET":
                    System.out.println("wertyhujiku7654");
                    return getPeriodConfirmResetKeyboard();
                case "VIEW_INFO":
                case "MAIN":
                default:
                    System.out.println(subState);
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
                return getExpenseMenuKeyboard();

            case "IncomeState":
                return getIncomeMenuKeyboard();

            default:
                return getMainMenuKeyboard();
        }
    }
}