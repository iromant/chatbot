package personalBanker.dialog.model;

import personalBanker.dialog.states.DialogState;
import personalBanker.dialog.states.StartState;
import java.util.HashMap;
import java.util.Map;

public class UserSession {
    private final Long userId;
    private DialogState currentState;
    private DialogState previousState;
    private final Map<Class<? extends DialogState>, DialogState> stateInstances; // Храним экземпляры состояний

    public UserSession(Long userId) {
        this.userId = userId;
        this.stateInstances = new HashMap<>();
        this.currentState = getOrCreateState(StartState.class);
        this.previousState = null;
    }

    // Метод для получения или создания состояния
    public DialogState getOrCreateState(Class<? extends DialogState> stateClass) {
        return stateInstances.computeIfAbsent(stateClass, clazz -> {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Ошибка создания состояния: " + clazz.getSimpleName());
            }
        });
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
            // Сохраняем экземпляр в кэше
            stateInstances.put(newState.getClass(), newState);
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
