package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;

import java.util.Optional;

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

            default:
                return Optional.empty();
        }
    }
}