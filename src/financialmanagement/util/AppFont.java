package financialmanagement.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

public class AppFont {
    private static final String FONT_FAMILY;

    static {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] families = ge.getAvailableFontFamilyNames();
        boolean hasRoboto = Arrays.stream(families).anyMatch(f -> f.equalsIgnoreCase("Roboto"));
        FONT_FAMILY = hasRoboto ? "Roboto" : Font.SANS_SERIF;
    }

    public static Font get(int style, float size) {
        return new Font(FONT_FAMILY, style, (int) size);
    }

    public static Font plain(float size) {
        return get(Font.PLAIN, size);
    }

    public static Font bold(float size) {
        return get(Font.BOLD, size);
    }

    public static Font italic(float size) {
        return get(Font.ITALIC, size);
    }

    public static String getFamily() {
        return FONT_FAMILY;
    }
}

