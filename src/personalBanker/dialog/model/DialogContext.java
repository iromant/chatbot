package personalBanker.dialog.model;

import personalBanker.dialog.states.DialogState;
import personalBanker.messageprovider.MessageProvider;

public class DialogContext {
    private final UserSession userSession;
    private final String userInput;
    private final MessageProvider messageProvider;
    private DialogState nextState;

    public DialogContext(UserSession userSession, String userInput, MessageProvider messageProvider) {
        this.userSession = userSession;
        this.userInput = userInput;
        this.messageProvider = messageProvider;
        this.nextState = null;
    }

    public UserSession getUserSession() {
        return userSession;
    }

    public String getUserInput() {
        return userInput;
    }

    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    // Методы для работы с nextState
    public DialogState getNextState() {
        return nextState;
    }

    public void setNextState(DialogState nextState) {
        this.nextState = nextState;
    }

    public boolean hasNextState() {
        return nextState != null;
    }

    public void clearNextState() {
        this.nextState = null;
    }
}