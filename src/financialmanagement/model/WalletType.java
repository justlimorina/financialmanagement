package financialmanagement.model;

public enum WalletType {
    CASH("Tiền mặt"),
    BANK("Tài khoản ngân hàng"),
    E_WALLET("Ví điện tử"),
    CREDIT("Thẻ tín dụng");

    private final String displayName;

    WalletType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static WalletType fromString(String text) {
        for (WalletType b : WalletType.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return CASH;
    }
}
