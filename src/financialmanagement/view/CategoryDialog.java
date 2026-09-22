package financialmanagement.view;

import financialmanagement.dao.CategoryDAO;
import financialmanagement.model.Category;
import financialmanagement.model.TransactionType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryDialog extends JDialog {
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final Category categoryToEdit;
    private final Runnable onSuccessCallback;

    private JTextField txtName;
    private JComboBox<String> cbType;
    private JComboBox<String> cbIcon;
    private String selectedColor = "#2196F3";
    private JPanel colorPreview;

    private static final Map<String, String> ICON_MAP = new LinkedHashMap<>();
    static {
        ICON_MAP.put("🍔 Ăn uống", "food");
        ICON_MAP.put("☕ Cafe & Đồ uống", "coffee");
        ICON_MAP.put("🏠 Nhà cửa & Thuê nhà", "home");
        ICON_MAP.put("💡 Điện, Nước, Net", "bills");
        ICON_MAP.put("🚗 Đi lại & Xăng xe", "car");
        ICON_MAP.put("🛍️ Mua sắm", "shopping");
        ICON_MAP.put("🎮 Giải trí & Xem phim", "entertainment");
        ICON_MAP.put("💊 Y tế & Sức khỏe", "medical");
        ICON_MAP.put("🎓 Giáo dục & Học tập", "education");
        ICON_MAP.put("✈️ Du lịch", "travel");
        ICON_MAP.put("📱 Công nghệ & Thiết bị", "tech");
        ICON_MAP.put("🏋️ Thể thao & Gym", "sport");
        ICON_MAP.put("🐾 Thú cưng", "pet");
        ICON_MAP.put("👗 Thời trang & Mỹ phẩm", "beauty");
        ICON_MAP.put("💵 Tiền lương", "salary");
        ICON_MAP.put("🎁 Tiền thưởng", "bonus");
        ICON_MAP.put("📈 Đầu tư & Sinh lời", "investment");
        ICON_MAP.put("💼 Thu nhập phụ / Freelance", "side_income");
        ICON_MAP.put("🏷️ Khác", "other");
    }

    private static final String[] PRESET_COLORS = {
            "#F44336", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#00BCD4", "#009688",
            "#4CAF50", "#8BC34A", "#FF9800", "#FF5722",
            "#795548", "#607D8B"
    };

    public CategoryDialog(Frame parent, Category categoryToEdit, Runnable onSuccessCallback) {
        super(parent, categoryToEdit == null ? "Thêm Danh Mục Mới" : "Chỉnh Sửa Danh Mục", true);
        this.categoryToEdit = categoryToEdit;
        this.onSuccessCallback = onSuccessCallback;

        if (categoryToEdit != null && categoryToEdit.getColor() != null) {
            this.selectedColor = categoryToEdit.getColor();
        }

        initComponents();
        populateDataIfEditing();

        setSize(460, 420);
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

        int row = 0;

        // 1. Tên danh mục
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Tên danh mục:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtName = new JTextField();
        txtName.putClientProperty("JTextField.placeholderText", "Ví dụ: Ăn uống, Tiền nhà...");
        form.add(txtName, gbc);

        // 2. Loại danh mục (Chi tiêu / Thu nhập)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Loại danh mục:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cbType = new JComboBox<>(new String[]{"Chi tiêu (EXPENSE)", "Thu nhập (INCOME)"});
        form.add(cbType, gbc);

        // 3. Biểu tượng (Icon / Emoji)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Biểu tượng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cbIcon = new JComboBox<>(ICON_MAP.keySet().toArray(new String[0]));
        form.add(cbIcon, gbc);

        // 4. Bảng màu (Color Picker)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        form.add(new JLabel("Màu nhận diện:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;

        JPanel colorChooserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        colorChooserPanel.setOpaque(false);

        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(32, 32));
        colorPreview.setBackground(Color.decode(selectedColor));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));

        JButton btnChooseColor = new JButton("Chọn màu...");
        btnChooseColor.putClientProperty("JButton.buttonType", "roundRect");
        btnChooseColor.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Chọn màu cho danh mục", Color.decode(selectedColor));
            if (chosen != null) {
                selectedColor = String.format("#%02x%02x%02x", chosen.getRed(), chosen.getGreen(), chosen.getBlue());
                colorPreview.setBackground(chosen);
            }
        });

        colorChooserPanel.add(colorPreview);
        colorChooserPanel.add(btnChooseColor);

        // Nhanh một số nút màu preset
        JPanel presetPanel = new JPanel(new GridLayout(2, 7, 4, 4));
        presetPanel.setOpaque(false);
        for (String hex : PRESET_COLORS) {
            JButton btnP = new JButton();
            btnP.setPreferredSize(new Dimension(22, 22));
            btnP.setBackground(Color.decode(hex));
            btnP.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            btnP.setFocusPainted(false);
            btnP.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnP.addActionListener(ev -> {
                selectedColor = hex;
                colorPreview.setBackground(Color.decode(hex));
            });
            presetPanel.add(btnP);
        }

        JPanel colorWrap = new JPanel(new BorderLayout(0, 6));
        colorWrap.setOpaque(false);
        colorWrap.add(colorChooserPanel, BorderLayout.NORTH);
        colorWrap.add(presetPanel, BorderLayout.SOUTH);
        form.add(colorWrap, gbc);

        // Buttons (Lưu / Hủy)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = new JButton("Hủy");
        btnCancel.putClientProperty("JButton.buttonType", "roundRect");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(categoryToEdit == null ? "Tạo Danh Mục" : "Lưu Thay Đổi");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.putClientProperty("JButton.buttonType", "roundRect");
        btnSave.setBackground(new Color(33, 150, 243));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveCategory());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void populateDataIfEditing() {
        if (categoryToEdit == null) return;

        txtName.setText(categoryToEdit.getName());
        cbType.setSelectedIndex(categoryToEdit.getType() == TransactionType.EXPENSE ? 0 : 1);

        String catIcon = categoryToEdit.getIcon();
        for (Map.Entry<String, String> entry : ICON_MAP.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(catIcon) || entry.getKey().contains(catIcon)) {
                cbIcon.setSelectedItem(entry.getKey());
                break;
            }
        }

        if (categoryToEdit.getColor() != null && !categoryToEdit.getColor().isEmpty()) {
            try {
                selectedColor = categoryToEdit.getColor();
                colorPreview.setBackground(Color.decode(selectedColor));
            } catch (Exception ignored) {}
        }
    }

    private void saveCategory() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên danh mục!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }

        TransactionType type = cbType.getSelectedIndex() == 0 ? TransactionType.EXPENSE : TransactionType.INCOME;
        String selectedIconLabel = (String) cbIcon.getSelectedItem();
        String iconKey = ICON_MAP.getOrDefault(selectedIconLabel, "tag");

        boolean success;
        if (categoryToEdit == null) {
            Category newCat = new Category(0, name, type, selectedColor, iconKey);
            success = categoryDAO.addCategory(newCat);
        } else {
            categoryToEdit.setName(name);
            categoryToEdit.setType(type);
            categoryToEdit.setColor(selectedColor);
            categoryToEdit.setIcon(iconKey);
            success = categoryDAO.updateCategory(categoryToEdit);
        }

        if (success) {
            JOptionPane.showMessageDialog(this,
                    categoryToEdit == null ? "Đã thêm danh mục thành công!" : "Đã cập nhật danh mục thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu danh mục. Vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

