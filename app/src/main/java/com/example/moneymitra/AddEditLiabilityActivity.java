package com.example.moneymitra;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymitra.model.LiabilityItem;
import com.example.moneymitra.repository.FirestoreLiabilityRepository;
import com.example.moneymitra.repository.LiabilityRepository;

/**
 * AddEditLiabilityActivity
 *
 * Single screen used for:
 * 1️⃣ Adding a new liability
 * 2️⃣ Editing an existing liability
 *
 * How it decides mode:
 * - If Intent contains "LIABILITY_ID" → EDIT MODE
 * - Else → ADD MODE
 */
public class AddEditLiabilityActivity extends AppCompatActivity {

    // -------------------------
    // DATA
    // -------------------------
    private LiabilityRepository repository;

    // Edit-mode flags
    private boolean isEditMode = false;
    private String editLiabilityId = null;

    // -------------------------
    // LIFECYCLE
    // -------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_liability);

        // -------------------------
        // CHECK WHETHER THIS IS EDIT MODE
        // -------------------------
        if (getIntent() != null && getIntent().hasExtra("LIABILITY_ID")) {
            isEditMode = true;
            editLiabilityId = getIntent().getStringExtra("LIABILITY_ID");
        }

        // -------------------------
        // REPOSITORY
        // -------------------------
        repository = new FirestoreLiabilityRepository();

        // -------------------------
        // UI REFERENCES
        // -------------------------
        EditText etName = findViewById(R.id.etName);
        EditText etCategory = findViewById(R.id.etCategory);
        EditText etTotalAmount = findViewById(R.id.etTotalAmount);
        EditText etEmi = findViewById(R.id.etEmi);
        EditText etNote = findViewById(R.id.etNote);
        Button btnSave = findViewById(R.id.btnSave);

        // Category dropdown
        AutoCompleteTextView categoryDropdown = (AutoCompleteTextView) etCategory;

        String[] categories = {
                "Home Loan",
                "Car Loan",
                "Education Loan",
                "Personal Loan",
                "Credit Card",
                "Other"
        };

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        categories
                );

        categoryDropdown.setAdapter(categoryAdapter);

// 🔥 THIS LINE FIXES EVERYTHING
        categoryDropdown.setOnClickListener(v -> categoryDropdown.showDropDown());


        // -------------------------
        // PREFILL DATA (EDIT MODE ONLY)
        // -------------------------
        if (isEditMode) {

            etName.setText(getIntent().getStringExtra("NAME"));
            etCategory.setText(getIntent().getStringExtra("CATEGORY"));

            etTotalAmount.setText(
                    String.valueOf(
                            getIntent().getDoubleExtra("TOTAL_AMOUNT", 0)
                    )
            );

            // EMI is optional
            if (getIntent().hasExtra("EMI")) {
                etEmi.setText(
                        String.valueOf(getIntent().getDoubleExtra("EMI", 0))
                );
            }

            etNote.setText(getIntent().getStringExtra("NOTE"));

            // Change button text to reflect edit mode
            btnSave.setText("Update Liability");
        }

        // -------------------------
        // SAVE BUTTON LOGIC
        // -------------------------
        btnSave.setOnClickListener(v -> {

            // Read user input
            String name = etName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String totalAmountStr = etTotalAmount.getText().toString().trim();
            String emiStr = etEmi.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            // Basic validation
            if (TextUtils.isEmpty(name)) {
                etName.setError("Required");
                return;
            }

            if (TextUtils.isEmpty(totalAmountStr)) {
                etTotalAmount.setError("Required");
                return;
            }

            double totalAmount = Double.parseDouble(totalAmountStr);
            Double emi = TextUtils.isEmpty(emiStr) ? null : Double.parseDouble(emiStr);

            // -------------------------
            // EDIT MODE → UPDATE EXISTING
            // -------------------------
            if (isEditMode) {

                LiabilityItem updatedItem = new LiabilityItem(
                        editLiabilityId,   // IMPORTANT: existing ID
                        name,
                        category,
                        totalAmount,
                        emi,
                        note,
                        System.currentTimeMillis()
                );

                // Firestore update (implemented in next step)
                repository.updateLiability(updatedItem);

                Toast.makeText(this, "Liability updated", Toast.LENGTH_SHORT).show();

            }
            // -------------------------
            // ADD MODE → CREATE NEW
            // -------------------------
            else {

                LiabilityItem newItem = new LiabilityItem(
                        null, // ID generated by Firestore
                        name,
                        category,
                        totalAmount,
                        emi,
                        note,
                        System.currentTimeMillis()
                );

                repository.addLiability(newItem);

                Toast.makeText(this, "Liability added", Toast.LENGTH_SHORT).show();
            }

            // Close screen and return to list
            finish();
        });
    }
}
