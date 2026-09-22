package financialmanagement;

import com.formdev.flatlaf.FlatLightLaf;
import financialmanagement.dao.DatabaseHelper;
import financialmanagement.util.AppFont;
import financialmanagement.view.MainFrame;

import javax.swing.*;
import java.awt.Font;

public class Main {
    public static void main(String[] args) {
        // 1. Kích hoạt khử răng cưa chữ Subpixel và scale tỉ lệ UI thoải mái, rõ ràng (1.25x)
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("flatlaf.uiScale", "1.25");

        // 2. Khởi tạo FlatLaf Look and Feel hiện đại với font Roboto 14pt
        try {
            FlatLightLaf.setup();

            // Áp dụng font Roboto 14pt cho toàn bộ thành phần UI
            Font defaultFont = AppFont.plain(14);
            UIManager.put("defaultFont", defaultFont);

            // Cấu hình bo góc tinh tế và kích thước padding thoáng đãng
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("Table.rowHeight", 36);
        } catch (Exception e) {
            System.err.println("Không thể khởi tạo FlatLaf Look and Feel: " + e.getMessage());
        }

        // 3. Khởi tạo Database SQLite & Seeding dữ liệu
        DatabaseHelper.initDatabase();

        // 4. Khởi chạy giao diện chính trên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
