package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.CategoriesMessage;
import personalBanker.messageprovider.MessageProvider;

public class MainState implements DialogState {
    private final MessageProvider messageProvider;

    public MainState() {
        this.messageProvider = new CategoriesMessage();
    }

    @Override
    public String onEnter() {
        return messageProvider.getMessage("menu.main");
    }

    @Override
    public String userRequest(DialogContext context) {
        String input = context.getUserInput().toLowerCase().trim();

        switch (input) {
            case "/help":
            case "help":
            case "помощь":
            case "справка":
                context.setNextState(new HelpState());
                return "📖 Переход в раздел справки...";

            case "/back":
            case "back":
            case "назад":
            case "возврат":
                if (context.getUserSession().getPreviousState() != null) {
                    context.setNextState(context.getUserSession().getPreviousState());
                    return "↩️ Возврат в предыдущее состояние...";
                } else {
                    return messageProvider.getMessage("error.operation.cancelled");
                }

                // В методе userRequest MainState.java замените создание состояний:
            case "1":
            case "доходы":
            case "incomes":
            case "income":
            case "доход":
                IncomeState incomeState = (IncomeState) context.getUserSession().getOrCreateState(IncomeState.class);
                context.setNextState(incomeState);
                return "Переход к управлению доходами...";

            case "2":
            case "расходы":
            case "expenses":
            case "expense":
            case "расход":
                ExpenseState expenseState = (ExpenseState) context.getUserSession().getOrCreateState(ExpenseState.class);
                context.setNextState(expenseState);
                return "Переход к управлению расходами...";

            case "/start":
            case "start":
            case "старт":
            case "начать":
                context.setNextState(new StartState());
                return "Перезапуск бота...";

            case "/menu":
            case "menu":
            case "меню":
                return onEnter();

            default:
                return messageProvider.getMessage("error.unknown.command");
        }
    }

    @Override
    public DialogState goNextState(DialogContext context) {
        return context.hasNextState() ? context.getNextState() : this;
    }
}
