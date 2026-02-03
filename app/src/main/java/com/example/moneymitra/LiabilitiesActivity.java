package com.example.moneymitra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.adapter.LiabilityAdapter;
import com.example.moneymitra.model.LiabilityItem;
import com.example.moneymitra.repository.FirestoreLiabilityRepository;
import com.example.moneymitra.repository.LiabilityRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * LiabilitiesActivity
 * Responsibility:
 * - Show list of liabilities
 * - Handle empty state
 * - Open Add screen
 * - Handle long-press → bottom sheet
 */
public class LiabilitiesActivity extends AppCompatActivity {

    // -------------------------
    // UI
    // -------------------------
    private RecyclerView rvLiabilities;
    private View tvEmpty;

    // -------------------------
    // DATA + ADAPTER
    // -------------------------
    private LiabilityAdapter adapter;
    private LiabilityRepository repository;
    private boolean isRefreshingSilently = false;
    private final List<LiabilityItem> liabilityList = new ArrayList<>();
    TextView tvTotalAmount;

    // -------------------------
    // LIFECYCLE
    // -------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liabilities);

        findViewById(R.id.ic_back).setOnClickListener(v -> {
            finish(); // go back to previous screen
        });

        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        // 1️⃣ Bind views
        rvLiabilities = findViewById(R.id.rvLiabilities);
        tvEmpty = findViewById(R.id.tvEmpty);
        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);

        // 2️⃣ Setup adapter
        adapter = new LiabilityAdapter();

        // Long-press callback from adapter
        adapter.setOnItemLongClickListener(item -> {
            showLiabilityActionsSheet(item);
        });

        rvLiabilities.setLayoutManager(new LinearLayoutManager(this));
        rvLiabilities.setAdapter(adapter);

        // 3️⃣ Repository
        repository = new FirestoreLiabilityRepository();

        // 4️⃣ Initial load
        loadLiabilities();

        // 5️⃣ Add button
        findViewById(R.id.ic_add).setOnClickListener(v ->
                startActivity(new Intent(
                        LiabilitiesActivity.this,
                        AddEditLiabilityActivity.class
                ))
        );
    }

    /**
     * Called when coming back from Add/Edit screen
     * Ensures fresh data every time
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadLiabilities();
    }

    // -------------------------
    // DATA LOADING
    // -------------------------
    private void loadLiabilities() {

        // Hide UI while loading (prevents flicker)
        if (!isRefreshingSilently) {
            tvEmpty.setVisibility(View.GONE);
            rvLiabilities.setVisibility(View.GONE);
        }

        repository.fetchAllLiabilities(new LiabilityRepository.Callback<List<LiabilityItem>>() {
            @Override
            public void onSuccess(List<LiabilityItem> result) {

                // -------------------------
                // 1️⃣ CLEAR OLD DATA

                liabilityList.clear();

                // -------------------------
                // 2️⃣ ADD FRESH DATA FROM FIRESTORE

                if (result != null) {
                    liabilityList.addAll(result);
                }

                // -------------------------
                // 3️⃣ CALCULATE TOTAL LIABILITY AMOUNT
                double total = 0;
                for (LiabilityItem item : liabilityList) {
                    total += item.getTotalAmount();
                }

                // Update total amount text on summary card
                tvTotalAmount.setText(formatAmount(total));


                // 4️⃣ EMPTY STATE vs LIST STATE

                if (liabilityList.isEmpty()) {

                    // No data → show empty message
                    tvEmpty.setVisibility(View.VISIBLE);

                    // Hide list & total card
                    rvLiabilities.setVisibility(View.GONE);
                    findViewById(R.id.cardTotalLiability).setVisibility(View.GONE);

                } else {

                    // Data exists → show list
                    tvEmpty.setVisibility(View.GONE);
                    rvLiabilities.setVisibility(View.VISIBLE);
                    findViewById(R.id.cardTotalLiability).setVisibility(View.VISIBLE);

                    // Send data to RecyclerView adapter
                    adapter.updateList(liabilityList);
                }
            }

            @Override
            public void onFailure(Exception e) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvLiabilities.setVisibility(View.GONE);
            }
        });
    }

    // -------------------------
    // BOTTOM SHEET (TASK 6.1)
    // -------------------------
    private void showLiabilityActionsSheet(LiabilityItem item) {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater()
                .inflate(R.layout.bottom_sheet_liability_actions, null);
// Edit Mode
        view.findViewById(R.id.tvEdit).setOnClickListener(v -> {
            dialog.dismiss();

            Intent intent = new Intent(
                    LiabilitiesActivity.this,
                    AddEditLiabilityActivity.class
            );

            // Pass data for EDIT MODE
            intent.putExtra("LIABILITY_ID", item.getId());
            intent.putExtra("NAME", item.getName());
            intent.putExtra("CATEGORY", item.getCategory());
            intent.putExtra("TOTAL_AMOUNT", item.getTotalAmount());

            if (item.getEmi() != null) {
                intent.putExtra("EMI", item.getEmi());
            }

            intent.putExtra("NOTE", item.getNote());

            startActivity(intent);
        });

        // Delete
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            dialog.dismiss();

            isRefreshingSilently = true;

            repository.deleteLiability(item.getId());
            loadLiabilities();

            isRefreshingSilently = false;

        });


        dialog.setContentView(view);
        dialog.show();
    }
    private String formatAmount(double amount) {
        return "₹ " + String.format("%,.0f", amount)
                .replace(",", "_")
                .replace("_", ",");
    }


}
