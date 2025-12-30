package com.example.moneymitra.model;

public class InvestmentItem {

    private String name;
    private int amount;
    private int color;
    private boolean isSelected;

    // ===== MAIN CONSTRUCTOR (USED EVERYWHERE) =====
    public InvestmentItem(String name, int amount, int color) {
        this.name = name;
        this.amount = amount;
        this.color = color;
        this.isSelected = false;
    }

    // ===== OPTIONAL CONSTRUCTOR (SAFE) =====
    // Agar kahin sirf name + amount pass ho
    public InvestmentItem(String name, int amount) {
        this.name = name;
        this.amount = amount;
        this.color = 0; // later set if needed
        this.isSelected = false;
    }

    // ===== GETTERS =====
    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public int getColor() {
        return color;
    }

    public boolean isSelected() {
        return isSelected;
    }

    // ===== SETTERS (EDIT FEATURE KE LIYE REQUIRED) =====
    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
