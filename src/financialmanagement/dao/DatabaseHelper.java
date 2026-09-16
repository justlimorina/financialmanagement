package financialmanagement.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:finance.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy SQLite JDBC Driver: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // 1. Tạo bảng Wallets
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS wallets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    balance REAL NOT NULL DEFAULT 0.0,
                    type TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 2. Tạo bảng Categories
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    color TEXT DEFAULT '#2196F3',
                    icon TEXT DEFAULT 'tag'
                );
            """);

            // 3. Tạo bảng Transactions
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    wallet_id INTEGER NOT NULL,
                    category_id INTEGER,
                    amount REAL NOT NULL,
                    type TEXT NOT NULL,
                    to_wallet_id INTEGER,
                    transaction_date TEXT NOT NULL,
                    note TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE,
                    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
                    FOREIGN KEY (to_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL
                );
            """);

            // 4. Tạo bảng Budgets
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category_id INTEGER NOT NULL,
                    amount_limit REAL NOT NULL,
                    month INTEGER NOT NULL,
                    year INTEGER NOT NULL,
                    UNIQUE(category_id, month, year),
                    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
                );
            """);

            // Seed dữ liệu mặc định
            seedDefaultData(conn);

        } catch (SQLException e) {
            System.err.println("Lỗi khởi tạo CSDL: " + e.getMessage());
        }
    }

    private static void seedDefaultData(Connection conn) throws SQLException {
        // Kiểm tra danh mục
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO categories (name, type, color, icon) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    // Danh mục Chi tiêu (EXPENSE)
                    Object[][] expenseCats = {
                        {"Ăn uống", "EXPENSE", "#FF5722", "food"},
                        {"Tiền nhà & Điện nước", "EXPENSE", "#3F51B5", "home"},
                        {"Đi lại & Xăng xe", "EXPENSE", "#009688", "car"},
                        {"Mua sắm", "EXPENSE", "#E91E63", "shopping"},
                        {"Giải trí", "EXPENSE", "#9C27B0", "entertainment"},
                        {"Y tế & Sức khỏe", "EXPENSE", "#00BCD4", "medical"},
                        {"Giáo dục", "EXPENSE", "#FF9800", "education"},
                        {"Chi phí khác", "EXPENSE", "#607D8B", "other"}
                    };
                    for (Object[] cat : expenseCats) {
                        ps.setString(1, (String) cat[0]);
                        ps.setString(2, (String) cat[1]);
                        ps.setString(3, (String) cat[2]);
                        ps.setString(4, (String) cat[3]);
                        ps.addBatch();
                    }

                    // Danh mục Thu nhập (INCOME)
                    Object[][] incomeCats = {
                        {"Tiền lương", "INCOME", "#4CAF50", "salary"},
                        {"Tiền thưởng", "INCOME", "#8BC34A", "bonus"},
                        {"Lợi nhuận đầu tư", "INCOME", "#2196F3", "investment"},
                        {"Thu nhập phụ", "INCOME", "#00E676", "side_income"},
                        {"Thu nhập khác", "INCOME", "#8D6E63", "other"}
                    };
                    for (Object[] cat : incomeCats) {
                        ps.setString(1, (String) cat[0]);
                        ps.setString(2, (String) cat[1]);
                        ps.setString(3, (String) cat[2]);
                        ps.setString(4, (String) cat[3]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }

        // Kiểm tra ví mặc định
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM wallets")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO wallets (name, balance, type) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, "Tiền mặt");
                    ps.setDouble(2, 0.0);
                    ps.setString(3, "CASH");
                    ps.addBatch();

                    ps.setString(1, "Tài khoản ngân hàng");
                    ps.setDouble(2, 0.0);
                    ps.setString(3, "BANK");
                    ps.addBatch();

                    ps.executeBatch();
                }
            }
        }
    }
}
