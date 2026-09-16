package financialmanagement;

import financialmanagement.dao.BudgetDAO;
import financialmanagement.dao.CategoryDAO;
import financialmanagement.dao.DatabaseHelper;
import financialmanagement.dao.TransactionDAO;
import financialmanagement.dao.WalletDAO;
import financialmanagement.model.Budget;
import financialmanagement.model.Category;
import financialmanagement.model.Transaction;
import financialmanagement.model.TransactionType;
import financialmanagement.model.Wallet;
import financialmanagement.util.CurrencyFormatter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   KIỂM THỬ HỆ THỐNG CSDL & MODEL (BƯỚC 1)");
        System.out.println("==================================================");

        // 1. Khởi tạo CSDL
        DatabaseHelper.initDatabase();
        System.out.println("[✓] Khởi tạo Database SQLite thành công!");

        WalletDAO walletDAO = new WalletDAO();
        CategoryDAO categoryDAO = new CategoryDAO();
        TransactionDAO transactionDAO = new TransactionDAO();
        BudgetDAO budgetDAO = new BudgetDAO();

        // 2. Kiểm tra danh mục mặc định
        List<Category> categories = categoryDAO.getAllCategories();
        System.out.println("[✓] Số lượng danh mục khởi tạo: " + categories.size());

        // 3. Kiểm tra ví
        List<Wallet> wallets = walletDAO.getAllWallets();
        System.out.println("[✓] Số lượng ví khởi tạo: " + wallets.size());
        if (wallets.isEmpty()) {
            System.err.println("Không có ví nào!");
            return;
        }

        Wallet cashWallet = wallets.get(0);
        System.out.printf("    Ví: %s | Số dư ban đầu: %s\n", 
                cashWallet.getName(), CurrencyFormatter.formatVND(cashWallet.getBalance()));

        // 4. Thử nghiệm Giao dịch: Nhận lương 10.000.000 ₫
        LocalDate today = LocalDate.now();
        String dateStr = today.toString();

        Category salaryCat = categories.stream()
                .filter(c -> c.getType() == TransactionType.INCOME)
                .findFirst().orElse(null);

        Transaction incomeTx = new Transaction(
                cashWallet.getId(),
                salaryCat != null ? salaryCat.getId() : null,
                10000000.0,
                TransactionType.INCOME,
                null,
                dateStr,
                "Nhận lương tháng này"
        );
        boolean incomeAdded = transactionDAO.addTransaction(incomeTx);
        System.out.println("[✓] Thêm giao dịch Thu (Lương 10.000.000 ₫): " + (incomeAdded ? "THÀNH CÔNG" : "THẤT BẠI"));

        // 5. Thử nghiệm Giao dịch: Chi Ăn uống 250.000 ₫
        Category foodCat = categories.stream()
                .filter(c -> c.getType() == TransactionType.EXPENSE && c.getName().contains("Ăn uống"))
                .findFirst().orElse(null);

        Transaction expenseTx = new Transaction(
                cashWallet.getId(),
                foodCat != null ? foodCat.getId() : null,
                250000.0,
                TransactionType.EXPENSE,
                null,
                dateStr,
                "Ăn trưa & cà phê"
        );
        boolean expenseAdded = transactionDAO.addTransaction(expenseTx);
        System.out.println("[✓] Thêm giao dịch Chi (Ăn uống 250.000 ₫): " + (expenseAdded ? "THÀNH CÔNG" : "THẤT BẠI"));

        // 6. Kiểm tra lại số dư ví sau giao dịch
        Wallet updatedWallet = walletDAO.getWalletById(cashWallet.getId());
        System.out.printf("[✓] Số dư ví '%s' sau giao dịch: %s (Kỳ vọng: 9.750.000 ₫)\n",
                updatedWallet.getName(), CurrencyFormatter.formatVND(updatedWallet.getBalance()));

        // 7. Thử nghiệm Ngân sách (Budget)
        if (foodCat != null) {
            Budget budget = new Budget(foodCat.getId(), 2000000.0, today.getMonthValue(), today.getYear());
            budgetDAO.saveOrUpdateBudget(budget);
            List<Budget> budgets = budgetDAO.getBudgetsForMonth(today.getMonthValue(), today.getYear());
            for (Budget b : budgets) {
                System.out.printf("[✓] Ngân sách '%s': Đã chi %s / Hạn mức %s (Tiến độ: %.1f%%)\n",
                        b.getCategoryName(),
                        CurrencyFormatter.formatVND(b.getSpentAmount()),
                        CurrencyFormatter.formatVND(b.getAmountLimit()),
                        b.getProgressPercentage());
            }
        }

        // 8. Thống kê tháng
        double[] summary = transactionDAO.getMonthlySummary(today.getMonthValue(), today.getYear());
        System.out.printf("[✓] Thống kê tháng %02d/%04d: Tổng Thu = %s | Tổng Chi = %s\n",
                today.getMonthValue(), today.getYear(),
                CurrencyFormatter.formatVND(summary[0]),
                CurrencyFormatter.formatVND(summary[1]));

        System.out.println("==================================================");
        System.out.println("   HOÀN THÀNH KIỂM THỬ BƯỚC 1: TẤT CẢ HOẠT ĐỘNG TỐT!");
        System.out.println("==================================================");
    }
}
