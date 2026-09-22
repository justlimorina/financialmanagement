package financialmanagement.view;

import financialmanagement.dao.BudgetDAO;
import financialmanagement.model.Budget;
import financialmanagement.util.AppFont;
import financialmanagement.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class BudgetPanel extends JPanel {
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final Frame parentFrame;
    private final Runnable onDataChangedCallback;

    private JComboBox<Integer> cbMonth;
    private JSpinner spYear;
    private JLabel lblTotalBudget;
    private JLabel lblTotalSpent;
    private JLabel lblTotalRemaining;
    private JProgressBar overallBar;

    private JPanel budgetListContainer;

    public BudgetPanel(Frame parentFrame, Runnable onDataChangedCallback) {
        this.parentFrame = parentFrame;
        this.onDataChangedCallback = onDataChangedCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        LocalDate now = LocalDate.now();

        // 1. Header (Title + Month/Year filter + Add Button)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        JLabel lblTitle = new JLabel("Kế Hoạch Ngân Sách");
        lblTitle.setFont(AppFont.bold(21));

        JLabel lblSub = new JLabel("Kiểm soát hạn mức chi tiêu hàng tháng theo từng danh mục");
        lblSub.setFont(AppFont.plain(12));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        // Control Box (Month, Year, Add Button)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        controls.add(new JLabel("Tháng:"));
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        cbMonth = new JComboBox<>(months);
        cbMonth.setSelectedItem(now.getMonthValue());
        cbMonth.addActionListener(e -> refreshData());
        controls.add(cbMonth);

        controls.add(new JLabel("Năm:"));
        spYear = new JSpinner(new SpinnerNumberModel(now.getYear(), 2020, 2040, 1));
        spYear.setPreferredSize(new Dimension(80, 30));
        spYear.addChangeListener(e -> refreshData());
        controls.add(spYear);

        JButton btnAddBudget = new JButton("+ Đặt Hạn Mức");
        btnAddBudget.setFont(AppFont.bold(13));
        btnAddBudget.putClientProperty("JButton.buttonType", "roundRect");
        btnAddBudget.setBackground(new Color(33, 150, 243));
        btnAddBudget.setForeground(Color.WHITE);
        btnAddBudget.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddBudget.addActionListener(e -> {
            int selectedMonth = (Integer) cbMonth.getSelectedItem();
            int selectedYear = (Integer) spYear.getValue();
            BudgetDialog dialog = new BudgetDialog(parentFrame, null, selectedMonth, selectedYear, () -> {
                refreshData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            });
            dialog.setVisible(true);
        });
        controls.add(btnAddBudget);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(controls, BorderLayout.EAST);

        // 2. Summary Card
        JPanel summaryCard = new JPanel(new BorderLayout(10, 10));
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 20, 0));
        statsRow.setOpaque(false);

        lblTotalBudget = new JLabel("Tổng ngân sách: 0 ₫");
        lblTotalBudget.setFont(AppFont.bold(15));

        lblTotalSpent = new JLabel("Đã chi: 0 ₫");
        lblTotalSpent.setFont(AppFont.bold(15));
        lblTotalSpent.setForeground(new Color(244, 67, 54));

        lblTotalRemaining = new JLabel("Còn lại: 0 ₫");
        lblTotalRemaining.setFont(AppFont.bold(15));
        lblTotalRemaining.setForeground(new Color(76, 175, 80));

        statsRow.add(lblTotalBudget);
        statsRow.add(lblTotalSpent);
        statsRow.add(lblTotalRemaining);

        overallBar = new JProgressBar(0, 100);
        overallBar.setStringPainted(true);
        overallBar.setPreferredSize(new Dimension(0, 18));

        summaryCard.add(statsRow, BorderLayout.NORTH);
        summaryCard.add(overallBar, BorderLayout.SOUTH);

        JPanel northGroup = new JPanel(new BorderLayout(0, 12));
        northGroup.setOpaque(false);
        northGroup.add(headerPanel, BorderLayout.NORTH);
        northGroup.add(summaryCard, BorderLayout.SOUTH);
        add(northGroup, BorderLayout.NORTH);

        // 3. List Container of Budgets
        budgetListContainer = new JPanel();
        budgetListContainer.setLayout(new BoxLayout(budgetListContainer, BoxLayout.Y_AXIS));
        budgetListContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(budgetListContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        budgetListContainer.removeAll();

        int month = (Integer) cbMonth.getSelectedItem();
        int year = (Integer) spYear.getValue();

        List<Budget> budgets = budgetDAO.getBudgetsForMonth(month, year);

        double totalBudget = 0;
        double totalSpent = 0;

        for (Budget b : budgets) {
            totalBudget += b.getAmountLimit();
            totalSpent += b.getSpentAmount();
            budgetListContainer.add(createBudgetItem(b));
            budgetListContainer.add(Box.createVerticalStrut(10));
        }

        double remaining = totalBudget - totalSpent;
        lblTotalBudget.setText("Tổng hạn mức: " + CurrencyFormatter.formatVND(totalBudget));
        lblTotalSpent.setText("Đã chi: " + CurrencyFormatter.formatVND(totalSpent));
        lblTotalRemaining.setText(String.format("Còn lại: %s%s", (remaining >= 0 ? "" : "-"), CurrencyFormatter.formatVND(Math.abs(remaining))));
        lblTotalRemaining.setForeground(remaining >= 0 ? new Color(76, 175, 80) : new Color(229, 57, 53));

        int overallPercent = (totalBudget > 0) ? (int) Math.round((totalSpent / totalBudget) * 100.0) : 0;
        overallBar.setValue(Math.min(100, overallPercent));
        overallBar.setString(String.format("Tổng thể: %d%% (%s / %s)", overallPercent, CurrencyFormatter.formatVND(totalSpent), CurrencyFormatter.formatVND(totalBudget)));

        if (overallPercent >= 100) {
            overallBar.setForeground(new Color(229, 57, 53));
        } else if (overallPercent >= 80) {
            overallBar.setForeground(new Color(255, 179, 0));
        } else {
            overallBar.setForeground(new Color(67, 160, 71));
        }

        if (budgets.isEmpty()) {
            JLabel lblEmpty = new JLabel("Chưa có hạn mức nào được thiết lập cho Tháng " + month + "/" + year, SwingConstants.CENTER);
            lblEmpty.setFont(AppFont.italic(14));
            lblEmpty.setForeground(UIManager.getColor("Label.disabledForeground"));
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblEmpty.setBorder(new EmptyBorder(40, 0, 0, 0));
            budgetListContainer.add(lblEmpty);
        }

        budgetListContainer.revalidate();
        budgetListContainer.repaint();
    }

    private JPanel createBudgetItem(Budget budget) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Header: Category name + Spent / Limit + Action buttons
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblName = new JLabel(budget.getCategoryName());
        lblName.setFont(AppFont.bold(14));

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        JLabel lblAmounts = new JLabel(String.format("%s / %s (%.1f%%)",
                CurrencyFormatter.formatVND(budget.getSpentAmount()),
                CurrencyFormatter.formatVND(budget.getAmountLimit()),
                budget.getProgressPercentage()));
        lblAmounts.setFont(AppFont.bold(13));

        JButton btnEdit = new JButton("Sửa");
        btnEdit.setToolTipText("Sửa hạn mức");
        btnEdit.setFont(AppFont.plain(12));
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnEdit.addActionListener(e -> {
            BudgetDialog dialog = new BudgetDialog(parentFrame, budget, budget.getMonth(), budget.getYear(), () -> {
                refreshData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            });
            dialog.setVisible(true);
        });

        JButton btnDelete = new JButton("Xóa");
        btnDelete.setToolTipText("Xóa ngân sách");
        btnDelete.setFont(AppFont.plain(12));
        btnDelete.putClientProperty("JButton.buttonType", "roundRect");
        btnDelete.setForeground(new Color(211, 47, 47));
        btnDelete.addActionListener(e -> deleteBudget(budget));

        rightBox.add(lblAmounts);
        rightBox.add(btnEdit);
        rightBox.add(btnDelete);

        top.add(lblName, BorderLayout.WEST);
        top.add(rightBox, BorderLayout.EAST);

        // Progress Bar
        JProgressBar bar = new JProgressBar(0, 100);
        int progress = (int) Math.round(budget.getProgressPercentage());
        bar.setValue(Math.min(100, progress));
        bar.setStringPainted(true);
        bar.setPreferredSize(new Dimension(0, 14));

        double rem = budget.getRemainingAmount();
        String statusText;
        if (progress >= 100) {
            bar.setForeground(new Color(229, 57, 53)); // Đỏ
            statusText = "⚠️ Đã vượt ngân sách " + CurrencyFormatter.formatVND(Math.abs(rem));
            bar.setString(statusText);
        } else if (progress >= 80) {
            bar.setForeground(new Color(255, 179, 0)); // Vàng
            statusText = "Cảnh báo: Còn lại " + CurrencyFormatter.formatVND(rem);
            bar.setString(statusText);
        } else {
            bar.setForeground(new Color(67, 160, 71)); // Xanh lá
            statusText = "Còn lại " + CurrencyFormatter.formatVND(rem);
            bar.setString(statusText);
        }

        card.add(top, BorderLayout.NORTH);
        card.add(bar, BorderLayout.SOUTH);

        return card;
    }

    private void deleteBudget(Budget budget) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Bạn có chắc muốn xóa hạn mức ngân sách '%s' cho Tháng %d/%d?",
                        budget.getCategoryName(), budget.getMonth(), budget.getYear()),
                "Xác nhận xóa ngân sách",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = budgetDAO.deleteBudget(budget.getId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa ngân sách thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
                if (onDataChangedCallback != null) {
                    onDataChangedCallback.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa ngân sách. Vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
