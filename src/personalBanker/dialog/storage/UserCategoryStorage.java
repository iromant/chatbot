package personalBanker.dialog.storage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserCategoryStorage {
    private static final String STORAGE_DIR = "user_data";
    private static final Map<Long, Map<String, Map<String, Double>>> cache = new HashMap<>();

    static {
        new File(STORAGE_DIR).mkdirs();
    }

    public static Map<String, Double> loadUserCategories(Long userId, String type) {
        String key = userId + "_" + type;

        if (cache.containsKey(userId)) {
            Map<String, Map<String, Double>> userData = cache.get(userId);
            if (userData.containsKey(type)) {
                return new HashMap<>(userData.get(type));
            }
        }

        File file = new File(STORAGE_DIR + "/" + userId + "_" + type + ".dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                @SuppressWarnings("unchecked")
                Map<String, Double> categories = (Map<String, Double>) ois.readObject();

                cache.computeIfAbsent(userId, k -> new HashMap<>())
                        .put(type, new HashMap<>(categories));

                return categories;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new HashMap<>();
    }

    public static void saveUserCategories(Long userId, String type, Map<String, Double> categories) {
        try {
            File file = new File(STORAGE_DIR + "/" + userId + "_" + type + ".dat");

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(categories);
            }

            cache.computeIfAbsent(userId, k -> new HashMap<>())
                    .put(type, new HashMap<>(categories));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}