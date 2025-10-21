package personalBanker.dialog.manager;

import personalBanker.dialog.model.UserSession;
import java.util.Map;
import java.util.HashMap;

public class UserSessionManager {
    private final Map<Long, UserSession> sessions;

    public UserSessionManager() {
        this.sessions = new HashMap<>();
    }

    public UserSession getOrCreateSession(Long userId) {
        if (userId < 0) {
            throw new IllegalArgumentException("userId must be non-negative");
        }

        UserSession session = sessions.get(userId);
        if (session == null) {
            session = new UserSession(userId);
            sessions.put(userId, session);
        }
        return session;
    }

    public UserSession getSession(Long userId) {
        return sessions.get(userId);
    }

    public void remove(Long userId) {
        sessions.remove(userId);
    }

    public boolean hasSession(Long userId) {
        return sessions.containsKey(userId);
    }
}
