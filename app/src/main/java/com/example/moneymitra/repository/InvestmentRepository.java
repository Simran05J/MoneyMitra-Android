package com.example.moneymitra.repository;

import com.example.moneymitra.model.InvestmentItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class InvestmentRepository {

    private final FirebaseFirestore db;
    private String userId;

    public InvestmentRepository() {
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }
    }

    private boolean isUserAvailable() {
        return userId != null;
    }

    // ================= COLLECTION REF =================
    private CollectionReference investmentsRef() {
        String uid = FirebaseAuth.getInstance().getUid();
        return db.collection("users")
                .document(uid)
                .collection("investments");   // EXACT Firebase name
    }


    // ================= ADD =================
    public Task<DocumentReference> addInvestment(InvestmentItem item) {
        if (!isUserAvailable()) {
            return Tasks.forException(
                    new Exception("User not logged in")
            );
        }

        return investmentsRef()
                .add(item)
                .addOnSuccessListener(docRef -> item.setId(docRef.getId()));
    }

    // ================= FETCH =================
    public void fetchInvestments(
            OnSuccessListener<QuerySnapshot> success,
            OnFailureListener failure
    ) {
        if (!isUserAvailable()) {
            failure.onFailure(new Exception("User not logged in"));
            return;
        }

        investmentsRef()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }

    // ================= DELETE =================
    public Task<Void> deleteInvestment(String investmentId) {
        if (!isUserAvailable()) {
            return Tasks.forException(
                    new Exception("User not logged in")
            );
        }

        return investmentsRef()
                .document(investmentId)
                .delete();
    }
    // ================= UPDATE =================
    public Task<Void> updateInvestment(InvestmentItem item) {
        if (!isUserAvailable()) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        return investmentsRef()
                .document(item.getId())
                .update(
                        "name", item.getName(),
                        "amount", item.getAmount(),
                        "color", item.getColor(),
                        "createdAt", item.getCreatedAt(),
                        "goal", item.getGoal() // 🔥 IMPORTANT
                );
    }


}
