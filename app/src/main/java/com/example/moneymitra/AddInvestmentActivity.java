package com.example.moneymitra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import android.widget.ArrayAdapter;

public class AddInvestmentActivity extends AppCompatActivity {

    // ================= VIEWS =================
    private EditText etInvestmentLabel;
    private EditText etAmount;
    private MaterialAutoCompleteTextView etCategory;
    private TextView btnSave;

    // ================= EDIT MODE =================
    private boolean isEditMode = false;
    private int editPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_investment);

        initViews();
        setupCategoryDropdown();
        checkEditMode();          // 🔥 MOST IMPORTANT
        setupClickListeners();
    }

    // ================= INIT VIEWS =================
    private void initViews() {
        etInvestmentLabel = findViewById(R.id.etName);
        etAmount = findViewById(R.id.etAmount);
        etCategory = findViewById(R.id.etCategory);
        btnSave = findViewById(R.id.btnSave);
    }

    // ================= EDIT MODE CHECK =================
    private void checkEditMode() {
        Intent intent = getIntent();

        if (intent != null && "edit".equals(intent.getStringExtra("mode"))) {
            isEditMode = true;
            editPosition = intent.getIntExtra("position", -1);

            String fullName = intent.getStringExtra("investment_name");
            int amount = intent.getIntExtra("investment_amount", 0);

            if (fullName != null) {
                // name (category) split
                if (fullName.contains("(")) {
                    String name = fullName.substring(0, fullName.indexOf("(")).trim();
                    String category =
                            fullName.substring(fullName.indexOf("(") + 1, fullName.indexOf(")"));

                    etInvestmentLabel.setText(name);
                    etCategory.setText(category, false);
                } else {
                    etInvestmentLabel.setText(fullName);
                }
            }

            etAmount.setText(String.valueOf(amount));
            btnSave.setText("Update Investment");
        }
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

            String name = etInvestmentLabel.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String category = etCategory.getText().toString().trim();

            if (name.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int amount = Integer.parseInt(amountStr);
            String finalName = name + " (" + category + ")";

            Intent resultIntent = new Intent();
            resultIntent.putExtra("investment_name", finalName);
            resultIntent.putExtra("investment_amount", amount);

            if (isEditMode) {
                resultIntent.putExtra("mode", "edit");
                resultIntent.putExtra("position", editPosition);
                Toast.makeText(this, "Investment updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Investment added", Toast.LENGTH_SHORT).show();
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        });

    }
}
