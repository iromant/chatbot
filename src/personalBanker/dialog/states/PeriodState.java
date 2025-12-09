package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.dialog.storage.UserCategoryStorage;
import personalBanker.messageprovider.AggregatorMessage;

import java.util.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PeriodState implements DialogState {
    private final AggregatorMessage messageProvider;
    private final Long userId;

    private enum SubState {
        MAIN,
        SET_PERIOD,
        VIEW_INFO,
        MANUAL_RESET
    }

    private SubState currentSubState;

    public PeriodState(Long userId) {
        this.messageProvider = new AggregatorMessage();
        this.userId = userId;
        this.currentSubState = SubState.MAIN;
    }

    @Override
    public String onEnter() {
        return showPeriodMenu();
    }

    @Override
    public DialogState goNextState(DialogContext context) {
        return context.hasNextState() ? context.getNextState() : this;
    }

    @Override
    public String userRequest(DialogContext context) {
        String input = context.getUserInput();

        // Обработка кнопки назад ВСЕГДА
        if (input.equalsIgnoreCase("назад") || input.equals("BACK")) {
            return handleBackButton();
        }

        // Обработка колбэков
        if (input.startsWith("PERIOD_")) {
            return handlePeriodCallback(input);
        }

        return "Пожалуйста, используйте кнопки для выбора";
    }

    @Override
    public String getCurrentSubState() {
        return currentSubState.name();
    }

    private String handleBackButton() {
        switch (currentSubState) {
            case MAIN:
                return onEnter();
            case SET_PERIOD:
            case VIEW_INFO:
            case MANUAL_RESET:
                currentSubState = SubState.MAIN;
                return showPeriodMenu();
            default:
                currentSubState = SubState.MAIN;
                return showPeriodMenu();
        }
    }

    private String handlePeriodCallback(String callbackData) {
        switch (callbackData) {
            case "PERIOD_SET":
                currentSubState = SubState.SET_PERIOD;
                return showPeriodSelection();

            case "PERIOD_INFO":
                currentSubState = SubState.VIEW_INFO;
                return showPeriodInfo();

            case "PERIOD_RESET_NOW":
                currentSubState = SubState.MANUAL_RESET;
                return messageProvider.getMessage("period.reset.now");

            case "PERIOD_CONFIRM_RESET":
                return manualResetPeriod();

            case "PERIOD_CANCEL_RESET":
                currentSubState = SubState.MAIN;
                return showPeriodMenu();

            case "PERIOD_DAY":
                return setPeriod("day");

            case "PERIOD_WEEK":
                return setPeriod("week");

            case "PERIOD_MONTH":
                return setPeriod("month");

            case "PERIOD_DISABLE":
                return disablePeriod();

            default:
                return "Неизвестная команда";
        }
    }

    private String showPeriodMenu() {
        Map<String, Object> periodInfo = UserCategoryStorage.getPeriodInfo(userId);
        boolean enabled = (Boolean) periodInfo.get("enabled");
        String periodType = (String) periodInfo.get("periodType");

        StringBuilder sb = new StringBuilder();
        sb.append("Настройки периода\n\n");

        if (enabled) {
            String periodName = getPeriodName(periodType);
            String nextReset = (String) periodInfo.get("nextResetDate");
            Long daysLeft = (Long) periodInfo.get("daysLeft");

            sb.append("✅ Периодический сброс ВКЛЮЧЕН\n");
            sb.append("Период: ").append(periodName).append("\n");
            sb.append("Следующий сброс: ").append(formatDate(nextReset)).append("\n");
            sb.append("Осталось дней: ").append(daysLeft).append("\n\n");

            sb.append("Выберите действие:");
        } else {
            sb.append("❌ Периодический сброс ВЫКЛЮЧЕН\n\n");
            sb.append("Вы можете настроить автоматический сброс сумм доходов и расходов.\n");
            sb.append("Суммы будут обнуляться через выбранный период времени.\n\n");
            sb.append("Выберите действие:");
        }

        return sb.toString();
    }

    private String showPeriodSelection() {
        return "Выберите период для автоматического сброса:\n\n" +
                "• День - сброс каждые 24 часа\n" +
                "• Неделя - сброс каждые 7 дней\n" +
                "• Месяц - сброс каждые 30 дней\n\n" +
                "Период действует одновременно для доходов и расходов.";
    }

    private String showPeriodInfo() {
        Map<String, Object> periodInfo = UserCategoryStorage.getPeriodInfo(userId);
        boolean enabled = (Boolean) periodInfo.get("enabled");
        String periodType = (String) periodInfo.get("periodType");
        String startDate = (String) periodInfo.get("periodStartDate");
        String nextReset = (String) periodInfo.get("nextResetDate");
        Long daysLeft = (Long) periodInfo.get("daysLeft");

        StringBuilder sb = new StringBuilder();
        sb.append("📊 Информация о периоде\n\n");

        sb.append("Статус: ").append(enabled ? "✅ ВКЛЮЧЕН" : "❌ ВЫКЛЮЧЕН").append("\n");

        if (enabled) {
            sb.append("Период: ").append(getPeriodName(periodType)).append("\n");
            sb.append("Начало периода: ").append(formatDate(startDate)).append("\n");
            sb.append("Следующий сброс: ").append(formatDate(nextReset)).append("\n");
            sb.append("Осталось дней: ").append(daysLeft).append("\n");

            // Показываем информацию о доходах и расходах
            double incomeTotal = getTotalForType("income");
            double expenseTotal = getTotalForType("expense");

            sb.append("\nТекущие суммы в периоде:\n");
            sb.append("• Доходы: ").append(String.format("%.2f", incomeTotal)).append(" руб\n");
            sb.append("• Расходы: ").append(String.format("%.2f", expenseTotal)).append(" руб\n");
            sb.append("• Баланс: ").append(String.format("%.2f", incomeTotal - expenseTotal)).append(" руб\n");
        }

        sb.append("\nПри сбросе периода обнуляются суммы доходов и расходов.\n");
        sb.append("Лимиты и цели сохраняются.");

        return sb.toString();
    }

    private double getTotalForType(String type) {
        Map<String, Double> categories = UserCategoryStorage.loadUserCategories(userId, type);
        return categories.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    private String setPeriod(String periodType) {
        UserCategoryStorage.setUserPeriod(userId, periodType);

        String periodName = getPeriodName(periodType);

        return "✅ Периодический сброс установлен\n\n" +
                "Период: " + periodName + "\n" +
                "Суммы доходов и расходов будут автоматически сбрасываться каждые " +
                (periodType.equals("day") ? "24 часа" :
                        periodType.equals("week") ? "7 дней" : "30 дней") + ".\n\n" +
                "Настройки периода применяются одновременно к доходам и расходам.";
    }

    private String disablePeriod() {
        UserCategoryStorage.disablePeriod(userId);

        return "✅ Периодический сброс выключен\n\n" +
                "Суммы доходов и расходов больше не будут автоматически сбрасываться.";
    }

    private String manualResetPeriod() {
        boolean success = UserCategoryStorage.manualResetPeriod(userId);

        if (success) {
            return "✅ Суммы успешно сброшены\n\n" +
                    "Все суммы доходов и расходов обнулены.\n" +
                    "Лимиты и цели сохранены.\n" +
                    "Новый период начат.";
        } else {
            return "❌ Не удалось сбросить суммы\n\n" +
                    "Попробуйте еще раз или проверьте настройки периода.";
        }
    }

    private String getPeriodName(String periodType) {
        switch (periodType) {
            case "day": return "День";
            case "week": return "Неделя";
            case "month": return "Месяц";
            default: return "Неизвестно";
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "не установлено";
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return dateStr;
        }
    }
}