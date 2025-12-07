package personalBanker.dialog.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class UserCategoryStorage {
    private static final String STORAGE_DIR = "user_data";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Long> lastAccessTime = new HashMap<>();
    private static final long CLEANUP_INTERVAL = 30 * 24 * 60 * 60 * 1000L;

    static {
        new File(STORAGE_DIR).mkdirs();
        startCleanupThread();
    }

    private static String getFileName(Long userId, String type) {
        return STORAGE_DIR + File.separator + userId + "_" + type + ".json";
    }

    public static Map<String, Double> loadUserCategories(Long userId, String type) {
        String fileName = getFileName(userId, type);
        File file = new File(fileName);

        if (!file.exists()) {
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type mapType = new TypeToken<Map<String, Double>>(){}.getType();
            Map<String, Double> categories = gson.fromJson(reader, mapType);

            lastAccessTime.put(fileName, System.currentTimeMillis());
            return categories != null ? categories : new HashMap<>();

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static void saveUserCategories(Long userId, String type, Map<String, Double> categories) {
        String fileName = getFileName(userId, type);

        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(categories, writer);
            lastAccessTime.put(fileName, System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteUserData(Long userId) {
        try {
            File dir = new File(STORAGE_DIR);
            File[] userFiles = dir.listFiles((d, name) ->
                    name.startsWith(userId + "_") && name.endsWith(".json")
            );

            if (userFiles != null) {
                for (File file : userFiles) {
                    if (file.delete()) {
                        lastAccessTime.remove(file.getAbsolutePath());
                        System.out.println("Удален файл: " + file.getName());
                    }
                }
            }

            lastAccessTime.keySet().removeIf(key -> key.contains(userId + "_"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(24 * 60 * 60 * 1000L);
                    cleanupOldFiles();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    private static void cleanupOldFiles() {
        try {
            File dir = new File(STORAGE_DIR);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

            if (files != null) {
                long currentTime = System.currentTimeMillis();
                for (File file : files) {
                    String filePath = file.getAbsolutePath();
                    Long lastAccess = lastAccessTime.get(filePath);

                    if (lastAccess == null || (currentTime - lastAccess) > CLEANUP_INTERVAL) {
                        if (file.delete()) {
                            lastAccessTime.remove(filePath);
                            System.out.println("Автоочистка: удален старый файл " + file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static StorageStats getStorageStats() {
        File dir = new File(STORAGE_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

        if (files == null) {
            return new StorageStats(0, 0);
        }

        long totalSize = 0;
        for (File file : files) {
            totalSize += file.length();
        }

        return new StorageStats(files.length, totalSize);
    }

    public static class StorageStats {
        public final int fileCount;
        public final long totalSizeBytes;

        public StorageStats(int fileCount, long totalSizeBytes) {
            this.fileCount = fileCount;
            this.totalSizeBytes = totalSizeBytes;
        }

        @Override
        public String toString() {
            return String.format("Файлов: %d, Размер: %.2f KB",
                    fileCount, totalSizeBytes / 1024.0);
        }
    }
    public static void clearAllUserData() {
        try {
            File dir = new File(STORAGE_DIR);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

            if (files != null) {
                int count = 0;
                for (File file : files) {
                    if (file.delete()) {
                        count++;
                        lastAccessTime.remove(file.getAbsolutePath());
                    }
                }
                System.out.println("Очищены все данные: " + count + " файлов");
            }

            lastAccessTime.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}