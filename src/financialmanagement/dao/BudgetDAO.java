package financialmanagement.dao;

import financialmanagement.model.Budget;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {

    public List<Budget> getBudgetsForMonth(int month, int year) {
        List<Budget> list = new ArrayList<>();
        String monthStr = String.format("%04d-%02d", year, month);
        String sql = """
            SELECT b.*, c.name AS category_name, c.color AS category_color,
                   COALESCE(
                       (SELECT SUM(t.amount) 
                        FROM transactions t 
                        WHERE t.category_id = b.category_id 
                          AND t.type = 'EXPENSE' 
                          AND strftime('%Y-%m', t.transaction_date) = ?), 
                       0.0
                   ) AS spent_amount
            FROM budgets b
            JOIN categories c ON b.category_id = c.id
            WHERE b.month = ? AND b.year = ?
            ORDER BY b.amount_limit DESC
        """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthStr);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Budget b = new Budget();
                    b.setId(rs.getInt("id"));
                    b.setCategoryId(rs.getInt("category_id"));
                    b.setAmountLimit(rs.getDouble("amount_limit"));
                    b.setMonth(rs.getInt("month"));
                    b.setYear(rs.getInt("year"));
                    b.setCategoryName(rs.getString("category_name"));
                    b.setCategoryColor(rs.getString("category_color"));
                    b.setSpentAmount(rs.getDouble("spent_amount"));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getBudgetsForMonth: " + e.getMessage());
        }
        return list;
    }

    public boolean saveOrUpdateBudget(Budget budget) {
        String sql = """
            INSERT INTO budgets (category_id, amount_limit, month, year)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(category_id, month, year)
            DO UPDATE SET amount_limit = excluded.amount_limit
        """;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, budget.getCategoryId());
            ps.setDouble(2, budget.getAmountLimit());
            ps.setInt(3, budget.getMonth());
            ps.setInt(4, budget.getYear());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        budget.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi saveOrUpdateBudget: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteBudget(int id) {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi deleteBudget: " + e.getMessage());
        }
        return false;
    }
}
