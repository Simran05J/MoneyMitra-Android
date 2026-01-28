package com.example.moneymitra;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymitra.model.InvestmentItem;
import com.example.moneymitra.repository.InvestmentRepository;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class AddInvestmentActivity extends AppCompatActivity {

    // ================= VIEWS =================
    private TextInputEditText etInvestmentLabel;
    private TextInputEditText etAmount;
    private TextInputEditText etGoal;
    private MaterialAutoCompleteTextView etCategory;
    private TextView btnSave;

    // ================= EDIT MODE =================
    private boolean isEditMode = false;
    private String editInvestmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_investment);

        initViews();
        setupCategoryDropdown();
        detectEditMode();
        setupClickListeners();
    }

    // ================= INIT VIEWS =================
    private void initViews() {
        etInvestmentLabel = findViewById(R.id.etName);
        etAmount = findViewById(R.id.etAmount);
        etGoal = findViewById(R.id.etGoal);
        etCategory = findViewById(R.id.etCategory);
        btnSave = findViewById(R.id.btnSave);
    }

    // ================= EDIT MODE =================
    private void detectEditMode() {
        if (getIntent() == null) return;

        if ("EDIT".equals(getIntent().getStringExtra("MODE"))) {
            isEditMode = true;
            editInvestmentId = getIntent().getStringExtra("INVESTMENT_ID");

            String fullName = getIntent().getStringExtra("NAME");
            long amount = getIntent().getLongExtra("AMOUNT", 0);
            String goal = getIntent().getStringExtra("GOAL");

            if (fullName != null && fullName.contains("(")) {
                String name = fullName.substring(0, fullName.indexOf("(")).trim();
                String category =
                        fullName.substring(fullName.indexOf("(") + 1, fullName.indexOf(")"));

                etInvestmentLabel.setText(name);
                etCategory.setText(category, false);
            } else if (fullName != null) {
                etInvestmentLabel.setText(fullName);
            }

            etAmount.setText(String.valueOf(amount));
            if (goal != null) etGoal.setText(goal);

            btnSave.setText("Update Investment");
        }
    }

    // ================= CATEGORY DROPDOWN =================
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

    // ================= SAVE HANDLING =================
    private void setupClickListeners() {

        btnSave.setOnClickListener(v -> {

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = etInvestmentLabel.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String goal = etGoal.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            if (name.isEmpty() || category.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            long amount;
            try {
                amount = Long.parseLong(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            String finalName = name + " (" + category + ")";
            InvestmentRepository repository = new InvestmentRepository();

            if (isEditMode) {
                InvestmentItem item = new InvestmentItem();
                item.setId(editInvestmentId);
                item.setName(finalName);
                item.setAmount(amount);
                item.setGoal(goal);

                repository.updateInvestment(item)
                        .addOnSuccessListener(a -> {
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                        );

            } else {
                InvestmentItem item = new InvestmentItem(
                        finalName,
                        amount,
                        0,
                        System.currentTimeMillis()
                );
                item.setGoal(goal);

                repository.addInvestment(item)
                        .addOnSuccessListener(a -> {
                            setResult(RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Add failed", Toast.LENGTH_SHORT).show()
                        );
            }
        });
    }
}
