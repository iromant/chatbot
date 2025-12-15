package personalBanker.dialog.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.text.MessageFormat;

public class UserCategoryStorage {
    private static final String CATEGORIES_FILE = "user_data/categories_data.json";
    private static final String PERIODS_FILE = "user_data/periods_data.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static Map<String, UserCategories> allCategories = new ConcurrentHashMap<>();
    private static Map<String, UserPeriods> allPeriods = new ConcurrentHashMap<>();

    private static final boolean USE_DATABASE = true;

    static {
        new File("user_data").mkdirs();
        if (!USE_DATABASE) {
            loadCategoriesData();
            loadPeriodsData();
            startAutoSaveThread();
        }
        startPeriodResetMonitorThread();
    }

    private static class UserCategories {
        Map<String, Double> categories = new HashMap<>();
        Map<String, Double> limitsGoals = new HashMap<>();
    }

    private static class UserPeriods {
        boolean enabled = false;
        String periodType = "month";
        String periodStartDate;
        String nextResetDate;
        long daysLeft = 0;
        String lastResetDate;
        List<Map<String, Object>> history = new ArrayList<>();

        UserPeriods() {
            LocalDate now = LocalDate.now();
            this.periodStartDate = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
            this.nextResetDate = calculateNextResetDate("month", now);
            this.daysLeft = calculateDaysLeft(now, this.nextResetDate);
            this.lastResetDate = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

    private static String getUserKey(Long userId, String type) {
        return userId + "_" + type;
    }

    private static String getPeriodKey(Long userId) {
        return userId.toString();
    }

    private static String calculateNextResetDate(String periodType, LocalDate startDate) {
        LocalDate nextDate;
        switch (periodType) {
            case "day":
                nextDate = startDate.plusDays(1);
                break;
            case "week":
                nextDate = startDate.plusWeeks(1);
                break;
            case "month":
            default:
                nextDate = startDate.plusMonths(1);
        }
        return nextDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static long calculateDaysLeft(LocalDate currentDate, String nextResetDateStr) {
        try {
            LocalDate nextResetDate = LocalDate.parse(nextResetDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return ChronoUnit.DAYS.between(currentDate, nextResetDate);
        } catch (Exception e) {
            return 0;
        }
    }

    private static void loadCategoriesData() {
        if (USE_DATABASE) return;

        File file = new File(CATEGORIES_FILE);
        if (!file.exists()) {
            allCategories = new ConcurrentHashMap<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, UserCategories>>() {}.getType();
            Map<String, UserCategories> loadedData = gson.fromJson(reader, type);

            if (loadedData != null) {
                allCategories = new ConcurrentHashMap<>(loadedData);
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Ошибка парсинга JSON файла категорий. Создаем новый файл.");
            backupCorruptedFile(CATEGORIES_FILE);
            allCategories = new ConcurrentHashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
            allCategories = new ConcurrentHashMap<>();
        }
    }

    private static void loadPeriodsData() {
        if (USE_DATABASE) return;

        File file = new File(PERIODS_FILE);
        if (!file.exists()) {
            allPeriods = new ConcurrentHashMap<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, UserPeriods>>() {}.getType();
            Map<String, UserPeriods> loadedData = gson.fromJson(reader, type);

            if (loadedData != null) {
                allPeriods = new ConcurrentHashMap<>(loadedData);
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Ошибка парсинга JSON файла периодов. Создаем новый файл.");
            backupCorruptedFile(PERIODS_FILE);
            allPeriods = new ConcurrentHashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
            allPeriods = new ConcurrentHashMap<>();
        }
    }

    private static void backupCorruptedFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                File backup = new File(filePath + ".corrupted_" + System.currentTimeMillis());
                file.renameTo(backup);
                System.err.println("Создана резервная копия поврежденного файла: " + backup.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveCategoriesData() {
        if (USE_DATABASE) return;

        try (FileWriter writer = new FileWriter(CATEGORIES_FILE)) {
            gson.toJson(allCategories, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void savePeriodsData() {
        if (USE_DATABASE) return;

        try (FileWriter writer = new FileWriter(PERIODS_FILE)) {
            gson.toJson(allPeriods, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveAllData() {
        if (USE_DATABASE) return;

        saveCategoriesData();
        savePeriodsData();
    }

    private static void startAutoSaveThread() {
        if (USE_DATABASE) return;

        Thread autoSaveThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30 * 1000L);
                    saveAllData();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        autoSaveThread.setDaemon(true);
        autoSaveThread.start();
    }

    private static UserCategories getUserCategories(Long userId, String type) {
        if (USE_DATABASE) {
            UserCategories userData = new UserCategories();
            userData.categories = DatabaseManager.loadUserCategories(userId, type);
            userData.limitsGoals = DatabaseManager.loadLimitsGoals(userId, type);
            return userData;
        }

        String key = getUserKey(userId, type);
        return allCategories.computeIfAbsent(key, k -> new UserCategories());
    }

    private static UserPeriods getUserPeriods(Long userId) {
        if (USE_DATABASE) {
            Map<String, Object> periodInfo = DatabaseManager.getPeriodInfo(userId);
            UserPeriods period = new UserPeriods();

            period.enabled = (Boolean) periodInfo.get("enabled");
            period.periodType = (String) periodInfo.get("periodType");
            period.periodStartDate = (String) periodInfo.get("periodStartDate");
            period.nextResetDate = (String) periodInfo.get("nextResetDate");
            period.daysLeft = (Long) periodInfo.get("daysLeft");
            period.lastResetDate = (String) periodInfo.get("lastResetDate");

            return period;
        }

        String key = getPeriodKey(userId);
        return allPeriods.computeIfAbsent(key, k -> new UserPeriods());
    }

    public static Map<String, Double> loadUserCategories(Long userId, String type) {
        UserCategories userData = getUserCategories(userId, type);
        return new HashMap<>(userData.categories);
    }

    public static Map<String, Double> loadLimitsGoals(Long userId, String type) {
        UserCategories userData = getUserCategories(userId, type);
        return new HashMap<>(userData.limitsGoals);
    }

    public static void saveUserCategoriesAndLimits(Long userId, String type,
                                                   Map<String, Double> categories,
                                                   Map<String, Double> limitsGoals) {
        if (USE_DATABASE) {
            DatabaseManager.saveUserCategoriesAndLimits(userId, type, categories, limitsGoals);
            return;
        }

        UserCategories userData = getUserCategories(userId, type);
        if (categories != null) {
            userData.categories = new HashMap<>(categories);
        }
        if (limitsGoals != null) {
            userData.limitsGoals = new HashMap<>(limitsGoals);
        }
        saveCategoriesData();
    }

    private static void updatePeriodInfo(Long userId) {
        UserPeriods periodData = getUserPeriods(userId);
        LocalDate currentDate = LocalDate.now();

        if (periodData.enabled && periodData.nextResetDate != null
                && !periodData.nextResetDate.isEmpty()) {
            LocalDate nextResetDate = LocalDate.parse(periodData.nextResetDate, DateTimeFormatter.ISO_LOCAL_DATE);

            if (currentDate.isAfter(nextResetDate) || currentDate.isEqual(nextResetDate)) {
                autoResetPeriod(userId);
            }
        }

        if (periodData.enabled) {
            periodData.daysLeft = calculateDaysLeft(currentDate, periodData.nextResetDate);
        } else {
            periodData.daysLeft = 0L;
        }

        if (USE_DATABASE) {
            Map<String, Object> periodInfo = new HashMap<>();
            periodInfo.put("enabled", periodData.enabled);
            periodInfo.put("periodType", periodData.periodType);
            periodInfo.put("periodStartDate", periodData.periodStartDate);
            periodInfo.put("nextResetDate", periodData.nextResetDate);
            periodInfo.put("daysLeft", periodData.daysLeft);
            periodInfo.put("lastResetDate", periodData.lastResetDate);

            DatabaseManager.savePeriodInfo(userId, periodInfo);
        }
    }

    public static Map<String, Object> getPeriodInfo(Long userId) {
        updatePeriodInfo(userId);
        UserPeriods periodData = getUserPeriods(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("enabled", periodData.enabled);
        result.put("periodType", periodData.periodType);
        result.put("periodStartDate", periodData.periodStartDate);
        result.put("nextResetDate", periodData.nextResetDate);
        result.put("daysLeft", periodData.daysLeft);
        result.put("lastResetDate", periodData.lastResetDate);
        result.put("history", new ArrayList<>(periodData.history));

        return result;
    }

    public static void setUserPeriod(Long userId, String periodType) {
        UserPeriods periodData = getUserPeriods(userId);

        periodData.enabled = true;
        periodData.periodType = periodType;

        LocalDate startDate = LocalDate.now();
        periodData.periodStartDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        String nextResetDate = calculateNextResetDate(periodType, startDate);
        periodData.nextResetDate = nextResetDate;
        periodData.daysLeft = calculateDaysLeft(startDate, nextResetDate);
        periodData.lastResetDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        if (USE_DATABASE) {
            Map<String, Object> periodInfo = new HashMap<>();
            periodInfo.put("enabled", periodData.enabled);
            periodInfo.put("periodType", periodData.periodType);
            periodInfo.put("periodStartDate", periodData.periodStartDate);
            periodInfo.put("nextResetDate", periodData.nextResetDate);
            periodInfo.put("daysLeft", periodData.daysLeft);
            periodInfo.put("lastResetDate", periodData.lastResetDate);

            DatabaseManager.savePeriodInfo(userId, periodInfo);
        } else {
            savePeriodsData();
        }
    }

    public static void disablePeriod(Long userId) {
        UserPeriods periodData = getUserPeriods(userId);
        periodData.enabled = false;
        periodData.daysLeft = 0L;

        if (USE_DATABASE) {
            Map<String, Object> periodInfo = new HashMap<>();
            periodInfo.put("enabled", periodData.enabled);
            periodInfo.put("periodType", periodData.periodType);
            periodInfo.put("periodStartDate", periodData.periodStartDate);
            periodInfo.put("nextResetDate", periodData.nextResetDate);
            periodInfo.put("daysLeft", periodData.daysLeft);
            periodInfo.put("lastResetDate", periodData.lastResetDate);

            DatabaseManager.savePeriodInfo(userId, periodInfo);
        } else {
            savePeriodsData();
        }
    }

    public static String manualResetPeriod(Long userId) {
        try {
            return resetPeriod(userId);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean autoResetPeriod(Long userId) {
        try {
            String notificationMessage = resetPeriod(userId);

            saveNotificationForUser(userId, notificationMessage);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String resetPeriod(Long userId) {
        UserPeriods periodData = getUserPeriods(userId);
        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("periodType", periodData.periodType);
        historyEntry.put("startDate", periodData.periodStartDate);
        historyEntry.put("endDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        Map<String, Double> incomeCategories = loadUserCategories(userId, "income");
        Map<String, Double> expenseCategories = loadUserCategories(userId, "expense");

        double incomeTotal = incomeCategories.values().stream().mapToDouble(Double::doubleValue).sum();
        double expenseTotal = expenseCategories.values().stream().mapToDouble(Double::doubleValue).sum();
        double balance = incomeTotal - expenseTotal;

        historyEntry.put("incomeTotal", incomeTotal);
        historyEntry.put("expenseTotal", expenseTotal);
        historyEntry.put("balance", balance);
        historyEntry.put("incomeDetails", new HashMap<>(incomeCategories));
        historyEntry.put("expenseDetails", new HashMap<>(expenseCategories));

        periodData.history.add(historyEntry);

        if (periodData.history.size() > 1) {
            periodData.history = periodData.history.subList(
                    periodData.history.size() - 1,
                    periodData.history.size()
            );
        }

        Map<String, Double> resetIncomeCategories = new HashMap<>();
        Map<String, Double> incomeLimitsGoals = loadLimitsGoals(userId, "income");
        for (String category : incomeCategories.keySet()) {
            resetIncomeCategories.put(category, 0.0);
        }
        saveUserCategoriesAndLimits(userId, "income", resetIncomeCategories, incomeLimitsGoals);

        Map<String, Double> resetExpenseCategories = new HashMap<>();
        Map<String, Double> expenseLimitsGoals = loadLimitsGoals(userId, "expense");
        for (String category : expenseCategories.keySet()) {
            resetExpenseCategories.put(category, 0.0);
        }
        saveUserCategoriesAndLimits(userId, "expense", resetExpenseCategories, expenseLimitsGoals);

        LocalDate newStartDate = LocalDate.now();
        periodData.periodStartDate = newStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        periodData.lastResetDate = newStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        String nextResetDate = calculateNextResetDate(periodData.periodType, newStartDate);
        periodData.nextResetDate = nextResetDate;
        periodData.daysLeft = calculateDaysLeft(newStartDate, nextResetDate);

        if (USE_DATABASE) {
            Map<String, Object> periodInfo = new HashMap<>();
            periodInfo.put("enabled", periodData.enabled);
            periodInfo.put("periodType", periodData.periodType);
            periodInfo.put("periodStartDate", periodData.periodStartDate);
            periodInfo.put("nextResetDate", periodData.nextResetDate);
            periodInfo.put("daysLeft", periodData.daysLeft);
            periodInfo.put("lastResetDate", periodData.lastResetDate);

            DatabaseManager.savePeriodInfo(userId, periodInfo);
        } else {
            savePeriodsData();
        }

        return generateResetNotification(
                periodData,
                incomeCategories,
                expenseCategories,
                incomeTotal,
                expenseTotal,
                balance
        );
    }

    private static Map<Long, String> pendingNotifications = new ConcurrentHashMap<>();

    private static void saveNotificationForUser(Long userId, String message) {
        pendingNotifications.put(userId, message);
    }

    public static String getPendingNotification(Long userId) {
        return pendingNotifications.remove(userId);
    }

    private static String generateResetNotification(UserPeriods periodData,
                                                    Map<String, Double> incomeCategories,
                                                    Map<String, Double> expenseCategories,
                                                    double incomeTotal, double expenseTotal,
                                                    double balance) {
        try {
            StringBuilder incomeDetails = new StringBuilder();
            if (!incomeCategories.isEmpty()) {
                boolean hasIncome = false;
                for (Map.Entry<String, Double> entry : incomeCategories.entrySet()) {
                    if (entry.getValue() > 0) {
                        incomeDetails.append("  • ").append(entry.getKey())
                                .append(": ").append(String.format("%.2f", entry.getValue()))
                                .append(" руб\n");
                        hasIncome = true;
                    }
                }
                if (!hasIncome) {
                    incomeDetails.append("  (нет доходов)\n");
                }
            } else {
                incomeDetails.append("  (нет доходов)\n");
            }

            StringBuilder expenseDetails = new StringBuilder();
            if (!expenseCategories.isEmpty()) {
                boolean hasExpense = false;
                for (Map.Entry<String, Double> entry : expenseCategories.entrySet()) {
                    if (entry.getValue() > 0) {
                        expenseDetails.append("  • ").append(entry.getKey())
                                .append(": ").append(String.format("%.2f", entry.getValue()))
                                .append(" руб\n");
                        hasExpense = true;
                    }
                }
                if (!hasExpense) {
                    expenseDetails.append("  (нет расходов)\n");
                }
            } else {
                expenseDetails.append("  (нет расходов)\n");
            }

            String periodName = getPeriodNameForNotification(periodData.periodType);
            String message = MessageFormat.format("""
🔄 Период сброшен

Завершен период: {0}
Новый период начат: {1}

📊 Итоги завершенного периода:
    Доходы: {2} руб
{3}
    Расходы: {4} руб
{5}
    Баланс: {6} руб

⏰ Следующий сброс: {7}
⏳ Дней до сброса: {8}

    Все суммы обнулены, начинаем новый период!""",
                    periodName,
                    LocalDate.now(),
                    String.format("%.2f", incomeTotal),
                    "* Детали по доходам:\n" + incomeDetails,
                    String.format("%.2f", expenseTotal),
                    "* Детали по расходам:\n" + expenseDetails,
                    String.format("%.2f", balance),
                    periodData.nextResetDate,
                    periodData.daysLeft
            );

            return message;

        } catch (Exception e) {
            System.err.println("Ошибка формирования уведомления: " + e.getMessage());
            return "🔄 Период был успешно сброшен. Начинаем новый период!";
        }
    }

    private static String getPeriodNameForNotification(String periodType) {
        switch (periodType) {
            case "day": return "День";
            case "week": return "Неделя";
            case "month": return "Месяц";
            default: return "Период";
        }
    }

    private static void startPeriodResetMonitorThread() {
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60 * 60 * 1000L);
                    checkAndResetAllPeriods();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private static void checkAndResetAllPeriods() {
        LocalDate currentDate = LocalDate.now();

        for (Map.Entry<String, UserPeriods> entry : allPeriods.entrySet()) {
            UserPeriods periodData = entry.getValue();

            if (periodData.enabled && periodData.nextResetDate != null && !periodData.nextResetDate.isEmpty()) {
                try {
                    LocalDate nextResetDate = LocalDate.parse(periodData.nextResetDate, DateTimeFormatter.ISO_LOCAL_DATE);

                    if (currentDate.isAfter(nextResetDate) || currentDate.isEqual(nextResetDate)) {
                        try {
                            Long userId = Long.parseLong(entry.getKey());
                            autoResetPeriod(userId);

                            System.out.println("Уведомление о сбросе периода сохранено для пользователя: " + userId);
                        } catch (NumberFormatException e) {
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка парсинга даты в checkAndResetAllPeriods для пользователя " +
                            entry.getKey() + ": " + e.getMessage());
                }
            }
        }
    }

    public static void deleteUserData(Long userId) {
        if (USE_DATABASE) {
            DatabaseManager.deleteUserData(userId);
        } else {
            allCategories.remove(getUserKey(userId, "income"));
            allCategories.remove(getUserKey(userId, "expense"));

            allPeriods.remove(getPeriodKey(userId));

            saveAllData();
        }

        pendingNotifications.remove(userId);
        System.out.println("Удалены данные пользователя: " + userId);
    }
}