package financialmanagement.model;

public class Budget {
    private int id;
    private int categoryId;
    private double amountLimit;
    private int month;
    private int year;

    // Joined / calculated fields
    private String categoryName;
    private String categoryColor;
    private double spentAmount;

    public Budget() {
    }

    public Budget(int id, int categoryId, double amountLimit, int month, int year) {
        this.id = id;
        this.categoryId = categoryId;
        this.amountLimit = amountLimit;
        this.month = month;
        this.year = year;
    }

    public Budget(int categoryId, double amountLimit, int month, int year) {
        this.categoryId = categoryId;
        this.amountLimit = amountLimit;
        this.month = month;
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(double amountLimit) {
        this.amountLimit = amountLimit;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return amountLimit - spentAmount;
    }

    public double getProgressPercentage() {
        if (amountLimit <= 0) return 0;
        return Math.min(100.0, (spentAmount / amountLimit) * 100.0);
    }
}
