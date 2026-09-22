package financialmanagement.dao;

import financialmanagement.model.Category;
import financialmanagement.model.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, type, color, icon FROM categories ORDER BY type, name";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllCategories: " + e.getMessage());
        }
        return list;
    }

    public List<Category> getCategoriesByType(TransactionType type) {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, type, color, icon FROM categories WHERE type = ? ORDER BY name";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getCategoriesByType: " + e.getMessage());
        }
        return list;
    }

    public Category getCategoryById(int id) {
        String sql = "SELECT id, name, type, color, icon FROM categories WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getCategoryById: " + e.getMessage());
        }
        return null;
    }

    public boolean addCategory(Category category) {
        String sql = "INSERT INTO categories (name, type, color, icon) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getType().name());
            ps.setString(3, category.getColor());
            ps.setString(4, category.getIcon());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi addCategory: " + e.getMessage());
        }
        return false;
    }

    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ?, type = ?, color = ?, icon = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getType().name());
            ps.setString(3, category.getColor());
            ps.setString(4, category.getIcon());
            ps.setInt(5, category.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi updateCategory: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi deleteCategory: " + e.getMessage());
        }
        return false;
    }

    public int countUsage(int categoryId) {
        int count = 0;
        String sqlTx = "SELECT COUNT(*) FROM transactions WHERE category_id = ?";
        String sqlBg = "SELECT COUNT(*) FROM budgets WHERE category_id = ?";
        try (Connection conn = DatabaseHelper.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlTx)) {
                ps.setInt(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) count += rs.getInt(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlBg)) {
                ps.setInt(1, categoryId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) count += rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi countUsage: " + e.getMessage());
        }
        return count;
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
            rs.getInt("id"),
            rs.getString("name"),
            TransactionType.fromString(rs.getString("type")),
            rs.getString("color"),
            rs.getString("icon")
        );
    }
}
