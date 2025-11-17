package personalBanker.dialog.manager;

import personalBanker.dialog.model.UserSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionManager {
    private final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();

    public UserSession getOrCreateSession(Long userId) {
        return sessions.computeIfAbsent(userId, UserSession::new);
    }

    public UserSession getSession(Long userId) {
        return sessions.get(userId);
    }

    public void remove(Long userId) {
        sessions.remove(userId);
    }
}