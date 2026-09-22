package financialmanagement.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import financialmanagement.util.AppFont;
import financialmanagement.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private JPanel contentCards;
    private CardLayout cardLayout;
    private boolean isDarkMode = false;
    private final List<NavItem> navItems = new ArrayList<>();

    private DashboardPanel dashboardPanel;
    private TransactionPanel transactionPanel;
    private WalletPanel walletPanel;
    private CategoryPanel categoryPanel;
    private BudgetPanel budgetPanel;
    private ReportPanel reportPanel;

    private static class NavItem {
        final JButton button;
        final JLabel iconLabel;
        final JLabel textLabel;
        final String cardName;

        NavItem(JButton button, JLabel iconLabel, JLabel textLabel, String cardName) {
            this.button = button;
            this.iconLabel = iconLabel;
            this.textLabel = textLabel;
            this.cardName = cardName;
        }
    }

    public MainFrame() {
        setTitle("FinTrack - Quản Lý Tài Chính Cá Nhân");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Kích thước rộng rãi, thoáng mắt
        setSize(1140, 710);
        setMinimumSize(new Dimension(940, 580));
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
            if (categoryPanel != null) categoryPanel.refreshData();
            if (budgetPanel != null) budgetPanel.refreshData();
            if (reportPanel != null) reportPanel.refreshData();
        };

        dashboardPanel = new DashboardPanel(this);
        transactionPanel = new TransactionPanel(this, syncAll);
        walletPanel = new WalletPanel(this, syncAll);
        categoryPanel = new CategoryPanel(this, syncAll);
        budgetPanel = new BudgetPanel(this, syncAll);
        reportPanel = new ReportPanel(this);

        contentCards.add(dashboardPanel, "DASHBOARD");
        contentCards.add(transactionPanel, "TRANSACTIONS");
        contentCards.add(walletPanel, "WALLETS");
        contentCards.add(categoryPanel, "CATEGORIES");
        contentCards.add(budgetPanel, "BUDGETS");
        contentCards.add(reportPanel, "REPORTS");

        add(contentCards, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

        // Brand header
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 18));
        brandPanel.setOpaque(false);

        JLabel lblLogo = IconHelper.createIcon(IconHelper.ATTACH_MONEY, 28, new Color(33, 150, 243));
        JLabel lblBrand = new JLabel("FinTrack");
        lblBrand.setFont(AppFont.bold(21));
        lblBrand.setForeground(new Color(33, 150, 243));

        brandPanel.add(lblLogo);
        brandPanel.add(lblBrand);

        // Navigation menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        menuPanel.add(createNavButton(IconHelper.DASHBOARD, "Tổng quan", "DASHBOARD", true));
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(createNavButton(IconHelper.TRANSACTIONS, "Sổ giao dịch", "TRANSACTIONS", false));
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(createNavButton(IconHelper.WALLETS, "Ví tài khoản", "WALLETS", false));
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(createNavButton(IconHelper.CATEGORIES, "Danh mục", "CATEGORIES", false));
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(createNavButton(IconHelper.BUDGETS, "Ngân sách", "BUDGETS", false));
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(createNavButton(IconHelper.REPORTS, "Báo cáo", "REPORTS", false));

        // Footer / Theme toggle
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(12, 12, 16, 12));

        JButton btnThemeToggle = new JButton();
        btnThemeToggle.setLayout(new BorderLayout(10, 0));
        btnThemeToggle.setBorder(new EmptyBorder(10, 14, 10, 14));
        btnThemeToggle.setFocusPainted(false);
        btnThemeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThemeToggle.putClientProperty("JButton.buttonType", "roundRect");

        JLabel themeIcon = IconHelper.createIcon(IconHelper.DARK_MODE, 20);
        JLabel themeText = new JLabel("Giao diện tối");
        themeText.setFont(AppFont.plain(13));

        btnThemeToggle.add(themeIcon, BorderLayout.WEST);
        btnThemeToggle.add(themeText, BorderLayout.CENTER);

        btnThemeToggle.addActionListener(e -> toggleTheme(themeIcon, themeText));
        footerPanel.add(btnThemeToggle, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(menuPanel, BorderLayout.CENTER);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String iconGlyph, String text, String cardName, boolean isActive) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout(12, 0));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(210, 44));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        Color activeColor = new Color(33, 150, 243);
        Color normalColor = UIManager.getColor("Label.foreground");

        JLabel iconLabel = IconHelper.createIcon(iconGlyph, 21, isActive ? activeColor : normalColor);
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(isActive ? AppFont.bold(14) : AppFont.plain(14));
        textLabel.setForeground(isActive ? activeColor : normalColor);

        if (isActive) {
            btn.setBackground(new Color(33, 150, 243, 28));
        } else {
            btn.setBackground(new Color(0, 0, 0, 0));
        }

        btn.add(iconLabel, BorderLayout.WEST);
        btn.add(textLabel, BorderLayout.CENTER);

        NavItem item = new NavItem(btn, iconLabel, textLabel, cardName);
        navItems.add(item);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (contentCards != null && !cardName.equals(getActiveCardName())) {
                    btn.setBackground(isDarkMode ? new Color(255, 255, 255, 14) : new Color(0, 0, 0, 10));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (contentCards != null && !cardName.equals(getActiveCardName())) {
                    btn.setBackground(new Color(0, 0, 0, 0));
                }
            }
        });

        btn.addActionListener(e -> {
            cardLayout.show(contentCards, cardName);
            if ("DASHBOARD".equals(cardName)) {
                dashboardPanel.refreshData();
            } else if ("TRANSACTIONS".equals(cardName)) {
                transactionPanel.applyFilter();
            } else if ("WALLETS".equals(cardName)) {
                walletPanel.refreshData();
            } else if ("CATEGORIES".equals(cardName)) {
                categoryPanel.refreshData();
            } else if ("BUDGETS".equals(cardName)) {
                budgetPanel.refreshData();
            } else if ("REPORTS".equals(cardName)) {
                reportPanel.refreshData();
            }
            updateActiveNav(item);
        });

        return btn;
    }

    private String activeCard = "DASHBOARD";
    private String getActiveCardName() {
        return activeCard;
    }

    private void updateActiveNav(NavItem activeItem) {
        activeCard = activeItem.cardName;
        Color activeColor = new Color(33, 150, 243);
        Color normalColor = UIManager.getColor("Label.foreground");

        for (NavItem item : navItems) {
            if (item == activeItem) {
                item.button.setBackground(new Color(33, 150, 243, 28));
                item.iconLabel.setForeground(activeColor);
                item.textLabel.setForeground(activeColor);
                item.textLabel.setFont(AppFont.bold(14));
            } else {
                item.button.setBackground(new Color(0, 0, 0, 0));
                item.iconLabel.setForeground(normalColor);
                item.textLabel.setForeground(normalColor);
                item.textLabel.setFont(AppFont.plain(14));
            }
        }
    }

    private void toggleTheme(JLabel themeIcon, JLabel themeText) {
        isDarkMode = !isDarkMode;
        try {
            if (isDarkMode) {
                FlatDarkLaf.setup();
                themeIcon.setText(IconHelper.LIGHT_MODE);
                themeText.setText("Giao diện sáng");
            } else {
                FlatLightLaf.setup();
                themeIcon.setText(IconHelper.DARK_MODE);
                themeText.setText("Giao diện tối");
            }

            // Áp dụng lại font Roboto 14pt và UI Defaults
            UIManager.put("defaultFont", AppFont.plain(14));
            FlatLaf.updateUI();

            // Cập nhật lại màu sắc nav buttons
            for (NavItem item : navItems) {
                if (item.cardName.equals(activeCard)) {
                    item.iconLabel.setForeground(new Color(33, 150, 243));
                    item.textLabel.setForeground(new Color(33, 150, 243));
                } else {
                    item.iconLabel.setForeground(UIManager.getColor("Label.foreground"));
                    item.textLabel.setForeground(UIManager.getColor("Label.foreground"));
                }
            }

            if (dashboardPanel != null) dashboardPanel.refreshData();
            if (transactionPanel != null) transactionPanel.applyFilter();
            if (walletPanel != null) walletPanel.refreshData();
            if (categoryPanel != null) categoryPanel.refreshData();
            if (budgetPanel != null) budgetPanel.refreshData();
            if (reportPanel != null) reportPanel.refreshData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
