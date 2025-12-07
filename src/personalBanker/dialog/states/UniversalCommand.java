package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.AggregatorMessage;

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
                context.setNextState(context.getUserSession().getOrCreateState(IncomeState.class));
                return Optional.of("");

            case "расходы":
            case "расход":
            case "expense_menu":
                context.setNextState(context.getUserSession().getOrCreateState(ExpenseState.class));
                return Optional.of("");

            case "мои данные":
            case "/mydata":
                try {
                    if (context.getUserSession().getCurrentState() instanceof FinanceState) {
                        FinanceState financeState = (FinanceState) context.getUserSession().getCurrentState();
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
                return Optional.of("Не в режиме доходов/расходов");

            case "/delete_my_data":
            case "удалить мои данные":
                return Optional.of("Для удаления данных используйте команду напрямую");
            case "очистить данные":
            case "удалить данные":
            case "clear_data":
                return Optional.of("Для удаления всех данных нажмите кнопку 'Удалить данные' в главном меню.");
            default:
                return Optional.empty();
        }
    }
}