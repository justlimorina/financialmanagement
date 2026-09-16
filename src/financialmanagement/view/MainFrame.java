package financialmanagement.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private JPanel contentCards;
    private CardLayout cardLayout;
    private boolean isDarkMode = false;
    private final List<JButton> navButtons = new ArrayList<>();

    private DashboardPanel dashboardPanel;
    private TransactionPanel transactionPanel;
    private WalletPanel walletPanel;
    private BudgetPanel budgetPanel;
    private ReportPanel reportPanel;

    public MainFrame() {
        setTitle("FinTrack - Quản Lý Tài Chính Cá Nhân");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 750);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Sidebar Panel (West)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Main Content Panel with CardLayout (Center)
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);

        Runnable syncAll = () -> {
            if (dashboardPanel != null) dashboardPanel.refreshData();
            if (transactionPanel != null) transactionPanel.applyFilter();
            if (walletPanel != null) walletPanel.refreshData();
            if (budgetPanel != null) budgetPanel.refreshData();
            if (reportPanel != null) reportPanel.refreshData();
        };

        dashboardPanel = new DashboardPanel(this);
        transactionPanel = new TransactionPanel(this, syncAll);
        walletPanel = new WalletPanel(this, syncAll);
        budgetPanel = new BudgetPanel(this, syncAll);
        reportPanel = new ReportPanel(this);

        contentCards.add(dashboardPanel, "DASHBOARD");
        contentCards.add(transactionPanel, "TRANSACTIONS");
        contentCards.add(walletPanel, "WALLETS");
        contentCards.add(budgetPanel, "BUDGETS");
        contentCards.add(reportPanel, "REPORTS");

        add(contentCards, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

        // Brand header
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 18));
        brandPanel.setOpaque(false);
        JLabel lblLogo = new JLabel("💰");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel lblBrand = new JLabel("FinTrack");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBrand.setForeground(new Color(33, 150, 243));

        brandPanel.add(lblLogo);
        brandPanel.add(lblBrand);

        // Navigation menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnDash = createNavButton("📊  Tổng quan", "DASHBOARD", true);
        JButton btnTrans = createNavButton("💸  Sổ giao dịch", "TRANSACTIONS", false);
        JButton btnWallets = createNavButton("💳  Ví tài khoản", "WALLETS", false);
        JButton btnBudgets = createNavButton("🎯  Ngân sách", "BUDGETS", false);
        JButton btnReports = createNavButton("📈  Báo cáo", "REPORTS", false);

        menuPanel.add(btnDash);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(btnTrans);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(btnWallets);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(btnBudgets);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(btnReports);

        // Footer / Theme toggle
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 12, 15, 12));

        JButton btnThemeToggle = new JButton("🌙  Giao diện tối");
        btnThemeToggle.setFocusPainted(false);
        btnThemeToggle.putClientProperty("JButton.buttonType", "roundRect");
        btnThemeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThemeToggle.addActionListener(e -> toggleTheme(btnThemeToggle));

        footerPanel.add(btnThemeToggle, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(menuPanel, BorderLayout.CENTER);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");

        if (isActive) {
            btn.setBackground(new Color(33, 150, 243, 30));
            btn.setForeground(new Color(33, 150, 243));
        }

        btn.addActionListener(e -> {
            cardLayout.show(contentCards, cardName);
            if ("DASHBOARD".equals(cardName)) {
                dashboardPanel.refreshData();
            } else if ("TRANSACTIONS".equals(cardName)) {
                transactionPanel.applyFilter();
            } else if ("WALLETS".equals(cardName)) {
                walletPanel.refreshData();
            } else if ("BUDGETS".equals(cardName)) {
                budgetPanel.refreshData();
            } else if ("REPORTS".equals(cardName)) {
                reportPanel.refreshData();
            }
            updateActiveNav(btn);
        });

        navButtons.add(btn);
        return btn;
    }

    private void updateActiveNav(JButton activeBtn) {
        for (JButton b : navButtons) {
            if (b == activeBtn) {
                b.setFont(new Font("Segoe UI", Font.BOLD, 14));
                b.setBackground(new Color(33, 150, 243, 30));
                b.setForeground(new Color(33, 150, 243));
            } else {
                b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                b.setBackground(null);
                b.setForeground(UIManager.getColor("Button.foreground"));
            }
        }
    }

    private void toggleTheme(JButton btnThemeToggle) {
        isDarkMode = !isDarkMode;
        try {
            if (isDarkMode) {
                FlatDarkLaf.setup();
                btnThemeToggle.setText("☀️  Giao diện sáng");
            } else {
                FlatLightLaf.setup();
                btnThemeToggle.setText("🌙  Giao diện tối");
            }
            FlatLaf.updateUI();
            dashboardPanel.refreshData();
            if (transactionPanel != null) {
                transactionPanel.applyFilter();
            }
            if (walletPanel != null) {
                walletPanel.refreshData();
            }
            if (budgetPanel != null) {
                budgetPanel.refreshData();
            }
            if (reportPanel != null) {
                reportPanel.refreshData();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel("Chức năng " + title + " đang được hoàn thiện ở các bước tiếp theo...");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        lbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        p.add(lbl);
        return p;
    }
}
