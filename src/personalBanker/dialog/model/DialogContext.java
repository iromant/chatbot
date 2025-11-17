//все, что можно узнать про состояние диалога на данный момент
package personalBanker.dialog.model;

import personalBanker.dialog.states.DialogState;

public class DialogContext {
    private final UserSession userSession;
    private final String userInput;
    private DialogState nextState;

    public DialogContext(UserSession userSession, String userInput) {
        this.userSession = userSession;
        this.userInput = userInput;
    }

    public UserSession getUserSession() {return userSession;}

    public String getUserInput() {return userInput;}

    public DialogState getNextState() {return nextState;}

    public void setNextState(DialogState nextState) {this.nextState = nextState;}

    public boolean hasNextState() {return nextState != null;}
}