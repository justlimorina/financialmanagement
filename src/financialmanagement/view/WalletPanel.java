package financialmanagement.view;

import financialmanagement.dao.WalletDAO;
import financialmanagement.model.Wallet;
import financialmanagement.model.WalletType;
import financialmanagement.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class WalletPanel extends JPanel {
    private final WalletDAO walletDAO = new WalletDAO();
    private final Frame parentFrame;
    private final Runnable onDataChangedCallback;

    private JLabel lblTotalBalance;
    private JLabel lblWalletCount;
    private JPanel gridContainer;

    public WalletPanel(Frame parentFrame, Runnable onDataChangedCallback) {
        this.parentFrame = parentFrame;
        this.onDataChangedCallback = onDataChangedCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // 1. Header (Title + Actions)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel lblTitle = new JLabel("Ví & Tài Khoản Tiền");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblSub = new JLabel("Quản lý dòng tiền trên từng tài khoản ngân hàng, ví điện tử, tiền mặt");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtons.setOpaque(false);

        JButton btnTransfer = new JButton("💸 Chuyển Tiền");
        btnTransfer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTransfer.putClientProperty("JButton.buttonType", "roundRect");
        btnTransfer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTransfer.addActionListener(e -> {
            TransactionDialog dialog = new TransactionDialog(parentFrame, () -> {
                refreshData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            });
            dialog.setVisible(true);
        });

        JButton btnAddWallet = new JButton("+ Thêm Ví Mới");
        btnAddWallet.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddWallet.putClientProperty("JButton.buttonType", "roundRect");
        btnAddWallet.setBackground(new Color(33, 150, 243));
        btnAddWallet.setForeground(Color.WHITE);
        btnAddWallet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddWallet.addActionListener(e -> {
            WalletDialog dialog = new WalletDialog(parentFrame, null, () -> {
                refreshData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            });
            dialog.setVisible(true);
        });

        actionButtons.add(btnTransfer);
        actionButtons.add(btnAddWallet);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(actionButtons, BorderLayout.EAST);

        // 2. Summary Bar
        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        summaryBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                new EmptyBorder(8, 15, 8, 15)
        ));

        lblTotalBalance = new JLabel("Tổng tài sản: 0 ₫");
        lblTotalBalance.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalBalance.setForeground(new Color(33, 150, 243));

        lblWalletCount = new JLabel("Số lượng ví: 0");
        lblWalletCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWalletCount.setForeground(UIManager.getColor("Label.disabledForeground"));

        summaryBar.add(lblTotalBalance);
        summaryBar.add(new JSeparator(SwingConstants.VERTICAL));
        summaryBar.add(lblWalletCount);

        JPanel northGroup = new JPanel(new BorderLayout(0, 15));
        northGroup.setOpaque(false);
        northGroup.add(headerPanel, BorderLayout.NORTH);
        northGroup.add(summaryBar, BorderLayout.SOUTH);
        add(northGroup, BorderLayout.NORTH);

        // 3. Grid Container of Wallet Cards
        gridContainer = new JPanel();
        gridContainer.setLayout(new GridLayout(0, 3, 15, 15)); // 3 columns, dynamic rows
        gridContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        gridContainer.removeAll();

        List<Wallet> wallets = walletDAO.getAllWallets();
        double total = walletDAO.getTotalBalance();

        lblTotalBalance.setText("Tổng tài sản: " + CurrencyFormatter.formatVND(total));
        lblWalletCount.setText("Số lượng ví: " + wallets.size() + " ví");

        for (Wallet w : wallets) {
            gridContainer.add(createWalletCard(w));
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    private JPanel createWalletCard(Wallet wallet) {
        JPanel card = new JPanel(new BorderLayout(10, 12));
        Color accentColor;
        String iconEmoji;

        if (wallet.getType() == WalletType.CASH) {
            accentColor = new Color(76, 175, 80); // Xanh lá
            iconEmoji = "💵";
        } else if (wallet.getType() == WalletType.BANK) {
            accentColor = new Color(33, 150, 243); // Xanh dương
            iconEmoji = "🏦";
        } else if (wallet.getType() == WalletType.E_WALLET) {
            accentColor = new Color(233, 30, 99); // Hồng / Momo
            iconEmoji = "📱";
        } else {
            accentColor = new Color(156, 39, 176); // Tím
            iconEmoji = "💳";
        }

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 70), 1, true),
                new EmptyBorder(16, 18, 14, 18)
        ));

        // Header: Emoji + Type Name
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblIcon = new JLabel(iconEmoji + "  " + wallet.getType().getDisplayName().toUpperCase());
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblIcon.setForeground(accentColor);
        top.add(lblIcon, BorderLayout.WEST);

        // Center: Wallet Name + Big Balance
        JPanel center = new JPanel(new GridLayout(2, 1, 0, 4));
        center.setOpaque(false);

        JLabel lblName = new JLabel(wallet.getName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblBalance = new JLabel(CurrencyFormatter.formatVND(wallet.getBalance()));
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBalance.setForeground(wallet.getBalance() >= 0 ? UIManager.getColor("Label.foreground") : new Color(229, 57, 53));

        center.add(lblName);
        center.add(lblBalance);

        // Footer: Actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bottom.setOpaque(false);

        JButton btnEdit = new JButton("✏️ Sửa");
        btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnEdit.addActionListener(e -> {
            WalletDialog dialog = new WalletDialog(parentFrame, wallet, () -> {
                refreshData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            });
            dialog.setVisible(true);
        });

        JButton btnDelete = new JButton("🗑️");
        btnDelete.setToolTipText("Xóa ví");
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDelete.putClientProperty("JButton.buttonType", "roundRect");
        btnDelete.setForeground(new Color(211, 47, 47));
        btnDelete.addActionListener(e -> deleteWallet(wallet));

        bottom.add(btnEdit);
        bottom.add(btnDelete);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private void deleteWallet(Wallet wallet) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Bạn có chắc muốn xóa ví '%s'?\nLưu ý: Tất cả các giao dịch liên quan đến ví này cũng sẽ bị xóa.", wallet.getName()),
                "Xác nhận xóa ví",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = walletDAO.deleteWallet(wallet.getId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa ví thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
                if (onDataChangedCallback != null) {
                    onDataChangedCallback.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa ví. Vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
