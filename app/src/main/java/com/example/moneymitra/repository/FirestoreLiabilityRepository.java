package com.example.moneymitra.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.moneymitra.model.LiabilityItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;


import java.util.ArrayList;

import java.util.List;
import java.util.Map;

public class FirestoreLiabilityRepository implements LiabilityRepository {

    private static final String TAG = "LiabilityRepo";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public FirestoreLiabilityRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * TASK 5.2
     * REAL Firestore write
     * Adds a liability under:
     * users/{userId}/liabilities/{liabilityId}
     */
    @Override
    public void addLiability(LiabilityItem item) {

        // 1️⃣ Ensure user is logged in
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "addLiability failed: user not logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // 2️⃣ Create document reference (auto-generated ID)
        DocumentReference docRef = firestore
                .collection("users")
                .document(userId)
                .collection("liabilities")
                .document();

        // 3️⃣ Prepare Firestore data map
        Map<String, Object> data = new HashMap<>();
        data.put("id", docRef.getId()); // VERY IMPORTANT (for edit/delete later)
        data.put("name", item.getName());
        data.put("category", item.getCategory());
        data.put("totalAmount", item.getTotalAmount());
        data.put("createdAt", System.currentTimeMillis());

        // 4️⃣ Optional fields — add ONLY if present
        if (item.getEmi() != null) {
            data.put("emi", item.getEmi());
        }

        if (item.getNote() != null && !item.getNote().isEmpty()) {
            data.put("note", item.getNote());
        }

        // 5️⃣ Write to Firestore
        docRef.set(data)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Liability added successfully: " + docRef.getId())
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to add liability", e)
                );
    }

    // Update Liability
    @Override
    public void updateLiability(LiabilityItem item) {

        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "updateLiability failed: user not logged in");
            return;
        }

        if (item.getId() == null) {
            Log.e(TAG, "updateLiability failed: liability ID is null");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", item.getName());
        updates.put("category", item.getCategory());
        updates.put("totalAmount", item.getTotalAmount());
        updates.put("updatedAt", System.currentTimeMillis());

        // Optional fields
        if (item.getEmi() != null) {
            updates.put("emi", item.getEmi());
        } else {
            updates.put("emi", FieldValue.delete());
        }

        if (item.getNote() != null && !item.getNote().isEmpty()) {
            updates.put("note", item.getNote());
        } else {
            updates.put("note", FieldValue.delete());
        }

        firestore
                .collection("users")
                .document(userId)
                .collection("liabilities")
                .document(item.getId())
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Liability updated: " + item.getId())
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update liability", e)
                );
    }



    // Delete Liability
    @Override
    public void deleteLiability(String liabilityId) {

        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "deleteLiability failed: user not logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        firestore
                .collection("users")
                .document(userId)
                .collection("liabilities")
                .document(liabilityId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Liability deleted: " + liabilityId)
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to delete liability", e)
                );
    }


    // ❌ STUB — WILL BE IMPLEMENTED IN TASK 5.3
    @Override
    public void fetchAllLiabilities(Callback<List<LiabilityItem>> callback) {

        if (auth.getCurrentUser() == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        firestore
                .collection("users")
                .document(userId)
                .collection("liabilities")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<LiabilityItem> list = new ArrayList<>();

                    for (var doc : querySnapshot.getDocuments()) {

                        LiabilityItem item = new LiabilityItem();

                        item.setId(doc.getString("id"));
                        item.setName(doc.getString("name"));
                        item.setCategory(doc.getString("category"));

                        Double amount = doc.getDouble("totalAmount");
                        item.setTotalAmount(amount != null ? amount : 0);

                        // Optional fields
                        item.setEmi(doc.contains("emi") ? doc.getDouble("emi") : null);
                        item.setNote(doc.contains("note") ? doc.getString("note") : null);

                        list.add(item);
                    }

                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

}
