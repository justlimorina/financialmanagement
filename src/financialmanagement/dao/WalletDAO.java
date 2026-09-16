package financialmanagement.dao;

import financialmanagement.model.Wallet;
import financialmanagement.model.WalletType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WalletDAO {

    public List<Wallet> getAllWallets() {
        List<Wallet> list = new ArrayList<>();
        String sql = "SELECT id, name, balance, type, created_at FROM wallets ORDER BY id ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToWallet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllWallets: " + e.getMessage());
        }
        return list;
    }

    public Wallet getWalletById(int id) {
        String sql = "SELECT id, name, balance, type, created_at FROM wallets WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWallet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getWalletById: " + e.getMessage());
        }
        return null;
    }

    public boolean addWallet(Wallet wallet) {
        String sql = "INSERT INTO wallets (name, balance, type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, wallet.getName());
            ps.setDouble(2, wallet.getBalance());
            ps.setString(3, wallet.getType().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        wallet.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi addWallet: " + e.getMessage());
        }
        return false;
    }

    public boolean updateWallet(Wallet wallet) {
        String sql = "UPDATE wallets SET name = ?, balance = ?, type = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wallet.getName());
            ps.setDouble(2, wallet.getBalance());
            ps.setString(3, wallet.getType().name());
            ps.setInt(4, wallet.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi updateWallet: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteWallet(int id) {
        String sql = "DELETE FROM wallets WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi deleteWallet: " + e.getMessage());
        }
        return false;
    }

    public double getTotalBalance() {
        String sql = "SELECT SUM(balance) FROM wallets";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTotalBalance: " + e.getMessage());
        }
        return 0.0;
    }

    private Wallet mapResultSetToWallet(ResultSet rs) throws SQLException {
        return new Wallet(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("balance"),
            WalletType.fromString(rs.getString("type")),
            rs.getString("created_at")
        );
    }
}
