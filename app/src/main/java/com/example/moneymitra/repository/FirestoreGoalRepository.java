package com.example.moneymitra.repository;

import com.example.moneymitra.model.GoalItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreGoalRepository implements GoalRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();

    @Override
    public void addGoal(GoalItem goal, RepositoryCallback callback) {
        if (userId == null) return;

        db.collection("goals")
                .document(userId)
                .collection("userGoals")
                .document(goal.getGoalId())
                .set(goal)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void getGoals(GoalCallback callback) {
        if (userId == null) return;

        db.collection("goals")
                .document(userId)
                .collection("userGoals")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GoalItem> goals = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        GoalItem goal = doc.toObject(GoalItem.class);
                        if (goal != null) {
                            goals.add(goal);
                        }
                    }
                    callback.onSuccess(goals);
                })
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void deleteGoal(String goalId, RepositoryCallback callback) {
        if (userId == null) return;

        db.collection("goals")
                .document(userId)
                .collection("userGoals")
                .document(goalId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
    @Override
    public void updateGoal(GoalItem goal, RepositoryCallback callback) {
        if (userId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("goalName", goal.getGoalName());
        updates.put("targetAmount", goal.getTargetAmount());
        updates.put("linkedInvestmentId", goal.getLinkedInvestmentId());

        db.collection("goals")
                .document(userId)
                .collection("userGoals")
                .document(goal.getGoalId())
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

}
