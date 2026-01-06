package com.example.moneymitra;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymitra.model.InvestmentItem;
import com.example.moneymitra.repository.InvestmentRepository;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;

public class AddInvestmentActivity extends AppCompatActivity {

    // ================= VIEWS =================
    private EditText etInvestmentLabel;
    private EditText etAmount;
    private EditText etGoal; // 🔥 NEW
    private MaterialAutoCompleteTextView etCategory;
    private TextView btnSave;

    // ================= EDIT MODE =================
    private boolean isEditMode = false;
    private String editInvestmentId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_investment);

        // ================= CHECK EDIT MODE =================
        if (getIntent() != null && "EDIT".equals(getIntent().getStringExtra("MODE"))) {
            isEditMode = true;
            editInvestmentId = getIntent().getStringExtra("INVESTMENT_ID");
        }

        initViews();
        setupCategoryDropdown();   // 🔥 adapter FIRST (important)

        // ================= PREFILL FOR EDIT =================
        if (isEditMode) {

            String name = getIntent().getStringExtra("NAME");
            long amount = getIntent().getLongExtra("AMOUNT", 0);
            String goal = getIntent().getStringExtra("GOAL"); // 🔥 NEW

            if (name != null) {
                // Expected format: "SIP (Mutual Funds)"
                if (name.contains("(") && name.contains(")")) {

                    String actualName =
                            name.substring(0, name.indexOf("(")).trim();

                    String category =
                            name.substring(name.indexOf("(") + 1, name.indexOf(")"));

                    etInvestmentLabel.setText(actualName);
                    etCategory.setText(category, false);

                } else {
                    etInvestmentLabel.setText(name);
                }
            }

            etAmount.setText(String.valueOf(amount));

            if (goal != null) {          // 🔥 PREFILL GOAL
                etGoal.setText(goal);
            }
        }

        setupClickListeners();
    }

    // ================= INIT VIEWS =================
    private void initViews() {
        etInvestmentLabel = findViewById(R.id.etName);
        etAmount = findViewById(R.id.etAmount);
        etCategory = findViewById(R.id.etCategory);
        etGoal = findViewById(R.id.etGoal);   // 🔥 NEW
        btnSave = findViewById(R.id.btnSave);
    }

    // ================= DROPDOWN =================
    private void setupCategoryDropdown() {

        String[] categories = {
                "Stocks",
                "Mutual Funds",
                "Crypto",
                "Gold",
                "Fixed Deposit",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                categories
        );

        etCategory.setAdapter(adapter);
        etCategory.setInputType(0);
    }

    // ================= CLICK HANDLING =================
    private void setupClickListeners() {

        btnSave.setOnClickListener(v -> {

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
                return;
            }

            String name = etInvestmentLabel.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String goal = etGoal.getText().toString().trim(); // 🔥 NEW

            if (name.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            long amount;
            try {
                amount = Long.parseLong(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            String finalName = name + " (" + category + ")";

            InvestmentRepository repository = new InvestmentRepository();

            if (isEditMode) {
                // ===== UPDATE EXISTING INVESTMENT =====
                InvestmentItem item = new InvestmentItem(
                        finalName,
                        amount,
                        0,
                        System.currentTimeMillis()
                );
                item.setId(editInvestmentId);
                item.setGoal(goal); // 🔥 SAVE GOAL

                repository.updateInvestment(item)
                        .addOnSuccessListener(aVoid -> {
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to update investment", Toast.LENGTH_SHORT).show()
                        );

            } else {
                // ===== ADD NEW INVESTMENT =====
                InvestmentItem item = new InvestmentItem(
                        finalName,
                        amount,
                        0,
                        System.currentTimeMillis()
                );
                item.setGoal(goal); // 🔥 SAVE GOAL

                repository.addInvestment(item)
                        .addOnSuccessListener(docRef -> {
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to add investment", Toast.LENGTH_SHORT).show()
                        );
            }
        });
    }
}
