package personalBanker.dialog.states;
import personalBanker.dialog.model.DialogContext;

public interface DialogState {
    String onEnter();

    String userRequest(DialogContext context);
    DialogState goNextState(DialogContext context);

    default String getCurrentSubState() {
        return "MAIN_MENU"; // значение по умолчанию
    }
}