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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TransactionPanel extends JPanel {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final WalletDAO walletDAO = new WalletDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private final Frame parentFrame;
    private final Runnable onDataChangedCallback;

    // Filter components
    private JTextField txtFromDate;
    private JTextField txtToDate;
    private JComboBox<Object> cbWalletFilter;
    private JComboBox<Object> cbCategoryFilter;
    private JComboBox<String> cbTypeFilter;

    // Table & Summary
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblCount;
    private JLabel lblTotalIncome;
    private JLabel lblTotalExpense;
    private JLabel lblNetCashflow;

    public TransactionPanel(Frame parentFrame, Runnable onDataChangedCallback) {
        this.parentFrame = parentFrame;
        this.onDataChangedCallback = onDataChangedCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initComponents();
        loadFilterData();
        applyFilter();
    }

    private void initComponents() {
        // 1. Header (Title + Add Button)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel lblTitle = new JLabel("Sổ Giao Dịch");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblSub = new JLabel("Lịch sử chi tiết các khoản thu, chi và chuyển khoản");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        JButton btnAdd = new JButton("+ Thêm Giao Dịch");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setBackground(new Color(33, 150, 243));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> {
            TransactionDialog dialog = new TransactionDialog(parentFrame, () -> {
                applyFilter();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            });
            dialog.setVisible(true);
        });

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(btnAdd, BorderLayout.EAST);

        // 2. Filter Bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        filterPanel.add(new JLabel("Từ ngày:"));
        txtFromDate = new JTextField(8);
        txtFromDate.putClientProperty("JTextField.placeholderText", "YYYY-MM-DD");
        filterPanel.add(txtFromDate);

        filterPanel.add(new JLabel("Đến ngày:"));
        txtToDate = new JTextField(8);
        txtToDate.putClientProperty("JTextField.placeholderText", "YYYY-MM-DD");
        filterPanel.add(txtToDate);

        filterPanel.add(new JLabel("Ví:"));
        cbWalletFilter = new JComboBox<>();
        cbWalletFilter.setPreferredSize(new Dimension(140, 30));
        filterPanel.add(cbWalletFilter);

        filterPanel.add(new JLabel("Danh mục:"));
        cbCategoryFilter = new JComboBox<>();
        cbCategoryFilter.setPreferredSize(new Dimension(140, 30));
        filterPanel.add(cbCategoryFilter);

        filterPanel.add(new JLabel("Loại:"));
        cbTypeFilter = new JComboBox<>(new String[]{"Tất cả loại", "Chi tiêu", "Thu nhập", "Chuyển khoản"});
        filterPanel.add(cbTypeFilter);

        JButton btnFilter = new JButton("🔍 Lọc");
        btnFilter.putClientProperty("JButton.buttonType", "roundRect");
        btnFilter.addActionListener(e -> applyFilter());
        filterPanel.add(btnFilter);

        JButton btnReset = new JButton("🔄 Đặt lại");
        btnReset.putClientProperty("JButton.buttonType", "roundRect");
        btnReset.addActionListener(e -> resetFilter());
        filterPanel.add(btnReset);

        // North Group: Header + Filter
        JPanel northGroup = new JPanel(new BorderLayout(0, 12));
        northGroup.setOpaque(false);
        northGroup.add(headerPanel, BorderLayout.NORTH);
        northGroup.add(filterPanel, BorderLayout.SOUTH);
        add(northGroup, BorderLayout.NORTH);

        // 3. Center Table
        String[] columns = {"Mã", "Ngày", "Loại", "Danh mục", "Từ ví", "Đến ví", "Số tiền", "Ghi chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(60); // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(95); // Date
        table.getColumnModel().getColumn(2).setPreferredWidth(90); // Type
        table.getColumnModel().getColumn(6).setPreferredWidth(130); // Amount

        // Color renderer for Amount
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
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

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedTransaction();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 4. South: Summary & Action Buttons
        JPanel southPanel = new JPanel(new BorderLayout(15, 0));
        southPanel.setOpaque(false);

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        summaryPanel.setOpaque(false);

        lblCount = new JLabel("Tổng: 0 giao dịch");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 13));

        lblTotalIncome = new JLabel("Thu: 0 ₫");
        lblTotalIncome.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalIncome.setForeground(new Color(46, 125, 50));

        lblTotalExpense = new JLabel("Chi: 0 ₫");
        lblTotalExpense.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalExpense.setForeground(new Color(198, 40, 40));

        lblNetCashflow = new JLabel("Dòng tiền: 0 ₫");
        lblNetCashflow.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNetCashflow.setForeground(new Color(33, 150, 243));

        summaryPanel.add(lblCount);
        summaryPanel.add(new JSeparator(SwingConstants.VERTICAL));
        summaryPanel.add(lblTotalIncome);
        summaryPanel.add(lblTotalExpense);
        summaryPanel.add(new JSeparator(SwingConstants.VERTICAL));
        summaryPanel.add(lblNetCashflow);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionsPanel.setOpaque(false);

        JButton btnEdit = new JButton("✏️ Sửa giao dịch");
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnEdit.addActionListener(e -> editSelectedTransaction());

        JButton btnDelete = new JButton("🗑️ Xóa giao dịch");
        btnDelete.putClientProperty("JButton.buttonType", "roundRect");
        btnDelete.setForeground(new Color(211, 47, 47));
        btnDelete.addActionListener(e -> deleteSelectedTransaction());

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.putClientProperty("JButton.buttonType", "roundRect");
        btnRefresh.addActionListener(e -> {
            loadFilterData();
            applyFilter();
        });

        actionsPanel.add(btnEdit);
        actionsPanel.add(btnDelete);
        actionsPanel.add(btnRefresh);

        southPanel.add(summaryPanel, BorderLayout.WEST);
        southPanel.add(actionsPanel, BorderLayout.EAST);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadFilterData() {
        // Load wallets
        cbWalletFilter.removeAllItems();
        cbWalletFilter.addItem("-- Tất cả ví --");
        List<Wallet> wallets = walletDAO.getAllWallets();
        for (Wallet w : wallets) {
            cbWalletFilter.addItem(w);
        }

        // Load categories
        cbCategoryFilter.removeAllItems();
        cbCategoryFilter.addItem("-- Tất cả danh mục --");
        List<Category> categories = categoryDAO.getAllCategories();
        for (Category c : categories) {
            cbCategoryFilter.addItem(c);
        }
    }

    public void applyFilter() {
        String startDate = txtFromDate.getText().trim();
        String endDate = txtToDate.getText().trim();

        Integer walletId = null;
        Object selectedWallet = cbWalletFilter.getSelectedItem();
        if (selectedWallet instanceof Wallet) {
            walletId = ((Wallet) selectedWallet).getId();
        }

        Integer categoryId = null;
        Object selectedCategory = cbCategoryFilter.getSelectedItem();
        if (selectedCategory instanceof Category) {
            categoryId = ((Category) selectedCategory).getId();
        }

        TransactionType type = null;
        int typeIdx = cbTypeFilter.getSelectedIndex();
        if (typeIdx == 1) type = TransactionType.EXPENSE;
        else if (typeIdx == 2) type = TransactionType.INCOME;
        else if (typeIdx == 3) type = TransactionType.TRANSFER;

        List<Transaction> list = transactionDAO.getTransactionsByFilter(startDate, endDate, walletId, categoryId, type);

        tableModel.setRowCount(0);
        double totalInc = 0;
        double totalExp = 0;

        for (Transaction tx : list) {
            String typeStr;
            String amountFormatted;

            if (tx.getType() == TransactionType.INCOME) {
                typeStr = "Thu nhập";
                amountFormatted = "+" + CurrencyFormatter.formatVND(tx.getAmount());
                totalInc += tx.getAmount();
            } else if (tx.getType() == TransactionType.EXPENSE) {
                typeStr = "Chi tiêu";
                amountFormatted = "-" + CurrencyFormatter.formatVND(tx.getAmount());
                totalExp += tx.getAmount();
            } else {
                typeStr = "Chuyển khoản";
                amountFormatted = CurrencyFormatter.formatVND(tx.getAmount());
            }

            tableModel.addRow(new Object[]{
                    tx.getId(),
                    tx.getTransactionDate(),
                    typeStr,
                    (tx.getCategoryName() != null) ? tx.getCategoryName() : "—",
                    (tx.getWalletName() != null) ? tx.getWalletName() : "—",
                    (tx.getToWalletName() != null) ? tx.getToWalletName() : "—",
                    amountFormatted,
                    (tx.getNote() != null) ? tx.getNote() : ""
            });
        }

        // Cập nhật thanh tổng kết
        lblCount.setText(String.format("Tổng: %d giao dịch", list.size()));
        lblTotalIncome.setText("Thu: +" + CurrencyFormatter.formatVND(totalInc));
        lblTotalExpense.setText("Chi: -" + CurrencyFormatter.formatVND(totalExp));

        double net = totalInc - totalExp;
        lblNetCashflow.setText(String.format("Dòng tiền: %s%s", (net >= 0 ? "+" : ""), CurrencyFormatter.formatVND(net)));
        lblNetCashflow.setForeground(net >= 0 ? new Color(46, 125, 50) : new Color(198, 40, 40));
    }

    private void resetFilter() {
        txtFromDate.setText("");
        txtToDate.setText("");
        cbWalletFilter.setSelectedIndex(0);
        cbCategoryFilter.setSelectedIndex(0);
        cbTypeFilter.setSelectedIndex(0);
        applyFilter();
    }

    private void editSelectedTransaction() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 giao dịch trong bảng để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int txId = (int) tableModel.getValueAt(selectedRow, 0);
        Transaction tx = transactionDAO.getTransactionById(txId);
        if (tx != null) {
            TransactionDialog dialog = new TransactionDialog(parentFrame, tx, () -> {
                applyFilter();
                if (onDataChangedCallback != null) {
                    onDataChangedCallback.run();
                }
            });
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin giao dịch!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedTransaction() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 giao dịch trong bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int txId = (int) tableModel.getValueAt(selectedRow, 0);
        String date = (String) tableModel.getValueAt(selectedRow, 1);
        String amount = (String) tableModel.getValueAt(selectedRow, 6);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Bạn có chắc chắn muốn xóa giao dịch ngày %s (%s)?\nSố dư ví liên quan sẽ được tự động hoàn lại.", date, amount),
                "Xác nhận xóa giao dịch",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = transactionDAO.deleteTransaction(txId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa giao dịch và hoàn lại số dư ví!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                applyFilter();
                if (onDataChangedCallback != null) {
                    onDataChangedCallback.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa giao dịch. Vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
