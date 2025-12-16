package personalBanker.dialog.manager;

import personalBanker.dialog.states.*;
import personalBanker.dialog.model.*;

import java.util.*;

public class DialogManager {
    private final UserSessionManager sessionManager;

    public DialogManager(UserSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public String processUserInput(Long userId, String input) {
        try {
            // Получаем сессию пользователя
            UserSession session = sessionManager.getOrCreateSession(userId);
            DialogState currentState = session.getCurrentState();

            // 1. Проверяем универсальные команды (кроме "назад" внутри состояний)
            if (!shouldSkipUniversalCommand(currentState, input)) {
                DialogContext universalContext = new DialogContext(session, input);
                Optional<String> universalResult = UniversalCommand.executeCommand(input, universalContext);

                if (universalResult.isPresent()) {
                    // Если команда установила следующее состояние
                    if (universalContext.hasNextState()) {
                        DialogState nextState = universalContext.getNextState();
                        session.newCurrentState(nextState);
                        return nextState.onEnter();
                    }
                    // Если команда вернула текстовый ответ
                    return universalResult.get();
                }
            }

            // 2. Стандартная обработка текущего состояния
            DialogContext context = new DialogContext(session, input);
            String response = currentState.userRequest(context);

            // 3. Обработка перехода состояния
            if (context.hasNextState()) {
                DialogState nextState = context.getNextState();
                session.newCurrentState(nextState);
                return nextState.onEnter();
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return "Произошла ошибка при обработке команды. Пожалуйста, попробуйте еще раз.";
        }
    }

    // Проверяем, нужно ли пропускать универсальную команду
    private boolean shouldSkipUniversalCommand(DialogState currentState, String input) {
        if (input.equalsIgnoreCase("назад") || input.equals("BACK")) {
            String currentSubState = currentState.getCurrentSubState();

            if (currentState instanceof FinanceState) {
                return !"MAIN_MENU".equals(currentSubState);
            } else if (currentState instanceof PeriodState) {
                return !"MAIN".equals(currentSubState);
            }
        }
        return false;
    }

    public DialogState getCurrentState(Long userId) {
        UserSession session = sessionManager.getOrCreateSession(userId);
        DialogState state = session.getCurrentState();
        if (state == null) {
            state = new StartState();
            session.newCurrentState(state);
        }
        return state;
    }

    public String getCurrentSubState(Long userId) {
        DialogState state = getCurrentState(userId);
        return state.getCurrentSubState();
    }

    public static class ChartResponse {
        private final String statistics;

        public ChartResponse(String statistics) {
            this.statistics = statistics;
        }

        public String getStatistics() {
            return statistics;
        }
    }

    public ChartResponse processStatistics(Long userId) {
        DialogState currentState = getCurrentState(userId);

        if (currentState instanceof FinanceState) {
            FinanceState financeState = (FinanceState) currentState;
            UserSession session = sessionManager.getOrCreateSession(userId);
            String stats = financeState.userRequest(new DialogContext(session, "stats"));
            return new ChartResponse(stats);
        }

        return new ChartResponse("Статистика доступна только в режиме доходов или расходов.");
    }
}