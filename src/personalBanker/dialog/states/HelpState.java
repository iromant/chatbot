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
        String input = context.getUserInput();

        switch (input) {
            case "/start":
                context.setNextState(new StartState());
                return "Перезапуск бота...";
            case "/menu":
            case "/back":
                context.setNextState(new MainState());
                return "Возвращение на главное меню...";
            default:
                return messageProvider.getMessage("help.main");
        }
    }

    @Override
    public DialogState goNextState(DialogContext context) {
        return context.hasNextState() ? context.getNextState() : this;
    }
}