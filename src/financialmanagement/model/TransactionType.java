package financialmanagement.model;

public enum TransactionType {
    EXPENSE("Chi tiêu"),
    INCOME("Thu nhập"),
    TRANSFER("Chuyển khoản");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TransactionType fromString(String text) {
        for (TransactionType b : TransactionType.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return EXPENSE;
    }
}
