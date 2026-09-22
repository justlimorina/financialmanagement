package financialmanagement.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;

public class IconHelper {
    private static Font iconFont;

    // Navigation Icons
    public static final String DASHBOARD = "\ue871";     // dashboard
    public static final String TRANSACTIONS = "\uef63";  // payments
    public static final String WALLETS = "\ue850";       // account_balance_wallet
    public static final String ACCOUNT_BALANCE_WALLET = "\ue850";
    public static final String CATEGORIES = "\ue574";    // category
    public static final String BUDGETS = "\ue8f9";       // savings
    public static final String SAVINGS = "\ue8f9";
    public static final String REPORTS = "\ue85c";       // assessment
    public static final String DARK_MODE = "\ue51c";     // dark_mode
    public static final String LIGHT_MODE = "\ue518";    // light_mode

    // Action Icons
    public static final String ADD = "\ue145";           // add
    public static final String EDIT = "\ue3c9";          // edit
    public static final String DELETE = "\ue872";        // delete
    public static final String REFRESH = "\ue5d5";       // refresh
    public static final String FILTER = "\uef4f";        // filter_alt
    public static final String DOWNLOAD = "\ue2c4";      // file_download
    public static final String SWAP = "\ue8d4";          // swap_horiz
    public static final String SEARCH = "\ue8b6";        // search
    public static final String CLOSE = "\ue5cd";         // close
    public static final String CHECK = "\ue5ca";         // check

    // Metrics & Category Icons
    public static final String TRENDING_UP = "\ue8e5";   // trending_up
    public static final String TRENDING_DOWN = "\ue8e3"; // trending_down
    public static final String ACCOUNT_BALANCE = "\ue84f"; // account_balance
    public static final String PHONE_ANDROID = "\ue32c"; // phone_android
    public static final String CREDIT_CARD = "\ue870";   // credit_card
    public static final String ATTACH_MONEY = "\ue227";  // attach_money

    // Categories
    public static final String FOOD = "\ue57a";          // fastfood
    public static final String COFFEE = "\ue541";        // local_cafe
    public static final String HOME = "\ue88a";          // home
    public static final String BILLS = "\ue0f0";         // lightbulb
    public static final String CAR = "\ue531";           // directions_car
    public static final String SHOPPING = "\uea64";      // shopping_bag
    public static final String ENTERTAINMENT = "\uea28"; // sports_esports
    public static final String MEDICAL = "\ue548";       // local_hospital
    public static final String EDUCATION = "\ue80c";     // school
    public static final String TRAVEL = "\ue539";        // flight
    public static final String TECH = "\ue30a";          // computer
    public static final String SPORT = "\ueb43";         // fitness_center
    public static final String PET = "\ue91d";           // pets
    public static final String BEAUTY = "\uf19e";        // checkroom
    public static final String SALARY = "\ue227";        // attach_money
    public static final String BONUS = "\ue8f6";         // card_giftcard
    public static final String INVESTMENT = "\uf092";    // insights
    public static final String WORK = "\ueb3f";          // business_center
    public static final String OTHER = "\ue892";         // label

    static {
        try {
            File fontFile = new File("src/resources/fonts/MaterialIcons-Regular.ttf");
            if (fontFile.exists()) {
                iconFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            } else {
                InputStream is = IconHelper.class.getResourceAsStream("/resources/fonts/MaterialIcons-Regular.ttf");
                if (is != null) {
                    iconFont = Font.createFont(Font.TRUETYPE_FONT, is);
                } else {
                    iconFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
                }
            }
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(iconFont);
        } catch (Exception e) {
            System.err.println("Không thể load Material Icons font: " + e.getMessage());
            iconFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        }
    }

    public static Font getIconFont(float size) {
        return iconFont.deriveFont(size);
    }

    public static JLabel createIcon(String glyph, float size, Color color) {
        JLabel lbl = new JLabel(glyph);
        lbl.setFont(getIconFont(size));
        if (color != null) {
            lbl.setForeground(color);
        }
        return lbl;
    }

    public static JLabel createIcon(String glyph, float size) {
        return createIcon(glyph, size, null);
    }
}
