package personalBanker.dialog.model;

import personalBanker.messageprovider.MessageProvider;
import personalBanker.dialog.states.DialogState;

public class DialogContext {
    private final UserSession userSession;
    private final String userInput;
    private final MessageProvider messageProvider;
    private DialogState nextState;

    public DialogContext(UserSession userSession, String userInput, MessageProvider messageProvider) {
        this.userSession = userSession;
        this.userInput = userInput;
        this.messageProvider = messageProvider;
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

    public DialogState getNextState() {
        return nextState;
    }

    public void setNextState(DialogState nextState) {
        this.nextState = nextState;
    }

    public boolean hasNextState() {
        return nextState != null;
    }
}
