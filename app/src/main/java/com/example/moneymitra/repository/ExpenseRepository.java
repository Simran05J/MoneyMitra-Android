package com.example.moneymitra.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.moneymitra.model.ExpenseItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ExpenseRepository {

    private static ExpenseRepository instance;
    private final FirebaseFirestore db;
    private final String uid;

    private final List<ExpenseItem> expenseList = new ArrayList<>();

    private ExpenseRepository() {
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();
    }

    public static ExpenseRepository getInstance() {
        if (instance == null) {
            instance = new ExpenseRepository();
        }
        return instance;
    }

    // ================= ADD EXPENSE =================
    public void addExpense(ExpenseItem item) {
        db.collection("users")
                .document(uid)
                .collection("expenses")
                .add(item)
                .addOnSuccessListener(doc -> Log.d("EXPENSE_FIREBASE", "Added"))
                .addOnFailureListener(e -> Log.e("EXPENSE_FIREBASE", "Add failed", e));
    }


    // ================= LOAD ALL EXPENSES =================
    public void loadExpenses(Consumer<List<ExpenseItem>> callback) {

        expenseList.clear();

        db.collection("users")
                .document(uid)
                .collection("expenses")
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (QueryDocumentSnapshot doc : snapshot) {
                        ExpenseItem item = doc.toObject(ExpenseItem.class);
                        item.setId(doc.getId());
                        expenseList.add(item);
                    }

                    callback.accept(expenseList);
                })
                .addOnFailureListener(e ->
                        Log.e("EXPENSE_FIREBASE", "Load failed", e));
    }


    // ================= DELETE EXPENSE =================
    public void deleteExpense(ExpenseItem item) {
        db.collection("users")
                .document(uid)
                .collection("expenses")
                .document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d("EXPENSE_FIREBASE", "Deleted"))
                .addOnFailureListener(e ->
                        Log.e("EXPENSE_FIREBASE", "Delete failed", e));
    }


    // ================= UPDATE EXPENSE =================
    public void updateExpense(ExpenseItem item) {

        db.collection("users")
                .document(uid)
                .collection("expenses")
                .document(item.getId())
                .set(item) // overwrite document
                .addOnSuccessListener(aVoid ->
                        Log.d("EXPENSE_FIREBASE", "Updated"))
                .addOnFailureListener(e ->
                        Log.e("EXPENSE_FIREBASE", "Update failed", e));
    }

    public List<ExpenseItem> getAllExpenses() {
        return expenseList;
    }
}
