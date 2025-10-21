// StartState.java
package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.CategoriesMessage;
import personalBanker.messageprovider.MessageProvider;

public class StartState implements DialogState {
    private final MessageProvider messageProvider;

    public StartState() {
        this.messageProvider = new CategoriesMessage();
    }

    @Override
    public String onEnter() {
        return messageProvider.getMessage("welcome");
    }

    @Override
    public String userRequest(DialogContext context) {
        String input = context.getUserInput();

        switch (input) {
            case "/start":
                return "Бот перезапущен!\n\n" + onEnter();
            case "/menu":
            case "меню":
                context.setNextState(new MainState());
                return "Переход в главное меню...";
            case "/help":
            case "помощь":
                context.setNextState(new HelpState());
                return "Переход в справку...";
            default:
                return "Не понял команду, нажмите 'меню' или 'menu' для перехода в главное меню";
        }
    }

    @Override
    public DialogState goNextState(DialogContext context) {
        return context.hasNextState() ? context.getNextState() : this;
    }
}