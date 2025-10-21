// DialogManager.java
package personalBanker.dialog.manager;

import personalBanker.dialog.model.*;
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
            UserSession userSession = sessionManager.getOrCreateSession(userId);
            DialogContext context = new DialogContext(userSession, userInput, messageProvider);
            DialogState currentState = userSession.getCurrentState();

            // Отладочная информация
            System.out.println("Текущее состояние: " + currentState.getClass().getSimpleName());
            System.out.println("Ввод пользователя: " + userInput);

            String response = currentState.userRequest(context);
            DialogState nextState = currentState.goNextState(context);

            System.out.println("Ответ: " + response);
            System.out.println("Следующее состояние: " + (nextState != null ? nextState.getClass().getSimpleName() : "null"));

            if (nextState != null && nextState != currentState) {
                performStateTransition(userSession, nextState);
                String enterMessage = nextState.onEnter();
                if (enterMessage != null && !enterMessage.trim().isEmpty()) {
                    response += "\n" + enterMessage;
                }
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return messageProvider.getMessage("error.general");
        }
    }

    public String handleUserStart(Long userId) {
        UserSession userSession = sessionManager.getOrCreateSession(userId);
        return userSession.getCurrentState().onEnter();
    }

    public String goBack(Long userId) {
        UserSession userSession = sessionManager.getSession(userId);
        if (userSession == null) {
            return messageProvider.getMessage("error.operation.cancelled");
        }

        userSession.goBack();
        return userSession.getCurrentState().onEnter();
    }

    private void performStateTransition(UserSession session, DialogState nextState) {
        session.newCurrentState(nextState);
    }


}