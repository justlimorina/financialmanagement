package financialmanagement.view;

import financialmanagement.dao.BudgetDAO;
import financialmanagement.dao.CategoryDAO;
import financialmanagement.model.Budget;
import financialmanagement.model.Category;
import financialmanagement.model.TransactionType;
import financialmanagement.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class BudgetDialog extends JDialog {
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final Budget existingBudget;
    private final Runnable onSuccessCallback;

    private JComboBox<Category> cbCategory;
    private JTextField txtLimit;
    private JComboBox<Integer> cbMonth;
    private JTextField txtYear;

    public BudgetDialog(Frame parent, Budget existingBudget, int defaultMonth, int defaultYear, Runnable onSuccessCallback) {
        super(parent, existingBudget == null ? "Thiết Lập Ngân Sách Mới" : "Chỉnh Sửa Ngân Sách", true);
        this.existingBudget = existingBudget;
        this.onSuccessCallback = onSuccessCallback;

        initComponents(defaultMonth, defaultYear);
        loadCategories();

        if (existingBudget != null) {
            txtLimit.setText(CurrencyFormatter.formatNumber(existingBudget.getAmountLimit()));
            cbMonth.setSelectedItem(existingBudget.getMonth());
            txtYear.setText(String.valueOf(existingBudget.getYear()));
        }

        setSize(440, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents(int defaultMonth, int defaultYear) {
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 1. Danh mục chi tiêu
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        form.add(new JLabel("Danh mục chi tiêu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        cbCategory = new JComboBox<>();
        form.add(cbCategory, gbc);

        // 2. Hạn mức chi tiêu
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        form.add(new JLabel("Hạn mức (VNĐ):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtLimit = new JTextField();
        txtLimit.putClientProperty("JTextField.placeholderText", "Ví dụ: 3000000");
        form.add(txtLimit, gbc);

        // 3. Tháng
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        form.add(new JLabel("Tháng áp dụng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        cbMonth = new JComboBox<>(months);
        cbMonth.setSelectedItem(defaultMonth);
        form.add(cbMonth, gbc);

        // 4. Năm
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        form.add(new JLabel("Năm:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtYear = new JTextField(String.valueOf(defaultYear));
        form.add(txtYear, gbc);

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu Ngân Sách");
        btnSave.putClientProperty("JButton.buttonType", "roundRect");
        btnSave.setBackground(new Color(33, 150, 243));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveBudget());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadCategories() {
        List<Category> expenseCats = categoryDAO.getCategoriesByType(TransactionType.EXPENSE);
        cbCategory.removeAllItems();
        for (Category c : expenseCats) {
            cbCategory.addItem(c);
            if (existingBudget != null && c.getId() == existingBudget.getCategoryId()) {
                cbCategory.setSelectedItem(c);
            }
        }
    }

    private void saveBudget() {
        Category cat = (Category) cbCategory.getSelectedItem();
        if (cat == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục chi tiêu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double limit = CurrencyFormatter.parseNumber(txtLimit.getText().trim());
        if (limit <= 0) {
            JOptionPane.showMessageDialog(this, "Hạn mức phải lớn hơn 0 VNĐ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtLimit.requestFocus();
            return;
        }

        int month = (Integer) cbMonth.getSelectedItem();
        int year;
        try {
            year = Integer.parseInt(txtYear.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Năm không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtYear.requestFocus();
            return;
        }

        Budget budget = new Budget(cat.getId(), limit, month, year);
        boolean success = budgetDAO.saveOrUpdateBudget(budget);

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu hạn mức ngân sách thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu ngân sách. Vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
