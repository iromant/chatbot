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

    public void clearUserSession(Long userId) {
        UserSession session = sessions.remove(userId);
        if (session != null) {
            System.out.println("Сессия пользователя " + userId + " очищена");
        }
    }

    public void clearAllSessions() {
        int count = sessions.size();
        sessions.clear();
        System.out.println("Очищены все сессии: " + count + " пользователей");
    }
}