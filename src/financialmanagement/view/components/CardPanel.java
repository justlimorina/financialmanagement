package financialmanagement.view.components;

import financialmanagement.util.AppFont;
import financialmanagement.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CardPanel extends JPanel {
    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;

    public CardPanel(String title, String initialValue, String subtitle, Color accentColor, String iconGlyph) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 75), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Header panel: Icon + Title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerPanel.setOpaque(false);

        JLabel lblIcon = IconHelper.createIcon(iconGlyph, 22, accentColor);

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(AppFont.bold(12));
        lblTitle.setForeground(accentColor);

        headerPanel.add(lblIcon);
        headerPanel.add(lblTitle);

        // Center: Value
        lblValue = new JLabel(initialValue);
        lblValue.setFont(AppFont.bold(25));

        // Footer: Subtitle
        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(AppFont.plain(13));
        lblSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        add(headerPanel, BorderLayout.NORTH);
        add(lblValue, BorderLayout.CENTER);
        add(lblSubtitle, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }
}
