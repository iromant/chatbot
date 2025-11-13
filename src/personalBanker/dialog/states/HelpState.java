// HelpState.java
package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.CategoriesMessage;
import personalBanker.messageprovider.MessageProvider;

public class HelpState implements DialogState {
    private final MessageProvider messageProvider;

    public HelpState() {
        this.messageProvider = new CategoriesMessage();
    }

    @Override
    public String onEnter() {
        return messageProvider.getMessage("help.main");
    }

    @Override
    public String userRequest(DialogContext context) {
        String input = context.getUserInput().toLowerCase().trim();

        switch (input) {
            case "/start":
            case "start":
            case "старт":
            case "начать":
                context.setNextState(new StartState());
                return "Перезапуск бота...";
            case "/menu":
            case "menu":
            case "меню":
                context.setNextState(new MainState());
                return "Возвращение на главное меню...";
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
            default:
                return messageProvider.getMessage("help.main");
        }
    }

    @Override
    public DialogState goNextState(DialogContext context) {
        return context.hasNextState() ? context.getNextState() : this;
    }
}
