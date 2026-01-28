package com.example.moneymitra.model;

import java.io.Serializable;

public class ExpenseItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private double amount;
    private String mainCategory;
    private String subCategory;
    private long timestamp;
    private int month;
    private int year;

    // 🔥 REQUIRED EMPTY CONSTRUCTOR FOR FIRESTORE
    public ExpenseItem() {}

    // 🔹 Constructor WITHOUT id (when adding new expense)
    public ExpenseItem(String title, double amount, String mainCategory,
                       String subCategory, long timestamp, int month, int year) {
        this.title = title;
        this.amount = amount;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.timestamp = timestamp;
        this.month = month;
        this.year = year;
    }

    // 🔹 Constructor WITH id (when reading from Firestore)
    public ExpenseItem(String id, String title, double amount, String mainCategory,
                       String subCategory, long timestamp, int month, int year) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.timestamp = timestamp;
        this.month = month;
        this.year = year;
    }

    // ===== GETTERS =====
    public String getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getMainCategory() { return mainCategory; }
    public String getSubCategory() { return subCategory; }
    public long getTimestamp() { return timestamp; }
    public int getMonth() { return month; }
    public int getYear() { return year; }

    // ===== SETTERS (needed for edit flow) =====
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setMainCategory(String mainCategory) { this.mainCategory = mainCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setMonth(int month) { this.month = month; }
    public void setYear(int year) { this.year = year; }
}
