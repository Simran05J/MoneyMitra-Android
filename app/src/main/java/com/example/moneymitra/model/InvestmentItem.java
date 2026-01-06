package com.example.moneymitra.model;

import com.google.firebase.firestore.Exclude;

public class InvestmentItem {

    private String id;
    private String name;
    private long amount;
    private int color;
    private long createdAt;
    private String goal; // 🔥 NEW

    @Exclude
    private boolean isSelected;

    // 🔴 Required empty constructor for Firestore
    public InvestmentItem() {}

    // 🔥 Main constructor
    public InvestmentItem(String name, long amount, int color, long createdAt) {
        this.name = name;
        this.amount = amount;
        this.color = color;
        this.createdAt = createdAt;
        this.goal = ""; // default
    }

    // ================= GETTERS =================
    public String getId() { return id; }
    public String getName() { return name; }
    public long getAmount() { return amount; }
    public int getColor() { return color; }
    public long getCreatedAt() { return createdAt; }
    public String getGoal() { return goal; }

    // ================= SETTERS =================
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAmount(long amount) { this.amount = amount; }
    public void setColor(int color) { this.color = color; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setGoal(String goal) { this.goal = goal; }

    @Exclude
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
