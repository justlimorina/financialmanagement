package financialmanagement.view;

import financialmanagement.dao.TransactionDAO;
import financialmanagement.model.Transaction;
import financialmanagement.model.TransactionType;
import financialmanagement.util.CurrencyFormatter;
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

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        int currentYear = LocalDate.now().getYear();

        // 1. Header (Title + Year Selector + Export Button)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel lblTitle = new JLabel("Báo Cáo & Phân Tích");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblSub = new JLabel("Bức tranh toàn cảnh về thu chi và tích lũy qua các tháng trong năm");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        controls.add(new JLabel("Năm báo cáo:"));
        spYear = new JSpinner(new SpinnerNumberModel(currentYear, 2020, 2040, 1));
        spYear.setPreferredSize(new Dimension(85, 32));
        spYear.addChangeListener(e -> refreshData());
        controls.add(spYear);

        JButton btnExport = new JButton("📥 Xuất Báo Cáo (CSV)");
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExport.putClientProperty("JButton.buttonType", "roundRect");
        btnExport.setBackground(new Color(46, 125, 50));
        btnExport.setForeground(Color.WHITE);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.addActionListener(e -> exportToCSV());
        controls.add(btnExport);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(controls, BorderLayout.EAST);

        // 2. Metrics Cards (1x4)
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsGrid.setOpaque(false);

        cardYearIncome = new CardPanel("Tổng thu cả năm", "0 ₫", "Cả 12 tháng", new Color(76, 175, 80), "📈");
        cardYearExpense = new CardPanel("Tổng chi cả năm", "0 ₫", "Cả 12 tháng", new Color(244, 67, 54), "📉");
        cardYearNet = new CardPanel("Thặng dư tích lũy", "0 ₫", "Thu trừ Chi", new Color(33, 150, 243), "💰");
        cardSavingsRate = new CardPanel("Tỷ lệ tiết kiệm", "0%", "Trung bình cả năm", new Color(156, 39, 176), "🎯");

        cardsGrid.add(cardYearIncome);
        cardsGrid.add(cardYearExpense);
        cardsGrid.add(cardYearNet);
        cardsGrid.add(cardSavingsRate);

        JPanel northGroup = new JPanel(new BorderLayout(0, 15));
        northGroup.setOpaque(false);
        northGroup.add(headerPanel, BorderLayout.NORTH);
        northGroup.add(cardsGrid, BorderLayout.SOUTH);
        add(northGroup, BorderLayout.NORTH);

        // 3. Middle Charts (Bar Chart + Pie Chart)
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        chartsPanel.setOpaque(false);

        barChartContainer = new JPanel(new BorderLayout());
        barChartContainer.setBorder(BorderFactory.createTitledBorder("So Sánh Thu Nhập & Chi Tiêu 12 Tháng"));
        barChartContainer.setPreferredSize(new Dimension(500, 240));

        pieChartContainer = new JPanel(new BorderLayout());
        pieChartContainer.setBorder(BorderFactory.createTitledBorder("Cơ Cấu Chi Tiêu Theo Danh Mục Cả Năm"));
        pieChartContainer.setPreferredSize(new Dimension(400, 240));

        chartsPanel.add(barChartContainer);
        chartsPanel.add(pieChartContainer);

        // 4. Bottom Table (12-Month Breakdown)
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 6));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Bảng Chi Tiết Dòng Tiền Từng Tháng"));

        String[] columns = {"Tháng", "Tổng Thu Nhập", "Tổng Chi Tiêu", "Thặng Dư (Net)", "Tỷ Lệ Tiết Kiệm"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        monthlyTable = new JTable(tableModel);
        monthlyTable.setRowHeight(28);
        monthlyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        monthlyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        monthlyTable.setFillsViewportHeight(true);

        // Center / right align
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        monthlyTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        monthlyTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        monthlyTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        monthlyTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollTable = new JScrollPane(monthlyTable);
        scrollTable.setPreferredSize(new Dimension(800, 160));
        bottomPanel.add(scrollTable, BorderLayout.CENTER);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setOpaque(false);
        centerWrapper.add(chartsPanel, BorderLayout.CENTER);
        centerWrapper.add(bottomPanel, BorderLayout.SOUTH);

        add(centerWrapper, BorderLayout.CENTER);
    }

    public void refreshData() {
        int year = (Integer) spYear.getValue();

        Map<Integer, double[]> monthlyMap = transactionDAO.getYearlyMonthlySummary(year);
        Map<String, Double> categoryExpenseMap = transactionDAO.getYearlyExpenseByCategory(year);

        double totalYearIncome = 0;
        double totalYearExpense = 0;

        tableModel.setRowCount(0);

        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();

        for (int m = 1; m <= 12; m++) {
            double[] data = monthlyMap.get(m);
            double inc = (data != null) ? data[0] : 0.0;
            double exp = (data != null) ? data[1] : 0.0;
            double net = inc - exp;
            double rate = (inc > 0) ? Math.max(0, (net / inc) * 100.0) : 0.0;

            totalYearIncome += inc;
            totalYearExpense += exp;

            // Thêm vào bar chart
            String monthLabel = "T" + m;
            barDataset.addValue(inc, "Thu nhập", monthLabel);
            barDataset.addValue(exp, "Chi tiêu", monthLabel);

            // Thêm vào bảng
            tableModel.addRow(new Object[]{
                    "Tháng " + m,
                    CurrencyFormatter.formatVND(inc),
                    CurrencyFormatter.formatVND(exp),
                    (net >= 0 ? "+" : "") + CurrencyFormatter.formatVND(net),
                    String.format("%.1f%%", rate)
            });
        }

        // Hàng tổng kết cả năm
        double totalYearNet = totalYearIncome - totalYearExpense;
        double avgSavingsRate = (totalYearIncome > 0) ? Math.max(0, (totalYearNet / totalYearIncome) * 100.0) : 0.0;

        tableModel.addRow(new Object[]{
                "CẢ NĂM " + year,
                CurrencyFormatter.formatVND(totalYearIncome),
                CurrencyFormatter.formatVND(totalYearExpense),
                (totalYearNet >= 0 ? "+" : "") + CurrencyFormatter.formatVND(totalYearNet),
                String.format("%.1f%%", avgSavingsRate)
        });

        // 1. Cập nhật thẻ chỉ số
        cardYearIncome.setValue("+" + CurrencyFormatter.formatVND(totalYearIncome));
        cardYearExpense.setValue("-" + CurrencyFormatter.formatVND(totalYearExpense));
        cardYearNet.setValue((totalYearNet >= 0 ? "+" : "") + CurrencyFormatter.formatVND(totalYearNet));
        cardSavingsRate.setValue(String.format("%.1f%%", avgSavingsRate));

        // 2. Cập nhật Bar Chart
        updateBarChart(barDataset, year);

        // 3. Cập nhật Pie Chart
        updatePieChart(categoryExpenseMap, year);
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
        plot.setRangeGridlinePaint(new Color(200, 200, 200, 80));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(76, 175, 80)); // Xanh lá - Thu nhập
        renderer.setSeriesPaint(1, new Color(244, 67, 54)); // Đỏ - Chi tiêu
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
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
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
            plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
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

        int userSelection = fileChooser.showSaveDialog(parentFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileToSave), StandardCharsets.UTF_8))) {
                // Ghi UTF-8 BOM để Excel hiển thị tiếng Việt chuẩn
                writer.write('\ufeff');

                writer.println("BÁO CÁO TÀI CHÍNH NĂM " + year);
                writer.println("Ngày xuất:," + LocalDate.now());
                writer.println();

                // 1. Bảng tóm tắt theo tháng
                writer.println("1. TỔNG KẾT DÒNG TIỀN THEO THÁNG");
                writer.println("Tháng,Thu Nhập (VNĐ),Chi Tiêu (VNĐ),Thặng Dư Ròng (VNĐ),Tỷ Lệ Tiết Kiệm (%)");

                Map<Integer, double[]> monthlyMap = transactionDAO.getYearlyMonthlySummary(year);
                double totalInc = 0, totalExp = 0;

                for (int m = 1; m <= 12; m++) {
                    double[] d = monthlyMap.get(m);
                    double inc = (d != null) ? d[0] : 0;
                    double exp = (d != null) ? d[1] : 0;
                    double net = inc - exp;
                    double rate = (inc > 0) ? Math.max(0, (net / inc) * 100.0) : 0;
                    totalInc += inc;
                    totalExp += exp;

                    writer.printf("Tháng %d,%.0f,%.0f,%.0f,%.1f%%\n", m, inc, exp, net, rate);
                }
                writer.printf("CẢ NĂM %d,%.0f,%.0f,%.0f,%.1f%%\n", year, totalInc, totalExp, (totalInc - totalExp),
                        (totalInc > 0 ? ((totalInc - totalExp) / totalInc) * 100.0 : 0));
                writer.println();

                // 2. Chi tiết giao dịch trong năm
                writer.println("2. DANH SÁCH TOÀN BỘ GIAO DỊCH TRONG NĂM " + year);
                writer.println("Mã,Ngày,Loại,Danh Mục,Ví Tiền,Số Tiền (VNĐ),Ghi Chú");

                String start = year + "-01-01";
                String end = year + "-12-31";
                List<Transaction> txList = transactionDAO.getTransactionsByFilter(start, end, null, null, null);

                for (Transaction tx : txList) {
                    writer.printf("%d,%s,%s,\"%s\",\"%s\",%.0f,\"%s\"\n",
                            tx.getId(),
                            tx.getTransactionDate(),
                            tx.getType().getDisplayName(),
                            (tx.getCategoryName() != null ? tx.getCategoryName() : ""),
                            (tx.getWalletName() != null ? tx.getWalletName() : ""),
                            tx.getAmount(),
                            (tx.getNote() != null ? tx.getNote().replace("\"", "\"\"") : "")
                    );
                }

                JOptionPane.showMessageDialog(this, "Xuất báo cáo thành công tại:\n" + fileToSave.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
