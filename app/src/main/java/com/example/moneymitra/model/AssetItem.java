package com.example.moneymitra.model;

import java.util.HashMap;
import java.util.Map;

public class AssetItem {

    private String assetId;
    private String name;
    private String category;
    private double value;
    private String note;
    private long createdAt;

    // 🔹 Required empty constructor for Firestore
    public AssetItem() {
    }

    public AssetItem(String assetId, String name, String category,
                     double value, String note, long createdAt) {
        this.assetId = assetId;
        this.name = name;
        this.category = category;
        this.value = value;
        this.note = note;
        this.createdAt = createdAt;
    }

    // 🔹 Getters & Setters

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
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

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
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

    // 🔹 Helper for Firestore updates
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("assetId", assetId);
        map.put("name", name);
        map.put("category", category);
        map.put("value", value);
        map.put("note", note);
        map.put("createdAt", createdAt);
        return map;
    }
}
