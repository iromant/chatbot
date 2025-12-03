package personalBanker.dialog.model;

import personalBanker.dialog.states.DialogState;
import personalBanker.dialog.states.StartState;
import personalBanker.dialog.states.FinanceState;

import java.util.HashMap;
import java.util.Map;

public class UserSession {
    private DialogState currentState;
    private DialogState previousState;
    private final Map<Class<? extends DialogState>, DialogState> stateInstances;
    private final Long userId;

    public UserSession(Long userId) {
        this.userId = userId;
        this.stateInstances = new HashMap<>();
        this.currentState = getOrCreateState(StartState.class);
        this.previousState = null;
    }

    public DialogState getOrCreateState(Class<? extends DialogState> stateClass) {
        return stateInstances.computeIfAbsent(stateClass, clazz -> {
            try {
                if (FinanceState.class.isAssignableFrom(clazz)) {
                    return clazz.getConstructor(Long.class).newInstance(userId);
                } else {
                    return clazz.newInstance();
                }
            } catch (Exception e) {
                throw new RuntimeException("Ошибка создания состояния: " + clazz.getSimpleName());
            }
        });
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
            stateInstances.putIfAbsent(newState.getClass(), newState);
        }
    }
}