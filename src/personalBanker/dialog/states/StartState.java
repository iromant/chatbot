//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package personalBanker.dialog.states;

import personalBanker.dialog.model.DialogContext;
import personalBanker.messageprovider.CategoriesMessage;
import personalBanker.messageprovider.MessageProvider;

public class StartState implements DialogState {
    private final MessageProvider messageProvider = new CategoriesMessage();

    public String onEnter() {
        return this.messageProvider.getMessage("welcome");
    }

    public String userRequest(DialogContext context) {
        switch (context.getUserInput().toLowerCase().trim()) {
            case "/start":
            case "start":
            case "старт":
            case "начать":
                return "Бот перезапущен!\n\n" + this.onEnter();
            case "/menu":
            case "menu":
            case "меню":
                context.setNextState(new MainState());
                return "Переход в главное меню...";
            case "/help":
            case "help":
            case "помощь":
            case "справка":
                context.setNextState(new HelpState());
                return "Переход в справку...";
            default:
                return "Не понял команду, нажмите 'меню' или 'menu' для перехода в главное меню";
        }
    }

    public DialogState goNextState(DialogContext context) {
        return (DialogState)(context.hasNextState() ? context.getNextState() : this);
    }
}

