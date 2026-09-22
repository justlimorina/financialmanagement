package financialmanagement.view;

import financialmanagement.dao.CategoryDAO;
import financialmanagement.model.Category;
import financialmanagement.model.TransactionType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryPanel extends JPanel {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final Frame parentFrame;
    private final Runnable onDataChangedCallback;

    private JTabbedPane tabbedPane;
    private JTable expenseTable;
    private DefaultTableModel expenseTableModel;
    private JTable incomeTable;
    private DefaultTableModel incomeTableModel;

    private static final Map<String, String> ICON_TO_EMOJI = new HashMap<>();
    static {
        ICON_TO_EMOJI.put("food", "🍔");
        ICON_TO_EMOJI.put("coffee", "☕");
        ICON_TO_EMOJI.put("home", "🏠");
        ICON_TO_EMOJI.put("bills", "💡");
        ICON_TO_EMOJI.put("car", "🚗");
        ICON_TO_EMOJI.put("shopping", "🛍️");
        ICON_TO_EMOJI.put("entertainment", "🎮");
        ICON_TO_EMOJI.put("medical", "💊");
        ICON_TO_EMOJI.put("education", "🎓");
        ICON_TO_EMOJI.put("travel", "✈️");
        ICON_TO_EMOJI.put("tech", "📱");
        ICON_TO_EMOJI.put("sport", "🏋️");
        ICON_TO_EMOJI.put("pet", "🐾");
        ICON_TO_EMOJI.put("beauty", "👗");
        ICON_TO_EMOJI.put("salary", "💵");
        ICON_TO_EMOJI.put("bonus", "🎁");
        ICON_TO_EMOJI.put("investment", "📈");
        ICON_TO_EMOJI.put("side_income", "💼");
        ICON_TO_EMOJI.put("other", "🏷️");
    }

    public CategoryPanel(Frame parentFrame, Runnable onDataChangedCallback) {
        this.parentFrame = parentFrame;
        this.onDataChangedCallback = onDataChangedCallback;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel lblTitle = new JLabel("Danh Mục Thu & Chi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblSub = new JLabel("Phân loại các khoản chi tiêu và nguồn thu nhập cá nhân");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        JButton btnAdd = new JButton("+ Thêm Danh Mục");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setBackground(new Color(33, 150, 243));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> {
            CategoryDialog dialog = new CategoryDialog(parentFrame, null, () -> {
                refreshData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            });
            dialog.setVisible(true);
        });

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(btnAdd, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Tabs: Chi tiêu / Thu nhập
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        expenseTableModel = createTableModel();
        expenseTable = createTable(expenseTableModel, TransactionType.EXPENSE);
        JScrollPane scrollExpense = new JScrollPane(expenseTable);

        incomeTableModel = createTableModel();
        incomeTable = createTable(incomeTableModel, TransactionType.INCOME);
        JScrollPane scrollIncome = new JScrollPane(incomeTable);

        tabbedPane.addTab("💸 Chi Tiêu (Expense)", scrollExpense);
        tabbedPane.addTab("💰 Thu Nhập (Income)", scrollIncome);

        add(tabbedPane, BorderLayout.CENTER);

        // 3. Actions Panel (Bottom)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomPanel.setOpaque(false);

        JButton btnEdit = new JButton("✏️ Sửa danh mục");
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnEdit.addActionListener(e -> editSelectedCategory());

        JButton btnDelete = new JButton("🗑️ Xóa danh mục");
        btnDelete.putClientProperty("JButton.buttonType", "roundRect");
        btnDelete.setForeground(new Color(211, 47, 47));
        btnDelete.addActionListener(e -> deleteSelectedCategory());

        JButton btnRefresh = new JButton("🔄 Làm mới");
        btnRefresh.putClientProperty("JButton.buttonType", "roundRect");
        btnRefresh.addActionListener(e -> refreshData());

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnRefresh);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private DefaultTableModel createTableModel() {
        String[] columns = {"Mã", "Biểu tượng", "Tên danh mục", "Màu sắc", "Số mục liên kết"};
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createTable(DefaultTableModel model, TransactionType type) {
        JTable t = new JTable(model);
        t.setRowHeight(36);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);

        t.getColumnModel().getColumn(0).setMaxWidth(60);
        t.getColumnModel().getColumn(1).setPreferredWidth(80);
        t.getColumnModel().getColumn(2).setPreferredWidth(250);
        t.getColumnModel().getColumn(3).setPreferredWidth(120);
        t.getColumnModel().getColumn(4).setPreferredWidth(130);

        // Center emoji column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        t.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // Color renderer
        t.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
                p.setOpaque(true);
                if (isSelected) {
                    p.setBackground(table.getSelectionBackground());
                } else {
                    p.setBackground(table.getBackground());
                }

                String hex = value != null ? value.toString() : "#999999";
                JPanel dot = new JPanel();
                dot.setPreferredSize(new Dimension(16, 16));
                try {
                    dot.setBackground(Color.decode(hex));
                } catch (Exception ignored) {}
                dot.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));

                JLabel lblHex = new JLabel(hex);
                lblHex.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                if (isSelected) lblHex.setForeground(table.getSelectionForeground());

                p.add(dot);
                p.add(lblHex);
                return p;
            }
        });

        // Double click to edit
        t.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedCategory();
                }
            }
        });

        return t;
    }

    public void refreshData() {
        loadTableData(expenseTableModel, TransactionType.EXPENSE);
        loadTableData(incomeTableModel, TransactionType.INCOME);
    }

    private void loadTableData(DefaultTableModel model, TransactionType type) {
        model.setRowCount(0);
        List<Category> list = categoryDAO.getCategoriesByType(type);

        for (Category c : list) {
            String emoji = ICON_TO_EMOJI.getOrDefault(c.getIcon(), "🏷️");
            int usage = categoryDAO.countUsage(c.getId());
            model.addRow(new Object[]{
                    c.getId(),
                    emoji,
                    c.getName(),
                    c.getColor() != null ? c.getColor() : "#2196F3",
                    usage > 0 ? usage + " mục" : "Chưa có"
            });
        }
    }

    private JTable getActiveTable() {
        return tabbedPane.getSelectedIndex() == 0 ? expenseTable : incomeTable;
    }

    private void editSelectedCategory() {
        JTable table = getActiveTable();
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 danh mục để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int catId = (int) table.getValueAt(row, 0);
        Category category = categoryDAO.getCategoryById(catId);
        if (category != null) {
            CategoryDialog dialog = new CategoryDialog(parentFrame, category, () -> {
                refreshData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            });
            dialog.setVisible(true);
        }
    }

    private void deleteSelectedCategory() {
        JTable table = getActiveTable();
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 danh mục để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int catId = (int) table.getValueAt(row, 0);
        String catName = (String) table.getValueAt(row, 2);
        int usage = categoryDAO.countUsage(catId);

        String message = String.format("Bạn có chắc chắn muốn xóa danh mục \"%s\"?", catName);
        if (usage > 0) {
            message += String.format("\n\nLƯU Ý: Đang có %d giao dịch hoặc ngân sách liên kết với danh mục này.\nCác giao dịch sẽ được chuyển thành \"Chưa phân loại\".", usage);
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                message,
                "Xác nhận xóa danh mục",
                JOptionPane.YES_NO_OPTION,
                usage > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = categoryDAO.deleteCategory(catId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa danh mục thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
                if (onDataChangedCallback != null) {
                    onDataChangedCallback.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa danh mục. Vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

