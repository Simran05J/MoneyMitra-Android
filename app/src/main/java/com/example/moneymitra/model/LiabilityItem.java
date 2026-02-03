package com.example.moneymitra.model;

public class LiabilityItem {

    private String id;
    private String name;
    private String category;
    private double totalAmount;
    private Double emi; // nullable
    private String note;
    private long createdAt;

    public LiabilityItem() {
        // required empty constructor
    }

    public LiabilityItem(String id, String name, String category,
                         double totalAmount, Double emi,
                         String note, long createdAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.totalAmount = totalAmount;
        this.emi = emi;
        this.note = note;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getEmi() {
        return emi;
    }

    public void setEmi(Double emi) {
        this.emi = emi;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
