//отвечает за управление диалогом с пользователем
package personalBanker.dialog.manager;

import personalBanker.dialog.model.*;
import personalBanker.dialog.states.DialogState;
import personalBanker.messageprovider.AggregatorMessage;

public class DialogManager {
    private final UserSessionManager sessionManager;
    private final AggregatorMessage messageProvider;

    public DialogManager(UserSessionManager sessionManager,
                         AggregatorMessage messageProvider) {
        this.sessionManager = sessionManager;
        this.messageProvider = messageProvider;
    }

    public String processUserInput(Long userId, String userInput) {
        try {
            UserSession userSession = sessionManager.getOrCreateSession(userId);
            DialogContext context = new DialogContext(userSession, userInput);
            DialogState currentState = userSession.getCurrentState();

            String response = currentState.userRequest(context);
            DialogState nextState = currentState.goNextState(context);

            if (nextState != null && nextState != currentState) {
                userSession.newCurrentState(nextState);
                String enterMessage = nextState.onEnter();
                if (enterMessage != null && !enterMessage.trim().isEmpty()) {
                    response = enterMessage;
                }
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return messageProvider.getMessage("error.general");
        }
    }
}