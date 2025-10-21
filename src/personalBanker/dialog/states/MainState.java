// MainState.java
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
        String input = context.getUserInput();

        switch (input) {
            case "/help":
            case "помощь":
                context.setNextState(new HelpState());
                return "Переход в справку...";
            case "/back":
            case "назад":
                if (context.getUserSession().getPreviousState() != null) {
                    context.setNextState(context.getUserSession().getPreviousState());
                    return "Возврат в предыдущее состояние...";
                } else {
                    return "Нет предыдущего состояния";
                }
            default:
                return messageProvider.getMessage("error.unknown.command");
        }
    }

    @Override
    public DialogState goNextState(DialogContext context) {
        return context.hasNextState() ? context.getNextState() : this;
    }
}