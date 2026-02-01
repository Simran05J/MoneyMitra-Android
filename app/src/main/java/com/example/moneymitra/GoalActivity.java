package com.example.moneymitra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.moneymitra.repository.RepositoryCallback;

import android.view.MotionEvent;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.adapter.GoalAdapter;
import com.example.moneymitra.model.GoalItem;
import com.example.moneymitra.repository.FirestoreGoalRepository;
import com.example.moneymitra.repository.GoalRepository;
import com.example.moneymitra.repository.InvestmentRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalActivity extends AppCompatActivity {

    // ---------------- UI ----------------
    private RecyclerView rvGoals;
    private TextView tvEmptyState;

    // ---------------- ADAPTER & DATA ----------------
    private GoalAdapter adapter;
    private final List<GoalItem> goalList = new ArrayList<>();

    // ---------------- SELECTED ITEM (for edit/delete) ----------------
    private GoalItem selectedGoal;

    // ---------------- REPOSITORIES ----------------
    private GoalRepository goalRepository;
    private InvestmentRepository investmentRepository;
    private boolean isLoading = true;


    // ---------------- INVESTMENT MAPS ----------------
    // investmentId -> amount
    private final Map<String, Double> investmentAmountMap = new HashMap<>();
    // investmentId -> name
    private final Map<String, String> investmentNameMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        // ---------- Views ----------
        rvGoals = findViewById(R.id.rvGoals);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        // ---------- chatbot ----------
        ImageView ivChatbot = findViewById(R.id.ivChatbot);

        if (ivChatbot != null) {
            ivChatbot.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.press_scale));
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.release_scale));
                }
                return false;
            });
        }

// ---------- Add Goal Button ----------
        ImageView btnAddGoal = findViewById(R.id.btnAddGoal);
// ---------- Animation ----------
        btnAddGoal.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.press_scale));
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.release_scale));
            }
            return false;
        });

        // ---------- RecyclerView ----------
        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GoalAdapter(goalList, investmentAmountMap);
        adapter.setInvestmentNameMap(investmentNameMap);
        rvGoals.setAdapter(adapter);

        // ---------- Long press (Edit / Delete) ----------
        adapter.setOnGoalLongClickListener(goal -> {
            selectedGoal = goal;
            showGoalActionsSheet();
        });

        // ---------- Repositories ----------
        goalRepository = new FirestoreGoalRepository();
        investmentRepository = new InvestmentRepository();

        // ---------- Initial data fetch ----------
        fetchInvestments();

        // ---------- Add Goal ----------
        btnAddGoal.setOnClickListener(v ->
                startActivity(new Intent(this, AddGoalActivity.class))
        );

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvGoals.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

    }

    // =================================================
    // ============== BOTTOM SHEET =====================
    // =================================================
    private void showGoalActionsSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_goal_actions, null);

        dialog.setContentView(view);

        TextView btnEdit = view.findViewById(R.id.btnEditGoal);
        TextView btnDelete = view.findViewById(R.id.btnDeleteGoal);

        // EDIT
        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();

            Intent intent = new Intent(GoalActivity.this, AddGoalActivity.class);
            intent.putExtra("MODE", "EDIT");
            intent.putExtra("GOAL_ID", selectedGoal.getGoalId());
            intent.putExtra("GOAL_NAME", selectedGoal.getGoalName());
            intent.putExtra("GOAL_TARGET", selectedGoal.getTargetAmount());
            intent.putExtra("LINKED_INVESTMENT_ID", selectedGoal.getLinkedInvestmentId());

            startActivity(intent);
        });


        // DELETE
        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            deleteGoal();
        });

        dialog.show();
    }

    // =================================================
    // ============== DELETE GOAL ======================
    // =================================================
    private void deleteGoal() {
        if (selectedGoal == null) return;

        goalRepository.deleteGoal(
                selectedGoal.getGoalId(),
                new RepositoryCallback() {
                    @Override
                    public void onSuccess() {
                        goalList.remove(selectedGoal);
                        adapter.updateList(goalList);
                        selectedGoal = null;
                        toggleEmptyState();
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }



    // =================================================
    // ============== FETCH GOALS ======================
    // =================================================
    private void fetchGoals() {
        isLoading = true;

        goalRepository.getGoals(new GoalRepository.GoalCallback() {
            @Override
            public void onSuccess(List<GoalItem> goals) {
                isLoading = false;

                goalList.clear();
                goalList.addAll(goals);
                adapter.updateList(goalList);

                toggleEmptyState();
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    // =================================================
    // ============== EMPTY STATE ======================
    // =================================================
    private void toggleEmptyState() {

        if (isLoading) {
            rvGoals.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.GONE);
            return;
        }

        if (goalList.isEmpty()) {
            crossFade(tvEmptyState, rvGoals);
        } else {
            crossFade(rvGoals, tvEmptyState);
        }
    }


    // =================================================
    // ============== FETCH INVESTMENTS ================
    // =================================================
    private void fetchInvestments() {
        investmentRepository.fetchInvestments(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {

                investmentAmountMap.clear();
                investmentNameMap.clear();

                for (DocumentSnapshot doc : querySnapshot) {
                    String id = doc.getId();
                    Double amount = doc.getDouble("amount");
                    String name = doc.getString("name");

                    if (amount != null) investmentAmountMap.put(id, amount);
                    if (name != null) investmentNameMap.put(id, name);
                }

                fetchGoals();

            }

        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("INV_DEBUG", "Fetch investments failed", e);
            }
        });
    }
    //---------Helper Methods---------
    private void fadeIn(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void crossFade(View show, View hide) {
        hide.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> hide.setVisibility(View.GONE))
                .start();

        show.setAlpha(0f);
        show.setVisibility(View.VISIBLE);
        show.animate()
                .alpha(1f)
                .setDuration(250)
                .start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        fetchInvestments();
    }
}
