package financialmanagement.view;

import financialmanagement.dao.TransactionDAO;
import financialmanagement.model.Transaction;
import financialmanagement.model.TransactionType;
import financialmanagement.util.AppFont;
import financialmanagement.util.CurrencyFormatter;
import financialmanagement.util.IconHelper;
import financialmanagement.view.components.CardPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportPanel extends JPanel {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final Frame parentFrame;

    private JSpinner spYear;
    private CardPanel cardYearIncome;
    private CardPanel cardYearExpense;
    private CardPanel cardYearNet;
    private CardPanel cardSavingsRate;

    private JPanel barChartContainer;
    private JPanel pieChartContainer;
    private JTable monthlyTable;
    private DefaultTableModel tableModel;

    public ReportPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());

        initComponents();
        refreshData();
    }

    private void initComponents() {
        int currentYear = LocalDate.now().getYear();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Header (Title + Year Selector + Export Button)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        JLabel lblTitle = new JLabel("Báo Cáo & Phân Tích");
        lblTitle.setFont(AppFont.bold(21));

        JLabel lblSub = new JLabel("Bức tranh toàn cảnh về thu chi và tích lũy qua các tháng trong năm");
        lblSub.setFont(AppFont.plain(12));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        controls.add(new JLabel("Năm:"));
        spYear = new JSpinner(new SpinnerNumberModel(currentYear, 2020, 2040, 1));
        spYear.setPreferredSize(new Dimension(85, 32));
        spYear.addChangeListener(e -> refreshData());
        controls.add(spYear);

        JButton btnExport = new JButton("Xuất CSV");
        btnExport.setFont(AppFont.bold(13));
        btnExport.putClientProperty("JButton.buttonType", "roundRect");
        btnExport.setBackground(new Color(46, 125, 50));
        btnExport.setForeground(Color.WHITE);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.addActionListener(e -> exportToCSV());
        controls.add(btnExport);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(controls, BorderLayout.EAST);
        content.add(headerPanel);
        content.add(Box.createVerticalStrut(14));

        // 2. Metrics Cards (1x4)
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        cardsGrid.setOpaque(false);

        cardYearIncome = new CardPanel("Tổng thu cả năm", "0 ₫", "Cả 12 tháng", new Color(76, 175, 80), IconHelper.TRENDING_UP);
        cardYearExpense = new CardPanel("Tổng chi cả năm", "0 ₫", "Cả 12 tháng", new Color(244, 67, 54), IconHelper.TRENDING_DOWN);
        cardYearNet = new CardPanel("Thặng dư tích lũy", "0 ₫", "Thu trừ Chi", new Color(33, 150, 243), IconHelper.ATTACH_MONEY);
        cardSavingsRate = new CardPanel("Tỷ lệ tiết kiệm", "0%", "Trung bình cả năm", new Color(156, 39, 176), IconHelper.SAVINGS);

        cardsGrid.add(cardYearIncome);
        cardsGrid.add(cardYearExpense);
        cardsGrid.add(cardYearNet);
        cardsGrid.add(cardSavingsRate);
        content.add(cardsGrid);
        content.add(Box.createVerticalStrut(14));

        // 3. Middle Charts (Bar Chart + Pie Chart)
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        chartsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        chartsPanel.setPreferredSize(new Dimension(800, 250));
        chartsPanel.setOpaque(false);

        JPanel barCard = createSectionCard("So Sánh Thu Nhập & Chi Tiêu 12 Tháng");
        barChartContainer = new JPanel(new BorderLayout());
        barChartContainer.setOpaque(false);
        barCard.add(barChartContainer, BorderLayout.CENTER);

        JPanel pieCard = createSectionCard("Cơ Cấu Chi Tiêu Theo Danh Mục Cả Năm");
        pieChartContainer = new JPanel(new BorderLayout());
        pieChartContainer.setOpaque(false);
        pieCard.add(pieChartContainer, BorderLayout.CENTER);

        chartsPanel.add(barCard);
        chartsPanel.add(pieCard);
        content.add(chartsPanel);
        content.add(Box.createVerticalStrut(14));

        // 4. Bottom Table (12-Month Breakdown)
        JPanel tableCard = createSectionCard("Bảng Chi Tiết Dòng Tiền Từng Tháng");
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        tableCard.setPreferredSize(new Dimension(800, 220));

        String[] columns = {"Tháng", "Tổng Thu Nhập", "Tổng Chi Tiêu", "Thặng Dư (Net)", "Tỷ Lệ Tiết Kiệm"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        monthlyTable = new JTable(tableModel);
        monthlyTable.setRowHeight(30);
        monthlyTable.setFont(AppFont.plain(13));
        monthlyTable.getTableHeader().setFont(AppFont.bold(13));
        monthlyTable.setFillsViewportHeight(true);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        monthlyTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        monthlyTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        monthlyTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        monthlyTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollTable = new JScrollPane(monthlyTable);
        scrollTable.setBorder(null);
        tableCard.add(scrollTable, BorderLayout.CENTER);

        content.add(tableCard);

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
        lblTitle.setFont(AppFont.bold(14));
        card.add(lblTitle, BorderLayout.NORTH);
        return card;
    }

    public void refreshData() {
        int year = (Integer) spYear.getValue();

        double totalIncome = 0;
        double totalExpense = 0;

        tableModel.setRowCount(0);
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();

        for (int m = 1; m <= 12; m++) {
            double[] summary = transactionDAO.getMonthlySummary(m, year);
            double inc = summary[0];
            double exp = summary[1];
            double net = inc - exp;
            double savingsRate = (inc > 0) ? (Math.max(0, net) / inc) * 100 : 0;

            totalIncome += inc;
            totalExpense += exp;

            String monthStr = "Tháng " + m;
            barDataset.addValue(inc, "Thu nhập", monthStr);
            barDataset.addValue(exp, "Chi tiêu", monthStr);

            tableModel.addRow(new Object[]{
                    monthStr,
                    "+" + CurrencyFormatter.formatVND(inc),
                    "-" + CurrencyFormatter.formatVND(exp),
                    (net >= 0 ? "+" : "") + CurrencyFormatter.formatVND(net),
                    String.format("%.1f%%", savingsRate)
            });
        }

        // Cập nhật thẻ chỉ số
        cardYearIncome.setValue("+" + CurrencyFormatter.formatVND(totalIncome));
        cardYearExpense.setValue("-" + CurrencyFormatter.formatVND(totalExpense));

        double netYear = totalIncome - totalExpense;
        cardYearNet.setValue((netYear >= 0 ? "+" : "") + CurrencyFormatter.formatVND(netYear));

        double avgSavingsRate = (totalIncome > 0) ? (Math.max(0, netYear) / totalIncome) * 100 : 0;
        cardSavingsRate.setValue(String.format("%.1f%%", avgSavingsRate));

        // Cập nhật Bar Chart
        updateBarChart(barDataset, year);

        // Cập nhật Pie Chart
        Map<String, Double> categoryMap = transactionDAO.getYearlyExpenseByCategory(year);
        updatePieChart(categoryMap, year);
    }

    private void updateBarChart(DefaultCategoryDataset dataset, int year) {
        barChartContainer.removeAll();

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Tháng",
                "Số tiền (VNĐ)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(null);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(null);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(76, 175, 80));
        renderer.setSeriesPaint(1, new Color(244, 67, 54));
        renderer.setItemMargin(0.05);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        barChartContainer.add(chartPanel, BorderLayout.CENTER);

        barChartContainer.revalidate();
        barChartContainer.repaint();
    }

    private void updatePieChart(Map<String, Double> categoryMap, int year) {
        pieChartContainer.removeAll();

        if (categoryMap.isEmpty()) {
            JLabel lblEmpty = new JLabel("Chưa có dữ liệu chi tiêu trong năm " + year, SwingConstants.CENTER);
            lblEmpty.setFont(AppFont.italic(13));
            lblEmpty.setForeground(UIManager.getColor("Label.disabledForeground"));
            pieChartContainer.add(lblEmpty, BorderLayout.CENTER);
        } else {
            DefaultPieDataset dataset = new DefaultPieDataset();
            for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
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
            pieChartContainer.add(chartPanel, BorderLayout.CENTER);
        }

        pieChartContainer.revalidate();
        pieChartContainer.repaint();
    }

    private void exportToCSV() {
        int year = (Integer) spYear.getValue();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu Báo Cáo Tài Chính Năm " + year);
        fileChooser.setSelectedFile(new File("BaoCaoTaiChinh_" + year + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try (FileOutputStream fos = new FileOutputStream(fileToSave);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 PrintWriter pw = new PrintWriter(osw)) {

                // UTF-8 BOM để Excel hiển thị tiếng Việt không bị lỗi font
                fos.write(0xEF);
                fos.write(0xBB);
                fos.write(0xBF);

                pw.println("BÁO CÁO TÀI CHÍNH NĂM " + year);
                pw.println("Xuất ngày: " + LocalDate.now());
                pw.println();

                pw.println("Tháng,Thu Nhập,Chi Tiêu,Thặng Dư,Tỷ Lệ Tiết Kiệm");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            tableModel.getValueAt(i, 0),
                            tableModel.getValueAt(i, 1),
                            tableModel.getValueAt(i, 2),
                            tableModel.getValueAt(i, 3),
                            tableModel.getValueAt(i, 4)
                    );
                }

                pw.println();
                pw.println("CHI TIẾT CÁC GIAO DỊCH TRONG NĂM " + year);
                pw.println("Mã,Ngày,Loại,Danh mục,Từ ví,Đến ví,Số tiền,Ghi chú");

                List<Transaction> list = transactionDAO.getTransactionsByFilter(year + "-01-01", year + "-12-31", null, null, null);
                for (Transaction tx : list) {
                    pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.0f,\"%s\"\n",
                            tx.getId(),
                            tx.getTransactionDate(),
                            tx.getType() == TransactionType.INCOME ? "Thu nhập" : (tx.getType() == TransactionType.EXPENSE ? "Chi tiêu" : "Chuyển khoản"),
                            tx.getCategoryName() != null ? tx.getCategoryName() : "",
                            tx.getWalletName() != null ? tx.getWalletName() : "",
                            tx.getToWalletName() != null ? tx.getToWalletName() : "",
                            tx.getAmount(),
                            tx.getNote() != null ? tx.getNote().replace("\"", "\"\"") : ""
                    );
                }

                JOptionPane.showMessageDialog(this, "Xuất báo cáo CSV thành công!\n" + fileToSave.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
