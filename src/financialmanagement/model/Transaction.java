package financialmanagement.model;

public class Transaction {
    private int id;
    private int walletId;
    private Integer categoryId; // Can be null for transfer
    private double amount;
    private TransactionType type;
    private Integer toWalletId; // Used when type == TRANSFER
    private String transactionDate; // YYYY-MM-DD
    private String note;
    private String createdAt;

    // Joined fields for display convenience
    private String walletName;
    private String categoryName;
    private String toWalletName;
    private String categoryColor;

    public Transaction() {
    }

    public Transaction(int id, int walletId, Integer categoryId, double amount, 
                       TransactionType type, Integer toWalletId, String transactionDate, 
                       String note, String createdAt) {
        this.id = id;
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.toWalletId = toWalletId;
        this.transactionDate = transactionDate;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Transaction(int walletId, Integer categoryId, double amount, 
                       TransactionType type, Integer toWalletId, String transactionDate, 
                       String note) {
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.toWalletId = toWalletId;
        this.transactionDate = transactionDate;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Integer getToWalletId() {
        return toWalletId;
    }

    public void setToWalletId(Integer toWalletId) {
        this.toWalletId = toWalletId;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getToWalletName() {
        return toWalletName;
    }

    public void setToWalletName(String toWalletName) {
        this.toWalletName = toWalletName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }
}
