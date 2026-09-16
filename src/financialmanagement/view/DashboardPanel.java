package financialmanagement.view;

import financialmanagement.dao.BudgetDAO;
import financialmanagement.dao.TransactionDAO;
import financialmanagement.dao.WalletDAO;
import financialmanagement.model.Budget;
import financialmanagement.model.Transaction;
import financialmanagement.model.TransactionType;
import financialmanagement.util.CurrencyFormatter;
import financialmanagement.view.components.CardPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final WalletDAO walletDAO = new WalletDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();

    private CardPanel cardTotal;
    private CardPanel cardIncome;
    private CardPanel cardExpense;

    private JPanel chartContainer;
    private JPanel budgetContainer;
    private JTable recentTable;
    private DefaultTableModel tableModel;

    private final Frame parentFrame;

    public DashboardPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // 1. Header (Title + Quick Action Button)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel lblGreeting = new JLabel("Tổng quan Tài chính");
        lblGreeting.setFont(new Font("Segoe UI", Font.BOLD, 22));

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("'Hôm nay,' EEEE, dd/MM/yyyy"));
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDate.setForeground(UIManager.getColor("Label.disabledForeground"));

        titleBox.add(lblGreeting);
        titleBox.add(lblDate);

        JButton btnAddTransaction = new JButton("+ Thêm Giao Dịch");
        btnAddTransaction.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddTransaction.putClientProperty("JButton.buttonType", "roundRect");
        btnAddTransaction.setBackground(new Color(33, 150, 243));
        btnAddTransaction.setForeground(Color.WHITE);
        btnAddTransaction.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddTransaction.addActionListener(e -> {
            TransactionDialog dialog = new TransactionDialog(parentFrame, this::refreshData);
            dialog.setVisible(true);
        });

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(btnAddTransaction, BorderLayout.EAST);

        // 2. Metrics Cards (Grid 1x3)
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 15, 0));
        cardsGrid.setOpaque(false);

        cardTotal = new CardPanel("Tổng tài sản", "0 ₫", "Tất cả các ví", new Color(33, 150, 243), "💰");
        cardIncome = new CardPanel("Thu nhập tháng này", "0 ₫", "Tháng hiện tại", new Color(76, 175, 80), "📈");
        cardExpense = new CardPanel("Chi tiêu tháng này", "0 ₫", "Tháng hiện tại", new Color(244, 67, 54), "📉");

        cardsGrid.add(cardTotal);
        cardsGrid.add(cardIncome);
        cardsGrid.add(cardExpense);

        // Gom Header + Cards vào North Panel
        JPanel northPanel = new JPanel(new BorderLayout(0, 15));
        northPanel.setOpaque(false);
        northPanel.add(headerPanel, BorderLayout.NORTH);
        northPanel.add(cardsGrid, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);

        // 3. Center Section: Left (Pie Chart) + Right (Budget Progress)
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 15, 0));
        middlePanel.setOpaque(false);

        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createTitledBorder("Cơ cấu Chi tiêu Tháng Này"));
        chartContainer.setPreferredSize(new Dimension(380, 240));

        budgetContainer = new JPanel();
        budgetContainer.setLayout(new BoxLayout(budgetContainer, BoxLayout.Y_AXIS));
        budgetContainer.setBorder(BorderFactory.createTitledBorder("Tiến độ Ngân sách Tháng Này"));

        middlePanel.add(chartContainer);
        middlePanel.add(new JScrollPane(budgetContainer));

        // 4. Bottom Section: Recent Transactions Table
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Giao dịch gần đây"));

        String[] columns = {"Ngày", "Loại", "Danh mục", "Ví", "Số tiền", "Ghi chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTable = new JTable(tableModel);
        recentTable.setRowHeight(32);
        recentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        recentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        recentTable.setFillsViewportHeight(true);

        // Center align & color for amount column
        recentTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String valStr = (value != null) ? value.toString() : "";
                if (valStr.startsWith("+")) {
                    c.setForeground(new Color(46, 125, 50));
                } else if (valStr.startsWith("-")) {
                    c.setForeground(new Color(198, 40, 40));
                } else {
                    c.setForeground(table.getForeground());
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(recentTable);
        scrollTable.setPreferredSize(new Dimension(800, 180));
        bottomPanel.add(scrollTable, BorderLayout.CENTER);

        // Center Wrapper containing middlePanel & bottomPanel
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);
        centerWrapper.add(middlePanel, BorderLayout.CENTER);
        centerWrapper.add(bottomPanel, BorderLayout.SOUTH);

        add(centerWrapper, BorderLayout.CENTER);
    }

    public void refreshData() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        // 1. Cập nhật thẻ chỉ số
        double totalBalance = walletDAO.getTotalBalance();
        double[] monthlySummary = transactionDAO.getMonthlySummary(month, year);

        cardTotal.setValue(CurrencyFormatter.formatVND(totalBalance));
        cardIncome.setValue("+" + CurrencyFormatter.formatVND(monthlySummary[0]));
        cardExpense.setValue("-" + CurrencyFormatter.formatVND(monthlySummary[1]));

        // 2. Cập nhật Biểu đồ tròn
        updateChart(month, year);

        // 3. Cập nhật Ngân sách
        updateBudgets(month, year);

        // 4. Cập nhật Bảng giao dịch gần đây
        updateRecentTransactions();
    }

    private void updateChart(int month, int year) {
        chartContainer.removeAll();
        Map<String, Double> expenseData = transactionDAO.getMonthlyExpenseByCategory(month, year);

        if (expenseData.isEmpty()) {
            JLabel lblEmpty = new JLabel("Chưa có dữ liệu chi tiêu tháng này", SwingConstants.CENTER);
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblEmpty.setForeground(UIManager.getColor("Label.disabledForeground"));
            chartContainer.add(lblEmpty, BorderLayout.CENTER);
        } else {
            DefaultPieDataset dataset = new DefaultPieDataset();
            for (Map.Entry<String, Double> entry : expenseData.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }

            JFreeChart chart = ChartFactory.createPieChart(
                    null,
                    dataset,
                    true,
                    true,
                    false
            );

            // Styling chart
            chart.setBackgroundPaint(null);
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setBackgroundPaint(null);
            plot.setOutlinePaint(null);
            plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            plot.setShadowPaint(null);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setOpaque(false);
            chartContainer.add(chartPanel, BorderLayout.CENTER);
        }
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void updateBudgets(int month, int year) {
        budgetContainer.removeAll();
        List<Budget> budgets = budgetDAO.getBudgetsForMonth(month, year);

        if (budgets.isEmpty()) {
            JLabel lblEmpty = new JLabel("Chưa có hạn mức ngân sách tháng này", SwingConstants.CENTER);
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblEmpty.setForeground(UIManager.getColor("Label.disabledForeground"));
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblEmpty.setBorder(new EmptyBorder(30, 0, 0, 0));
            budgetContainer.add(lblEmpty);
        } else {
            for (Budget b : budgets) {
                JPanel item = new JPanel(new BorderLayout(5, 5));
                item.setOpaque(false);
                item.setBorder(new EmptyBorder(8, 12, 8, 12));

                JLabel lblName = new JLabel(b.getCategoryName());
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));

                JLabel lblAmount = new JLabel(String.format("%s / %s (%.0f%%)",
                        CurrencyFormatter.formatVND(b.getSpentAmount()),
                        CurrencyFormatter.formatVND(b.getAmountLimit()),
                        b.getProgressPercentage()));
                lblAmount.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                JProgressBar bar = new JProgressBar(0, 100);
                int progress = (int) Math.round(b.getProgressPercentage());
                bar.setValue(progress);
                bar.setStringPainted(true);

                if (progress >= 100) {
                    bar.setForeground(new Color(229, 57, 53)); // Đỏ
                } else if (progress >= 80) {
                    bar.setForeground(new Color(255, 179, 0)); // Vàng cam
                } else {
                    bar.setForeground(new Color(67, 160, 71)); // Xanh lá
                }

                item.add(lblName, BorderLayout.WEST);
                item.add(lblAmount, BorderLayout.EAST);
                item.add(bar, BorderLayout.SOUTH);

                budgetContainer.add(item);
                budgetContainer.add(Box.createVerticalStrut(5));
            }
        }
        budgetContainer.revalidate();
        budgetContainer.repaint();
    }

    private void updateRecentTransactions() {
        tableModel.setRowCount(0);
        List<Transaction> transactions = transactionDAO.getRecentTransactions(8);

        for (Transaction tx : transactions) {
            String typeStr;
            String amountFormatted;

            if (tx.getType() == TransactionType.INCOME) {
                typeStr = "Thu nhập";
                amountFormatted = "+" + CurrencyFormatter.formatVND(tx.getAmount());
            } else if (tx.getType() == TransactionType.EXPENSE) {
                typeStr = "Chi tiêu";
                amountFormatted = "-" + CurrencyFormatter.formatVND(tx.getAmount());
            } else {
                typeStr = "Chuyển khoản";
                amountFormatted = CurrencyFormatter.formatVND(tx.getAmount());
            }

            String catStr = (tx.getCategoryName() != null) ? tx.getCategoryName() : "—";
            String walletStr = (tx.getType() == TransactionType.TRANSFER) 
                    ? (tx.getWalletName() + " ➔ " + (tx.getToWalletName() != null ? tx.getToWalletName() : "?"))
                    : tx.getWalletName();

            tableModel.addRow(new Object[]{
                    tx.getTransactionDate(),
                    typeStr,
                    catStr,
                    walletStr,
                    amountFormatted,
                    (tx.getNote() != null) ? tx.getNote() : ""
            });
        }
    }
}
