package personalBanker.dialog.storage;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:personal_banker.db";
    private static Connection connection;
    private static final Map<String, Map<String, Double>> categoriesCache = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Double>> limitsCache = new ConcurrentHashMap<>();

    static {
        initializeDatabase();
    }

    private static synchronized void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            createTables();
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    private static void createTables() throws SQLException {
        String createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS user_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id BIGINT NOT NULL,
                type TEXT NOT NULL,
                category_name TEXT NOT NULL,
                amount REAL DEFAULT 0.0,
                UNIQUE(user_id, type, category_name)
            )
        """;

        String createLimitsTable = """
            CREATE TABLE IF NOT EXISTS user_limits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id BIGINT NOT NULL,
                type TEXT NOT NULL,
                category_name TEXT NOT NULL,
                limit_amount REAL DEFAULT 0.0,
                UNIQUE(user_id, type, category_name)
            )
        """;

        String createPeriodsTable = """
            CREATE TABLE IF NOT EXISTS user_periods (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id BIGINT NOT NULL UNIQUE,
                enabled BOOLEAN DEFAULT 0,
                period_type TEXT DEFAULT 'month',
                period_start_date TEXT,
                next_reset_date TEXT,
                days_left INTEGER DEFAULT 0,
                last_reset_date TEXT
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createCategoriesTable);
            stmt.execute(createLimitsTable);
            stmt.execute(createPeriodsTable);
        }
    }

    private static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static Map<String, Double> loadUserCategories(Long userId, String type) {
        String cacheKey = userId + "_" + type;

        if (categoriesCache.containsKey(cacheKey)) {
            return new HashMap<>(categoriesCache.get(cacheKey));
        }

        Map<String, Double> result = new HashMap<>();
        String query = "SELECT category_name, amount FROM user_categories WHERE user_id = ? AND type = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, type);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("category_name"), rs.getDouble("amount"));
                }
            }

            categoriesCache.put(cacheKey, new HashMap<>(result));

        } catch (SQLException e) {
            System.err.println("Ошибка загрузки категорий: " + e.getMessage());
        }

        return result;
    }

    public static Map<String, Double> loadLimitsGoals(Long userId, String type) {
        String cacheKey = userId + "_" + type + "_limits";

        if (limitsCache.containsKey(cacheKey)) {
            return new HashMap<>(limitsCache.get(cacheKey));
        }

        Map<String, Double> result = new HashMap<>();
        String query = "SELECT category_name, limit_amount FROM user_limits WHERE user_id = ? AND type = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, type);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("category_name"), rs.getDouble("limit_amount"));
                }
            }

            limitsCache.put(cacheKey, new HashMap<>(result));

        } catch (SQLException e) {
            System.err.println("Ошибка загрузки лимитов: " + e.getMessage());
        }

        return result;
    }

    public static void saveUserCategoriesAndLimits(Long userId, String type,
                                                   Map<String, Double> categories,
                                                   Map<String, Double> limitsGoals) {
        try {
            getConnection().setAutoCommit(false);

            String deleteCategories = "DELETE FROM user_categories WHERE user_id = ? AND type = ?";
            try (PreparedStatement pstmt = getConnection().prepareStatement(deleteCategories)) {
                pstmt.setLong(1, userId);
                pstmt.setString(2, type);
                pstmt.executeUpdate();
            }

            if (categories != null && !categories.isEmpty()) {
                String insertCategory = "INSERT INTO user_categories (user_id, type, category_name, amount) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = getConnection().prepareStatement(insertCategory)) {
                    for (Map.Entry<String, Double> entry : categories.entrySet()) {
                        pstmt.setLong(1, userId);
                        pstmt.setString(2, type);
                        pstmt.setString(3, entry.getKey());
                        pstmt.setDouble(4, entry.getValue());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                String cacheKey = userId + "_" + type;
                categoriesCache.put(cacheKey, new HashMap<>(categories));
            }

            String deleteLimits = "DELETE FROM user_limits WHERE user_id = ? AND type = ?";
            try (PreparedStatement pstmt = getConnection().prepareStatement(deleteLimits)) {
                pstmt.setLong(1, userId);
                pstmt.setString(2, type);
                pstmt.executeUpdate();
            }

            if (limitsGoals != null && !limitsGoals.isEmpty()) {
                String insertLimit = "INSERT INTO user_limits (user_id, type, category_name, limit_amount) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = getConnection().prepareStatement(insertLimit)) {
                    for (Map.Entry<String, Double> entry : limitsGoals.entrySet()) {
                        pstmt.setLong(1, userId);
                        pstmt.setString(2, type);
                        pstmt.setString(3, entry.getKey());
                        pstmt.setDouble(4, entry.getValue());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                String limitsCacheKey = userId + "_" + type + "_limits";
                limitsCache.put(limitsCacheKey, new HashMap<>(limitsGoals));
            }

            getConnection().commit();

        } catch (SQLException e) {
            try {
                getConnection().rollback();
                System.err.println("Ошибка сохранения данных: " + e.getMessage());
            } catch (SQLException ex) {
                System.err.println("Ошибка отката транзакции: " + ex.getMessage());
            }
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Ошибка восстановления autoCommit: " + e.getMessage());
            }
        }
    }

    public static Map<String, Object> getPeriodInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", false);
        result.put("periodType", "month");
        result.put("daysLeft", 0L);

        String query = "SELECT * FROM user_periods WHERE user_id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    result.put("enabled", rs.getBoolean("enabled"));
                    result.put("periodType", rs.getString("period_type"));
                    result.put("periodStartDate", rs.getString("period_start_date"));
                    result.put("nextResetDate", rs.getString("next_reset_date"));
                    result.put("daysLeft", rs.getLong("days_left"));
                    result.put("lastResetDate", rs.getString("last_reset_date"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка получения информации о периоде: " + e.getMessage());
        }

        return result;
    }

    public static void savePeriodInfo(Long userId, Map<String, Object> periodInfo) {
        String query = """
            INSERT OR REPLACE INTO user_periods 
            (user_id, enabled, period_type, period_start_date, next_reset_date, days_left, last_reset_date) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setLong(1, userId);
            pstmt.setBoolean(2, (Boolean) periodInfo.get("enabled"));
            pstmt.setString(3, (String) periodInfo.get("periodType"));
            pstmt.setString(4, (String) periodInfo.get("periodStartDate"));
            pstmt.setString(5, (String) periodInfo.get("nextResetDate"));
            pstmt.setLong(6, ((Number) periodInfo.get("daysLeft")).longValue());
            pstmt.setString(7, (String) periodInfo.get("lastResetDate"));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Ошибка сохранения периода в БД: " + e.getMessage());
        }
    }

    public static void deleteUserData(Long userId) {
        try {
            getConnection().setAutoCommit(false);

            String[] deleteQueries = {
                    "DELETE FROM user_categories WHERE user_id = ?",
                    "DELETE FROM user_limits WHERE user_id = ?",
                    "DELETE FROM user_periods WHERE user_id = ?"
            };

            for (String query : deleteQueries) {
                try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
                    pstmt.setLong(1, userId);
                    pstmt.executeUpdate();
                }
            }

            getConnection().commit();

            categoriesCache.keySet().removeIf(key -> key.startsWith(userId + "_"));
            limitsCache.keySet().removeIf(key -> key.startsWith(userId + "_"));

        } catch (SQLException e) {
            try {
                getConnection().rollback();
                System.err.println("Ошибка удаления данных: " + e.getMessage());
            } catch (SQLException ex) {
                System.err.println("Ошибка отката транзакции: " + ex.getMessage());
            }
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Ошибка восстановления autoCommit: " + e.getMessage());
            }
        }
    }
}
