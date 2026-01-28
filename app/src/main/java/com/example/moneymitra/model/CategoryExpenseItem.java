package com.example.moneymitra.model;

public class CategoryExpenseItem {

    private final String categoryName;
    private final float expense;
    private final float budget;
    private final int iconRes;

    public CategoryExpenseItem(String categoryName, float expense, float budget, int iconRes) {
        this.categoryName = categoryName;
        this.expense = expense;
        this.budget = budget;
        this.iconRes = iconRes;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public float getExpense() {
        return expense;
    }

    public float getBudget() {
        return budget;
    }

    public int getIconRes() {
        return iconRes;
    }
}
