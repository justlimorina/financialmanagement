package financialmanagement.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {

    private static final DecimalFormat formatter;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        formatter = new DecimalFormat("#,##0", symbols);
    }

    public static String formatVND(double amount) {
        return formatter.format(amount) + " ₫";
    }

    public static String formatNumber(double amount) {
        return formatter.format(amount);
    }

    public static double parseNumber(String text) {
        if (text == null) return 0;
        String clean = text.replaceAll("[^0-9.-]", "").replace(",", ".");
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
