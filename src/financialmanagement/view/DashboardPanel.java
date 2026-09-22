package financialmanagement.view;

import financialmanagement.dao.BudgetDAO;
import financialmanagement.dao.TransactionDAO;
import financialmanagement.dao.WalletDAO;
import financialmanagement.model.Budget;
import financialmanagement.model.Transaction;
import financialmanagement.model.TransactionType;
import financialmanagement.util.AppFont;
import financialmanagement.util.CurrencyFormatter;
import financialmanagement.util.IconHelper;
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
        setLayout(new BorderLayout());

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Content container inside JScrollPane for responsive display on 1366x768
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Header (Greeting + Quick Action)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);
        JLabel lblGreeting = new JLabel("Tổng quan Tài chính");
        lblGreeting.setFont(AppFont.bold(23));

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("'Hôm nay,' EEEE, dd/MM/yyyy"));
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(AppFont.plain(13));
        lblDate.setForeground(UIManager.getColor("Label.disabledForeground"));

        titleBox.add(lblGreeting);
        titleBox.add(lblDate);

        JButton btnAddTransaction = new JButton("+ Thêm Giao Dịch");
        btnAddTransaction.setFont(AppFont.bold(13));
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
        content.add(headerPanel);
        content.add(Box.createVerticalStrut(14));

        // 2. Metrics Cards (Grid 1x3)
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        cardsGrid.setOpaque(false);

        cardTotal = new CardPanel("Tổng tài sản", "0 ₫", "Tất cả các ví", new Color(33, 150, 243), IconHelper.ACCOUNT_BALANCE_WALLET);
        cardIncome = new CardPanel("Thu nhập tháng này", "0 ₫", "Tháng hiện tại", new Color(76, 175, 80), IconHelper.TRENDING_UP);
        cardExpense = new CardPanel("Chi tiêu tháng này", "0 ₫", "Tháng hiện tại", new Color(244, 67, 54), IconHelper.TRENDING_DOWN);

        cardsGrid.add(cardTotal);
        cardsGrid.add(cardIncome);
        cardsGrid.add(cardExpense);
        content.add(cardsGrid);
        content.add(Box.createVerticalStrut(14));

        // 3. Middle Section: Chart Card + Budget Card
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 14, 0));
        middlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        middlePanel.setPreferredSize(new Dimension(800, 250));
        middlePanel.setOpaque(false);

        // Chart Card
        JPanel chartCard = createSectionCard("Cơ cấu Chi tiêu Tháng Này");
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setOpaque(false);
        chartCard.add(chartContainer, BorderLayout.CENTER);

        // Budget Card
        JPanel budgetCard = createSectionCard("Tiến độ Ngân sách Tháng Này");
        budgetContainer = new JPanel();
        budgetContainer.setLayout(new BoxLayout(budgetContainer, BoxLayout.Y_AXIS));
        budgetContainer.setOpaque(false);
        JScrollPane budgetScroll = new JScrollPane(budgetContainer);
        budgetScroll.setBorder(null);
        budgetScroll.setOpaque(false);
        budgetScroll.getViewport().setOpaque(false);
        budgetCard.add(budgetScroll, BorderLayout.CENTER);

        middlePanel.add(chartCard);
        middlePanel.add(budgetCard);
        content.add(middlePanel);
        content.add(Box.createVerticalStrut(14));

        // 4. Bottom Section: Recent Transactions Table
        JPanel tableCard = createSectionCard("Giao dịch gần đây");
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        tableCard.setPreferredSize(new Dimension(800, 200));

        String[] columns = {"Ngày", "Loại", "Danh mục", "Ví", "Số tiền", "Ghi chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTable = new JTable(tableModel);
        recentTable.setRowHeight(34);
        recentTable.setFont(AppFont.plain(13));
        recentTable.getTableHeader().setFont(AppFont.bold(13));
        recentTable.setFillsViewportHeight(true);

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
        scrollTable.setBorder(null);
        tableCard.add(scrollTable, BorderLayout.CENTER);

        content.add(tableCard);

        // Wrap everything in a smooth vertical JScrollPane
        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel createSectionCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFont.bold(15));
        card.add(lblTitle, BorderLayout.NORTH);
        return card;
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
            lblEmpty.setFont(AppFont.italic(13));
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

            chart.setBackgroundPaint(null);
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setBackgroundPaint(null);
            plot.setOutlinePaint(null);
            plot.setLabelFont(AppFont.plain(11));
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
            lblEmpty.setFont(AppFont.italic(13));
            lblEmpty.setForeground(UIManager.getColor("Label.disabledForeground"));
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblEmpty.setBorder(new EmptyBorder(30, 0, 0, 0));
            budgetContainer.add(lblEmpty);
        } else {
            for (Budget b : budgets) {
                JPanel item = new JPanel(new BorderLayout(5, 5));
                item.setOpaque(false);
                item.setBorder(new EmptyBorder(6, 8, 6, 8));

                JLabel lblName = new JLabel(b.getCategoryName());
                lblName.setFont(AppFont.bold(13));

                JLabel lblAmount = new JLabel(String.format("%s / %s (%.0f%%)",
                        CurrencyFormatter.formatVND(b.getSpentAmount()),
                        CurrencyFormatter.formatVND(b.getAmountLimit()),
                        b.getProgressPercentage()));
                lblAmount.setFont(AppFont.plain(12));

                JProgressBar bar = new JProgressBar(0, 100);
                int progress = (int) Math.round(b.getProgressPercentage());
                bar.setValue(progress);
                bar.setStringPainted(true);

                if (progress >= 100) {
                    bar.setForeground(new Color(229, 57, 53));
                } else if (progress >= 80) {
                    bar.setForeground(new Color(255, 179, 0));
                } else {
                    bar.setForeground(new Color(67, 160, 71));
                }

                item.add(lblName, BorderLayout.WEST);
                item.add(lblAmount, BorderLayout.EAST);
                item.add(bar, BorderLayout.SOUTH);

                budgetContainer.add(item);
                budgetContainer.add(Box.createVerticalStrut(4));
            }
        }
        budgetContainer.revalidate();
        budgetContainer.repaint();
    }

    private void updateRecentTransactions() {
        tableModel.setRowCount(0);
        List<Transaction> recent = transactionDAO.getRecentTransactions(8);

        for (Transaction tx : recent) {
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

            tableModel.addRow(new Object[]{
                    tx.getTransactionDate(),
                    typeStr,
                    (tx.getCategoryName() != null) ? tx.getCategoryName() : "—",
                    (tx.getWalletName() != null) ? tx.getWalletName() : "—",
                    amountFormatted,
                    (tx.getNote() != null) ? tx.getNote() : ""
            });
        }
    }
}
