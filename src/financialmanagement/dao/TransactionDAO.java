package financialmanagement.dao;

import financialmanagement.model.Transaction;
import financialmanagement.model.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {

    public boolean addTransaction(Transaction tx) {
        String insertSql = """
            INSERT INTO transactions (wallet_id, category_id, amount, type, to_wallet_id, transaction_date, note)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        String updateBalanceSql = "UPDATE wallets SET balance = balance + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // Bắt đầu SQL Transaction

            // 1. Cập nhật số dư ví
            if (tx.getType() == TransactionType.EXPENSE) {
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, -tx.getAmount());
                    ps.setInt(2, tx.getWalletId());
                    ps.executeUpdate();
                }
            } else if (tx.getType() == TransactionType.INCOME) {
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, tx.getAmount());
                    ps.setInt(2, tx.getWalletId());
                    ps.executeUpdate();
                }
            } else if (tx.getType() == TransactionType.TRANSFER) {
                if (tx.getToWalletId() == null || tx.getToWalletId() == tx.getWalletId()) {
                    conn.rollback();
                    return false;
                }
                // Trừ tiền ở ví nguồn
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, -tx.getAmount());
                    ps.setInt(2, tx.getWalletId());
                    ps.executeUpdate();
                }
                // Cộng tiền ở ví đích
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, tx.getAmount());
                    ps.setInt(2, tx.getToWalletId());
                    ps.executeUpdate();
                }
            }

            // 2. Chèn bản ghi giao dịch
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, tx.getWalletId());
                if (tx.getCategoryId() != null) {
                    ps.setInt(2, tx.getCategoryId());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setDouble(3, tx.getAmount());
                ps.setString(4, tx.getType().name());
                if (tx.getToWalletId() != null) {
                    ps.setInt(5, tx.getToWalletId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ps.setString(6, tx.getTransactionDate());
                ps.setString(7, tx.getNote());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            tx.setId(rs.getInt(1));
                        }
                    }
                }
            }

            conn.commit(); // Thành công -> Commit
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi addTransaction: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Lỗi rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Lỗi đóng connection: " + e.getMessage());
                }
            }
        }
    }

    public boolean deleteTransaction(int id) {
        String selectSql = "SELECT wallet_id, category_id, amount, type, to_wallet_id FROM transactions WHERE id = ?";
        String deleteSql = "DELETE FROM transactions WHERE id = ?";
        String updateBalanceSql = "UPDATE wallets SET balance = balance + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            // 1. Tìm thông tin giao dịch cũ để hoàn lại số dư
            int walletId = 0;
            Integer toWalletId = null;
            double amount = 0;
            String typeStr = null;

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        walletId = rs.getInt("wallet_id");
                        amount = rs.getDouble("amount");
                        typeStr = rs.getString("type");
                        int toId = rs.getInt("to_wallet_id");
                        if (!rs.wasNull()) {
                            toWalletId = toId;
                        }
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            TransactionType type = TransactionType.fromString(typeStr);

            // 2. Hoàn lại số dư
            if (type == TransactionType.EXPENSE) {
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, amount); // Cộng lại tiền đã chi
                    ps.setInt(2, walletId);
                    ps.executeUpdate();
                }
            } else if (type == TransactionType.INCOME) {
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, -amount); // Trừ lại tiền đã thu
                    ps.setInt(2, walletId);
                    ps.executeUpdate();
                }
            } else if (type == TransactionType.TRANSFER && toWalletId != null) {
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, amount); // Trả lại tiền cho ví nguồn
                    ps.setInt(2, walletId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, -amount); // Thu hồi tiền từ ví đích
                    ps.setInt(2, toWalletId);
                    ps.executeUpdate();
                }
            }

            // 3. Xóa giao dịch
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi deleteTransaction: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Lỗi rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Lỗi đóng connection: " + e.getMessage());
                }
            }
        }
    }

    public Transaction getTransactionById(int id) {
        String sql = """
            SELECT t.*, 
                   w.name AS wallet_name, 
                   c.name AS category_name, c.color AS category_color,
                   tw.name AS to_wallet_name
            FROM transactions t
            LEFT JOIN wallets w ON t.wallet_id = w.id
            LEFT JOIN categories c ON t.category_id = c.id
            LEFT JOIN wallets tw ON t.to_wallet_id = tw.id
            WHERE t.id = ?
        """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTransactionById: " + e.getMessage());
        }
        return null;
    }

    public boolean updateTransaction(Transaction newTx) {
        String selectOldSql = "SELECT wallet_id, category_id, amount, type, to_wallet_id FROM transactions WHERE id = ?";
        String updateBalanceSql = "UPDATE wallets SET balance = balance + ? WHERE id = ?";
        String updateTxSql = """
            UPDATE transactions 
            SET wallet_id = ?, category_id = ?, amount = ?, type = ?, to_wallet_id = ?, transaction_date = ?, note = ?
            WHERE id = ?
        """;

        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            // 1. Lấy thông tin giao dịch cũ để hoàn tác (revert) số dư
            int oldWalletId = 0;
            Integer oldToWalletId = null;
            double oldAmount = 0;
            String oldTypeStr = null;

            try (PreparedStatement ps = conn.prepareStatement(selectOldSql)) {
                ps.setInt(1, newTx.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        oldWalletId = rs.getInt("wallet_id");
                        oldAmount = rs.getDouble("amount");
                        oldTypeStr = rs.getString("type");
                        int toId = rs.getInt("to_wallet_id");
                        if (!rs.wasNull()) {
                            oldToWalletId = toId;
                        }
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            TransactionType oldType = TransactionType.fromString(oldTypeStr);

            // 2. Hoàn lại số dư cũ
            if (oldType == TransactionType.EXPENSE) {
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, oldAmount);
                    ps.setInt(2, oldWalletId);
                    ps.executeUpdate();
                }
            } else if (oldType == TransactionType.INCOME) {
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, -oldAmount);
                    ps.setInt(2, oldWalletId);
                    ps.executeUpdate();
                }
            } else if (oldType == TransactionType.TRANSFER && oldToWalletId != null) {
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, oldAmount);
                    ps.setInt(2, oldWalletId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, -oldAmount);
                    ps.setInt(2, oldToWalletId);
                    ps.executeUpdate();
                }
            }

            // 3. Áp dụng tác động số dư của giao dịch mới
            if (newTx.getType() == TransactionType.EXPENSE) {
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, -newTx.getAmount());
                    ps.setInt(2, newTx.getWalletId());
                    ps.executeUpdate();
                }
            } else if (newTx.getType() == TransactionType.INCOME) {
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, newTx.getAmount());
                    ps.setInt(2, newTx.getWalletId());
                    ps.executeUpdate();
                }
            } else if (newTx.getType() == TransactionType.TRANSFER) {
                if (newTx.getToWalletId() == null || newTx.getToWalletId().equals(newTx.getWalletId())) {
                    conn.rollback();
                    return false;
                }
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, -newTx.getAmount());
                    ps.setInt(2, newTx.getWalletId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, newTx.getAmount());
                    ps.setInt(2, newTx.getToWalletId());
                    ps.executeUpdate();
                }
            }

            // 4. Cập nhật bản ghi transactions
            try (PreparedStatement ps = conn.prepareStatement(updateTxSql)) {
                ps.setInt(1, newTx.getWalletId());
                if (newTx.getCategoryId() != null) {
                    ps.setInt(2, newTx.getCategoryId());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setDouble(3, newTx.getAmount());
                ps.setString(4, newTx.getType().name());
                if (newTx.getToWalletId() != null) {
                    ps.setInt(5, newTx.getToWalletId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ps.setString(6, newTx.getTransactionDate());
                ps.setString(7, newTx.getNote());
                ps.setInt(8, newTx.getId());

                int affected = ps.executeUpdate();
                if (affected <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi updateTransaction: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Lỗi rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Lỗi đóng connection: " + e.getMessage());
                }
            }
        }
    }

    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT t.*, 
                   w.name AS wallet_name, 
                   c.name AS category_name, c.color AS category_color,
                   tw.name AS to_wallet_name
            FROM transactions t
            LEFT JOIN wallets w ON t.wallet_id = w.id
            LEFT JOIN categories c ON t.category_id = c.id
            LEFT JOIN wallets tw ON t.to_wallet_id = tw.id
            ORDER BY t.transaction_date DESC, t.id DESC
            LIMIT ?
        """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getRecentTransactions: " + e.getMessage());
        }
        return list;
    }

    public List<Transaction> getTransactionsByFilter(String startDate, String endDate, 
                                                     Integer walletId, Integer categoryId, 
                                                     TransactionType type) {
        List<Transaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT t.*, 
                   w.name AS wallet_name, 
                   c.name AS category_name, c.color AS category_color,
                   tw.name AS to_wallet_name
            FROM transactions t
            LEFT JOIN wallets w ON t.wallet_id = w.id
            LEFT JOIN categories c ON t.category_id = c.id
            LEFT JOIN wallets tw ON t.to_wallet_id = tw.id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND t.transaction_date >= ?");
            params.add(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND t.transaction_date <= ?");
            params.add(endDate);
        }
        if (walletId != null && walletId > 0) {
            sql.append(" AND (t.wallet_id = ? OR t.to_wallet_id = ?)");
            params.add(walletId);
            params.add(walletId);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND t.category_id = ?");
            params.add(categoryId);
        }
        if (type != null) {
            sql.append(" AND t.type = ?");
            params.add(type.name());
        }

        sql.append(" ORDER BY t.transaction_date DESC, t.id DESC");

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTransactionsByFilter: " + e.getMessage());
        }
        return list;
    }

    public Map<String, Double> getMonthlyExpenseByCategory(int month, int year) {
        Map<String, Double> result = new LinkedHashMap<>();
        String monthStr = String.format("%04d-%02d", year, month);
        String sql = """
            SELECT c.name, SUM(t.amount) AS total
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.type = 'EXPENSE' AND strftime('%Y-%m', t.transaction_date) = ?
            GROUP BY c.id, c.name
            ORDER BY total DESC
        """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("name"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getMonthlyExpenseByCategory: " + e.getMessage());
        }
        return result;
    }

    public double[] getMonthlySummary(int month, int year) {
        // [0]: Total Income, [1]: Total Expense
        double[] summary = new double[]{0.0, 0.0};
        String monthStr = String.format("%04d-%02d", year, month);
        String sql = """
            SELECT type, SUM(amount) AS total
            FROM transactions
            WHERE strftime('%Y-%m', transaction_date) = ? AND type IN ('INCOME', 'EXPENSE')
            GROUP BY type
        """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    if ("INCOME".equalsIgnoreCase(type)) {
                        summary[0] = rs.getDouble("total");
                    } else if ("EXPENSE".equalsIgnoreCase(type)) {
                        summary[1] = rs.getDouble("total");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getMonthlySummary: " + e.getMessage());
        }
        return summary;
    }

    public Map<Integer, double[]> getYearlyMonthlySummary(int year) {
        // Map: Month (1-12) -> [Total Income, Total Expense]
        Map<Integer, double[]> map = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            map.put(m, new double[]{0.0, 0.0});
        }

        String yearStr = String.valueOf(year);
        String sql = """
            SELECT CAST(strftime('%m', transaction_date) AS INTEGER) AS month, 
                   type, 
                   SUM(amount) AS total
            FROM transactions
            WHERE strftime('%Y', transaction_date) = ? AND type IN ('INCOME', 'EXPENSE')
            GROUP BY month, type
        """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yearStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int m = rs.getInt("month");
                    String type = rs.getString("type");
                    double total = rs.getDouble("total");
                    double[] arr = map.get(m);
                    if (arr != null) {
                        if ("INCOME".equalsIgnoreCase(type)) {
                            arr[0] = total;
                        } else if ("EXPENSE".equalsIgnoreCase(type)) {
                            arr[1] = total;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getYearlyMonthlySummary: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Double> getYearlyExpenseByCategory(int year) {
        Map<String, Double> result = new LinkedHashMap<>();
        String yearStr = String.valueOf(year);
        String sql = """
            SELECT c.name, SUM(t.amount) AS total
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.type = 'EXPENSE' AND strftime('%Y', t.transaction_date) = ?
            GROUP BY c.id, c.name
            ORDER BY total DESC
        """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yearStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("name"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getYearlyExpenseByCategory: " + e.getMessage());
        }
        return result;
    }


    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setId(rs.getInt("id"));
        tx.setWalletId(rs.getInt("wallet_id"));
        int catId = rs.getInt("category_id");
        tx.setCategoryId(rs.wasNull() ? null : catId);
        tx.setAmount(rs.getDouble("amount"));
        tx.setType(TransactionType.fromString(rs.getString("type")));
        int toId = rs.getInt("to_wallet_id");
        tx.setToWalletId(rs.wasNull() ? null : toId);
        tx.setTransactionDate(rs.getString("transaction_date"));
        tx.setNote(rs.getString("note"));
        tx.setCreatedAt(rs.getString("created_at"));

        tx.setWalletName(rs.getString("wallet_name"));
        tx.setCategoryName(rs.getString("category_name"));
        tx.setCategoryColor(rs.getString("category_color"));
        tx.setToWalletName(rs.getString("to_wallet_name"));
        return tx;
    }
}
