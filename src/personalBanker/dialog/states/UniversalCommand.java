package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;

import java.util.Optional;
import java.util.Map;

public class UniversalCommand {

    public static Optional<String> executeCommand(String input, DialogContext context) {
        switch (input.toLowerCase()) {
            case "старт":
            case "/start":
                context.setNextState(new StartState());
                return Optional.of("");

            case "меню":
            case "main_menu":
                context.setNextState(new MainState());
                return Optional.of("");

            case "справка":
            case "help":
                context.setNextState(new HelpState());
                return Optional.of("");

            case "назад":
            case "back":
                if (context.getUserSession().getPreviousState() != null) {
                    context.setNextState(context.getUserSession().getPreviousState());
                    return Optional.of("");
                }
                return Optional.of("Нельзя вернуться назад");

            case "доходы":
            case "доход":
            case "income_menu":
                Long userId = context.getUserSession().getUserId();
                context.setNextState(new IncomeState(userId));
                return Optional.of("");

            case "расходы":
            case "расход":
            case "expense_menu":
                userId = context.getUserSession().getUserId();
                context.setNextState(new ExpenseState(userId));
                return Optional.of("");

            case "период":
            case "периоды":
            case "period_menu":
            case "настройка периодов":
                userId = context.getUserSession().getUserId();
                context.setNextState(new PeriodState(userId));
                return Optional.of("");

                //интересная кнопка, я в шоке
            case "мои данные":
            case "/mydata":
                try {
                    DialogState currentState = context.getUserSession().getCurrentState();
                    if (currentState instanceof FinanceState) {
                        FinanceState financeState = (FinanceState) currentState;
                        StringBuilder sb = new StringBuilder();
                        sb.append("Ваши категории:\n\n");

                        for (Map.Entry<String, Double> entry : financeState.getCategoriesMap().entrySet()) {
                            sb.append("• ").append(entry.getKey())
                                    .append(": ").append(String.format("%.2f", entry.getValue()))
                                    .append(" руб\n");
                        }

                        return Optional.of(sb.toString());
                    }
                } catch (Exception e) {
                    return Optional.of("Не удалось получить данные");
                }
                return Optional.of("Вы не в режиме доходов/расходов");

            default:
                return Optional.empty();
        }
    }
}