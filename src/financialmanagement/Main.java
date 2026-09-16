package financialmanagement;

import com.formdev.flatlaf.FlatLightLaf;
import financialmanagement.dao.DatabaseHelper;
import financialmanagement.view.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. Khởi tạo FlatLaf Look and Feel hiện đại
        try {
            FlatLightLaf.setup();
            // Cấu hình giao diện đẹp mắt hơn
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.arc", 12);
        } catch (Exception e) {
            System.err.println("Không thể khởi tạo FlatLaf Look and Feel: " + e.getMessage());
        }

        // 2. Khởi tạo Database SQLite & Seeding dữ liệu
        DatabaseHelper.initDatabase();

        // 3. Khởi chạy giao diện chính trên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
