//нужен для запоминания пользователя, с которым идет диалог
package personalBanker.dialog.model;
import personalBanker.dialog.states.DialogState;
import personalBanker.dialog.states.StartState;

public class UserSession {
    private final Long userId;
    private DialogState currentState;
    private DialogState previousState;

    public UserSession(Long userId) {
        this.userId = userId;
        this.currentState = new StartState(); //возможно тут будет что-то другое, но пока так
        this.previousState = null;
    }

    public Long getUserId() {
        return userId;
    }

    public DialogState getCurrentState() {
        return currentState;
    }

    public DialogState getPreviousState() {
        return previousState;
    }

    public void newCurrentState(DialogState newState) {
        if (newState != null) {
            this.previousState = this.currentState;
            this.currentState = newState;
        }
    }

    public void goBack() {
        if (this.previousState != null) {
            DialogState temp = this.currentState;
            this.currentState = this.previousState;
            this.previousState = temp;
        }
    }
}
