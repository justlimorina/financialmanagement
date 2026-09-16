package financialmanagement.view;

import financialmanagement.dao.WalletDAO;
import financialmanagement.model.Wallet;
import financialmanagement.model.WalletType;
import financialmanagement.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WalletDialog extends JDialog {
    private final WalletDAO walletDAO = new WalletDAO();
    private final Wallet existingWallet;
    private final Runnable onSuccessCallback;

    private JTextField txtName;
    private JComboBox<WalletType> cbType;
    private JTextField txtBalance;

    public WalletDialog(Frame parent, Wallet existingWallet, Runnable onSuccessCallback) {
        super(parent, existingWallet == null ? "Thêm Ví / Tài Khoản Mới" : "Chỉnh Sửa Ví", true);
        this.existingWallet = existingWallet;
        this.onSuccessCallback = onSuccessCallback;

        initComponents();

        if (existingWallet != null) {
            txtName.setText(existingWallet.getName());
            cbType.setSelectedItem(existingWallet.getType());
            txtBalance.setText(CurrencyFormatter.formatNumber(existingWallet.getBalance()));
        }

        setSize(420, 320);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 1. Tên ví
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        form.add(new JLabel("Tên ví / Tài khoản:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtName = new JTextField();
        txtName.putClientProperty("JTextField.placeholderText", "Ví dụ: MoMo, MB Bank...");
        form.add(txtName, gbc);

        // 2. Loại ví
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        form.add(new JLabel("Loại ví:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        cbType = new JComboBox<>(WalletType.values());
        cbType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof WalletType) {
                    setText(((WalletType) value).getDisplayName());
                }
                return c;
            }
        });
        form.add(cbType, gbc);

        // 3. Số dư
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        form.add(new JLabel(existingWallet == null ? "Số dư ban đầu (VNĐ):" : "Số dư (VNĐ):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtBalance = new JTextField("0");
        form.add(txtBalance, gbc);

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu Ví");
        btnSave.putClientProperty("JButton.buttonType", "roundRect");
        btnSave.setBackground(new Color(33, 150, 243));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveWallet());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void saveWallet() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên ví!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }

        double balance = CurrencyFormatter.parseNumber(txtBalance.getText().trim());
        WalletType type = (WalletType) cbType.getSelectedItem();

        boolean success;
        if (existingWallet == null) {
            Wallet newWallet = new Wallet(name, balance, type);
            success = walletDAO.addWallet(newWallet);
        } else {
            existingWallet.setName(name);
            existingWallet.setType(type);
            existingWallet.setBalance(balance);
            success = walletDAO.updateWallet(existingWallet);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu thông tin ví thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu ví. Vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
