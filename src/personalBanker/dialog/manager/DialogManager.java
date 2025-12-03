package personalBanker.dialog.manager;

import personalBanker.dialog.model.*;
import personalBanker.dialog.states.DialogState;
import personalBanker.dialog.states.FinanceState;
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

    public DialogState getCurrentState(Long userId) {
        UserSession userSession = sessionManager.getOrCreateSession(userId);
        return userSession.getCurrentState();
    }

    public String getCurrentSubState(Long userId) {
        return getCurrentState(userId).getCurrentSubState();
    }

    public ChartResponse processStatistics(Long userId) {
        try {
            UserSession userSession = sessionManager.getOrCreateSession(userId);
            DialogState currentState = userSession.getCurrentState();

            if (currentState instanceof FinanceState) {
                FinanceState financeState = (FinanceState) currentState;
                String stats = financeState.userRequest(
                        new DialogContext(userSession, "статистика")
                );

                String chartPath = financeState.getChartPath();

                return new ChartResponse(stats, chartPath);
            }

            return new ChartResponse("Эта команда доступна только в режиме доходов/расходов", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ChartResponse("Ошибка при генерации статистики", null);
        }
    }

    public static class ChartResponse {
        private final String statistics;
        private final String chartPath;

        public ChartResponse(String statistics, String chartPath) {
            this.statistics = statistics;
            this.chartPath = chartPath;
        }

        public String getStatistics() {
            return statistics;
        }

        public String getChartPath() {
            return chartPath;
        }

        public boolean hasChart() {
            return chartPath != null;
        }
    }
}