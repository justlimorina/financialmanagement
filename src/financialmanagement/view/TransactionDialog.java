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

    private final Transaction transactionToEdit;
    private final Runnable onSuccessCallback;

    private JComboBox<String> cbType;
    private JComboBox<Wallet> cbWallet;
    private JComboBox<Wallet> cbToWallet;
    private JComboBox<Category> cbCategory;
    private JTextField txtAmount;
    private JTextField txtDate;
    private JTextField txtNote;

    private JLabel lblToWallet;
    private JLabel lblCategory;

    public TransactionDialog(Frame parent, Runnable onSuccessCallback) {
        this(parent, null, onSuccessCallback);
    }

    public TransactionDialog(Frame parent, Transaction transactionToEdit, Runnable onSuccessCallback) {
        super(parent, transactionToEdit == null ? "Thêm Giao Dịch Mới" : "Chỉnh Sửa Giao Dịch #" + transactionToEdit.getId(), true);
        this.transactionToEdit = transactionToEdit;
        this.onSuccessCallback = onSuccessCallback;

        initComponents();
        loadData();
        populateDataIfEditing();

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
        btnCancel.putClientProperty("JButton.buttonType", "roundRect");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(transactionToEdit == null ? "Lưu Giao Dịch" : "Lưu Thay Đổi");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
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

    private void populateDataIfEditing() {
        if (transactionToEdit == null) return;

        // 1. Set type
        if (transactionToEdit.getType() == TransactionType.EXPENSE) {
            cbType.setSelectedIndex(0);
        } else if (transactionToEdit.getType() == TransactionType.INCOME) {
            cbType.setSelectedIndex(1);
        } else {
            cbType.setSelectedIndex(2);
        }
        onTypeChanged();

        // 2. Set amount
        long amtLong = (long) transactionToEdit.getAmount();
        if (transactionToEdit.getAmount() == amtLong) {
            txtAmount.setText(String.valueOf(amtLong));
        } else {
            txtAmount.setText(String.valueOf(transactionToEdit.getAmount()));
        }

        // 3. Set wallet
        for (int i = 0; i < cbWallet.getItemCount(); i++) {
            Wallet w = cbWallet.getItemAt(i);
            if (w.getId() == transactionToEdit.getWalletId()) {
                cbWallet.setSelectedIndex(i);
                break;
            }
        }

        // 4. Set to_wallet (for transfer)
        if (transactionToEdit.getToWalletId() != null) {
            for (int i = 0; i < cbToWallet.getItemCount(); i++) {
                Wallet w = cbToWallet.getItemAt(i);
                if (w.getId() == transactionToEdit.getToWalletId()) {
                    cbToWallet.setSelectedIndex(i);
                    break;
                }
            }
        }

        // 5. Set category
        if (transactionToEdit.getCategoryId() != null) {
            for (int i = 0; i < cbCategory.getItemCount(); i++) {
                Category c = cbCategory.getItemAt(i);
                if (c.getId() == transactionToEdit.getCategoryId()) {
                    cbCategory.setSelectedIndex(i);
                    break;
                }
            }
        }

        // 6. Set date & note
        if (transactionToEdit.getTransactionDate() != null) {
            txtDate.setText(transactionToEdit.getTransactionDate());
        }
        if (transactionToEdit.getNote() != null) {
            txtNote.setText(transactionToEdit.getNote());
        }
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

        boolean success;
        if (transactionToEdit == null) {
            // Thêm mới
            Transaction tx = new Transaction(selectedWallet.getId(), categoryId, amount, type, toWalletId, date, note);
            success = transactionDAO.addTransaction(tx);
        } else {
            // Chỉnh sửa
            transactionToEdit.setWalletId(selectedWallet.getId());
            transactionToEdit.setCategoryId(categoryId);
            transactionToEdit.setAmount(amount);
            transactionToEdit.setType(type);
            transactionToEdit.setToWalletId(toWalletId);
            transactionToEdit.setTransactionDate(date);
            transactionToEdit.setNote(note);
            success = transactionDAO.updateTransaction(transactionToEdit);
        }

        if (success) {
            JOptionPane.showMessageDialog(this,
                    transactionToEdit == null ? "Đã thêm giao dịch thành công!" : "Đã cập nhật giao dịch thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu giao dịch. Vui lòng kiểm tra lại!", "Thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }
}
