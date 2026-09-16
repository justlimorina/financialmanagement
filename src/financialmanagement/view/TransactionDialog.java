package financialmanagement.view;

import financialmanagement.dao.CategoryDAO;
import financialmanagement.dao.TransactionDAO;
import financialmanagement.dao.WalletDAO;
import financialmanagement.model.Category;
import financialmanagement.model.Transaction;
import financialmanagement.model.TransactionType;
import financialmanagement.model.Wallet;
import financialmanagement.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class TransactionDialog extends JDialog {
    private final WalletDAO walletDAO = new WalletDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private JComboBox<String> cbType;
    private JComboBox<Wallet> cbWallet;
    private JComboBox<Wallet> cbToWallet;
    private JComboBox<Category> cbCategory;
    private JTextField txtAmount;
    private JTextField txtDate;
    private JTextField txtNote;

    private JLabel lblToWallet;
    private JLabel lblCategory;

    private final Runnable onSuccessCallback;

    public TransactionDialog(Frame parent, Runnable onSuccessCallback) {
        super(parent, "Thêm Giao Dịch Mới", true);
        this.onSuccessCallback = onSuccessCallback;

        initComponents();
        loadData();

        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // 1. Loại giao dịch
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Loại giao dịch:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cbType = new JComboBox<>(new String[]{"Chi tiêu", "Thu nhập", "Chuyển khoản"});
        cbType.addActionListener(e -> onTypeChanged());
        form.add(cbType, gbc);

        // 2. Số tiền
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Số tiền (VNĐ):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtAmount = new JTextField();
        txtAmount.putClientProperty("JTextField.placeholderText", "Ví dụ: 50000");
        form.add(txtAmount, gbc);

        // 3. Ví thanh toán
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Từ ví:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cbWallet = new JComboBox<>();
        form.add(cbWallet, gbc);

        // 4. Đến ví (chỉ dùng khi chuyển khoản)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        lblToWallet = new JLabel("Đến ví:");
        form.add(lblToWallet, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cbToWallet = new JComboBox<>();
        form.add(cbToWallet, gbc);

        // 5. Danh mục
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        lblCategory = new JLabel("Danh mục:");
        form.add(lblCategory, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cbCategory = new JComboBox<>();
        form.add(cbCategory, gbc);

        // 6. Ngày giao dịch
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Ngày (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDate = new JTextField(LocalDate.now().toString());
        form.add(txtDate, gbc);

        // 7. Ghi chú
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtNote = new JTextField();
        txtNote.putClientProperty("JTextField.placeholderText", "Chi tiết giao dịch...");
        form.add(txtNote, gbc);

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu Giao Dịch");
        btnSave.putClientProperty("JButton.buttonType", "roundRect");
        btnSave.setBackground(new Color(33, 150, 243));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveTransaction());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadData() {
        // Load wallets
        List<Wallet> wallets = walletDAO.getAllWallets();
        cbWallet.removeAllItems();
        cbToWallet.removeAllItems();
        for (Wallet w : wallets) {
            cbWallet.addItem(w);
            cbToWallet.addItem(w);
        }

        onTypeChanged();
    }

    private void onTypeChanged() {
        int selectedIndex = cbType.getSelectedIndex();
        boolean isTransfer = (selectedIndex == 2);

        lblToWallet.setVisible(isTransfer);
        cbToWallet.setVisible(isTransfer);

        lblCategory.setVisible(!isTransfer);
        cbCategory.setVisible(!isTransfer);

        if (!isTransfer) {
            TransactionType type = (selectedIndex == 0) ? TransactionType.EXPENSE : TransactionType.INCOME;
            List<Category> categories = categoryDAO.getCategoriesByType(type);
            cbCategory.removeAllItems();
            for (Category c : categories) {
                cbCategory.addItem(c);
            }
        }
    }

    private void saveTransaction() {
        // 1. Kiểm tra số tiền
        double amount = CurrencyFormatter.parseNumber(txtAmount.getText().trim());
        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền hợp lệ (> 0)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtAmount.requestFocus();
            return;
        }

        // 2. Kiểm tra ví
        Wallet selectedWallet = (Wallet) cbWallet.getSelectedItem();
        if (selectedWallet == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ví nguồn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int typeIndex = cbType.getSelectedIndex();
        TransactionType type;
        Integer toWalletId = null;
        Integer categoryId = null;

        if (typeIndex == 0) {
            type = TransactionType.EXPENSE;
        } else if (typeIndex == 1) {
            type = TransactionType.INCOME;
        } else {
            type = TransactionType.TRANSFER;
            Wallet toWallet = (Wallet) cbToWallet.getSelectedItem();
            if (toWallet == null || toWallet.getId() == selectedWallet.getId()) {
                JOptionPane.showMessageDialog(this, "Ví nhận tiền phải khác ví gửi tiền!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            toWalletId = toWallet.getId();
        }

        if (type != TransactionType.TRANSFER) {
            Category category = (Category) cbCategory.getSelectedItem();
            if (category != null) {
                categoryId = category.getId();
            }
        }

        String date = txtDate.getText().trim();
        if (date.isEmpty()) {
            date = LocalDate.now().toString();
        }

        String note = txtNote.getText().trim();

        // 3. Tạo Transaction và lưu vào DAO
        Transaction tx = new Transaction(selectedWallet.getId(), categoryId, amount, type, toWalletId, date, note);
        boolean success = transactionDAO.addTransaction(tx);

        if (success) {
            JOptionPane.showMessageDialog(this, "Đã thêm giao dịch thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu giao dịch. Vui lòng kiểm tra lại!", "Thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }
}
