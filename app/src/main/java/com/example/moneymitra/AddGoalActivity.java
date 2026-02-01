package com.example.moneymitra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;


import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymitra.model.GoalItem;
import com.example.moneymitra.repository.FirestoreGoalRepository;
import com.example.moneymitra.repository.GoalRepository;
import com.example.moneymitra.repository.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddGoalActivity extends AppCompatActivity {

    // ---------------- UI ----------------
    private EditText etTitle, etTarget;
    private Spinner spinnerInvestment;
    private Button btnSave;

    // ---------------- MODE ----------------
    private boolean isEditMode = false;
    private String editingGoalId = null;
    private String linkedInvestmentId = "NONE";

    // ---------------- REPO ----------------
    private GoalRepository goalRepository;

    // ---------------- INVESTMENT DATA ----------------
    private final List<String> investmentNames = new ArrayList<>();
    private final Map<String, String> investmentNameToIdMap = new HashMap<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        // ---------- Bind Views FIRST ----------
        etTitle = findViewById(R.id.etGoalTitle);
        etTarget = findViewById(R.id.etGoalTarget);
        spinnerInvestment = findViewById(R.id.spinnerInvestment);
        btnSave = findViewById(R.id.btnSaveGoal);

        goalRepository = new FirestoreGoalRepository();

        // ---------- Spinner setup ----------
        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                investmentNames
        );
        spinnerInvestment.setAdapter(spinnerAdapter);

        // ---------- Read Intent (EDIT MODE) ----------
        readEditIntent();
        if (!isEditMode) {
            btnSave.setText("Save Goal");
        }


        // ---------- Fetch investments ----------
        fetchInvestments();
        // ---------- Button Animation ----------
        btnSave.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.press_scale));
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.release_scale));
            }
            return false;
        });

        // ---------- Save button ----------
        btnSave.setOnClickListener(v -> saveGoal());
    }

    // =================================================
    // ============== READ EDIT MODE ===================
    // =================================================
    private void readEditIntent() {
        Intent intent = getIntent();
        if (intent != null && "EDIT".equals(intent.getStringExtra("MODE"))) {
            isEditMode = true;

            editingGoalId = intent.getStringExtra("GOAL_ID");
            linkedInvestmentId = intent.getStringExtra("LINKED_INVESTMENT_ID");

            String goalName = intent.getStringExtra("GOAL_NAME");
            double targetAmount = intent.getDoubleExtra("GOAL_TARGET", 0);

            etTitle.setText(goalName);
            etTarget.setText(String.valueOf(targetAmount));

            btnSave.setText("Update Goal");

        }

    }

    // =================================================
    // ============== FETCH INVESTMENTS ================
    // =================================================
    private void fetchInvestments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();

        investmentNames.clear();
        investmentNameToIdMap.clear();

        investmentNames.add("No Investment");
        investmentNameToIdMap.put("No Investment", "NONE");

        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("investments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("name");

                        if (name != null) {
                            investmentNames.add(name);
                            investmentNameToIdMap.put(name, id);
                        }
                    }

                    spinnerAdapter.notifyDataSetChanged();
                    restoreSpinnerSelection(); // 🔥 important for EDIT mode
                })
                .addOnFailureListener(e -> {
                    Log.e("INV_ERROR", "Fetch failed", e);
                    Toast.makeText(this, "Failed to load investments", Toast.LENGTH_SHORT).show();
                });
    }

    // =================================================
    // ============== RESTORE SPINNER ==================
    // =================================================
    private void restoreSpinnerSelection() {
        if (!isEditMode || linkedInvestmentId == null) return;

        for (int i = 0; i < investmentNames.size(); i++) {
            String name = investmentNames.get(i);
            if (linkedInvestmentId.equals(investmentNameToIdMap.get(name))) {
                spinnerInvestment.setSelection(i);
                break;
            }
        }
    }

    // =================================================
    // ============== SAVE / UPDATE GOAL ===============
    // =================================================
    private void saveGoal() {
        String title = etTitle.getText().toString().trim();
        String targetStr = etTarget.getText().toString().trim();

        if (title.isEmpty() || targetStr.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double targetAmount = Double.parseDouble(targetStr);
        String selectedName = spinnerInvestment.getSelectedItem().toString();

        linkedInvestmentId = investmentNameToIdMap.getOrDefault(selectedName, "NONE");

        // 🔥 SAME MODEL FOR ADD & EDIT
        GoalItem goal = new GoalItem(
                isEditMode ? editingGoalId : UUID.randomUUID().toString(),
                title,
                targetAmount,
                0,
                linkedInvestmentId,
                System.currentTimeMillis()
        );

        if (isEditMode) {
            updateGoal(goal);
        } else {
            addGoal(goal);
        }
    }

    // ---------------- ADD ----------------
    private void addGoal(GoalItem goal) {
        goalRepository.addGoal(goal, new RepositoryCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddGoalActivity.this, "Goal added", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AddGoalActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------- UPDATE ----------------
    private void updateGoal(GoalItem goal) {
        goalRepository.updateGoal(goal, new RepositoryCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddGoalActivity.this, "Goal updated", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AddGoalActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
