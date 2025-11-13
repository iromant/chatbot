package personalBanker.messageprovider;

public interface MessageProvider {
    String getMessage(String key);
    String getMessage(String key, Object... args);
    boolean containsKey(String key);
    String getCategoryName();
}
