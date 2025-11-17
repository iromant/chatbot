package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.AggregatorMessage;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class UniversalCommand {

    public enum Command {
        START("/start", "start", "старт", "начать") {
            @Override
            public String execute(DialogContext context, AggregatorMessage messages) {
                context.setNextState(new StartState());
                return "Бот запущен!\n\n";
            }
        },

        MENU("/menu", "menu", "меню") {
            @Override
            public String execute(DialogContext context, AggregatorMessage messages) {
                context.setNextState(new MainState());
                return "";
            }
        },

        HELP("/help", "help", "помощь", "справка") {
            @Override
            public String execute(DialogContext context, AggregatorMessage messages) {
                context.setNextState(new HelpState());
                return "";
            }
        },

        BACK("/back", "back", "назад", "возврат") {
            @Override
            public String execute(DialogContext context, AggregatorMessage messages) {
                if (context.getUserSession().getPreviousState() != null) {
                    context.setNextState(context.getUserSession().getPreviousState());
                    return "";
                }
                return messages.getMessage("error.operation.cancelled");
            }
        },

        INCOME("доходы", "1") {
            @Override
            public String execute(DialogContext context, AggregatorMessage messages) {
                IncomeState incomeState = (IncomeState) context.getUserSession().getOrCreateState(IncomeState.class);
                context.setNextState(incomeState);
                return "";
            }
        },

        EXPENSE("расходы", "2") {
            @Override
            public String execute(DialogContext context, AggregatorMessage messages) {
                ExpenseState expenseState = (ExpenseState) context.getUserSession().getOrCreateState(ExpenseState.class);
                context.setNextState(expenseState);
                return "";
            }
        };

        private final Set<String> aliases;

        Command(String... aliases) {
            this.aliases = Set.of(aliases);
        }

        public abstract String execute(DialogContext context, AggregatorMessage messages);

        public static Optional<Command> fromInput(String input) {
            return Arrays.stream(values())
                    .filter(command -> command.aliases.contains(input))
                    .findFirst();
        }
    }

    public static Optional<String> executeCommand(String input, DialogContext context, AggregatorMessage messages) {
        Optional<Command> command = Command.fromInput(input);
        if (command.isPresent()) {
            return Optional.of(command.get().execute(context, messages));
        }
        return Optional.empty();
    }
}