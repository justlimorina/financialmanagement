package financialmanagement.model;

import java.time.LocalDateTime;

public class Wallet {
    private int id;
    private String name;
    private double balance;
    private WalletType type;
    private String createdAt;

    public Wallet() {
    }

    public Wallet(int id, String name, double balance, WalletType type, String createdAt) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.type = type;
        this.createdAt = createdAt;
    }

    public Wallet(String name, double balance, WalletType type) {
        this.name = name;
        this.balance = balance;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public WalletType getType() {
        return type;
    }

    public void setType(WalletType type) {
        this.type = type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
}
