//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package personalBanker.dialog.manager;

import personalBanker.dialog.model.DialogContext;
import personalBanker.dialog.model.UserSession;
import personalBanker.dialog.states.DialogState;
import personalBanker.messageprovider.MessageProvider;

public class DialogManager {
    private final UserSessionManager sessionManager;
    private final MessageProvider messageProvider;

    public DialogManager(UserSessionManager sessionManager, MessageProvider messageProvider) {
        this.sessionManager = sessionManager;
        this.messageProvider = messageProvider;
    }

    public String processUserInput(Long userId, String userInput) {
        try {
            UserSession userSession = this.sessionManager.getOrCreateSession(userId);
            DialogContext context = new DialogContext(userSession, userInput, this.messageProvider);
            DialogState currentState = userSession.getCurrentState();
            String response = currentState.userRequest(context);
            DialogState nextState = currentState.goNextState(context);
            if (nextState != null && nextState != currentState) {
                userSession.newCurrentState(nextState);
                String enterMessage = nextState.onEnter();
                if (enterMessage != null && !enterMessage.trim().isEmpty()) {
                    response = response + "\n" + enterMessage;
                }
            }

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return this.messageProvider.getMessage("error.general");
        }
    }

    public String handleUserStart(Long userId) {
        UserSession userSession = this.sessionManager.getOrCreateSession(userId);
        return userSession.getCurrentState().onEnter();
    }

    public String goBack(Long userId) {
        UserSession userSession = this.sessionManager.getSession(userId);
        if (userSession == null) {
            return this.messageProvider.getMessage("error.operation.cancelled");
        } else {
            userSession.goBack();
            return userSession.getCurrentState().onEnter();
        }
    }

    public String handleUserMenu(Long chatId) {
        return "\ud83d\udccb Главное меню:\nВыберите нужное действие:";
    }
}

