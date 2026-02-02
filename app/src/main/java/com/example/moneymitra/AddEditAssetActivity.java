package com.example.moneymitra;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymitra.model.AssetItem;
import com.example.moneymitra.repository.AssetRepository;
import com.example.moneymitra.repository.FirestoreAssetRepository;

public class AddEditAssetActivity extends AppCompatActivity {

    // UI fields
    private EditText etName, etValue, etNote;
    private Spinner spCategory;

    // Edit mode state
    private boolean isEditMode = false;
    private String assetId; // required only for update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_asset);

        // 1️⃣ Bind UI
        etName = findViewById(R.id.etAssetName);
        etValue = findViewById(R.id.etAssetValue);
        etNote = findViewById(R.id.etAssetNote);
        spCategory = findViewById(R.id.spAssetCategory);
        Button btnSave = findViewById(R.id.btnSaveAsset);

        if ("EDIT".equals(getIntent().getStringExtra("MODE"))) {
            btnSave.setText("Update Asset");
        }


        // 2️⃣ Setup fixed category dropdown
        setupCategorySpinner();

        // 3️⃣ Detect EDIT mode and prefill
        if ("EDIT".equals(getIntent().getStringExtra("MODE"))) {
            isEditMode = true;
            prefillData();
        }

        // 4️⃣ Save button (Add or Update)
        btnSave.setOnClickListener(v -> {
            v.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.press_scale)
            );
            validateForm();
        });

    }

    /**
     * Setup fixed categories for Assets
     */
    private void setupCategorySpinner() {
        String[] categories = {
                "Cash",
                "Bank Account",
                "Investment",
                "Property",
                "Gold",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );
        spCategory.setAdapter(adapter);
    }

    /**
     * Prefill fields when coming from Edit action
     */
    private void prefillData() {
        assetId = getIntent().getStringExtra("ASSET_ID");

        etName.setText(getIntent().getStringExtra("ASSET_NAME"));
        etValue.setText(String.valueOf(
                getIntent().getDoubleExtra("ASSET_VALUE", 0)
        ));
        etNote.setText(getIntent().getStringExtra("ASSET_NOTE"));

        String category = getIntent().getStringExtra("ASSET_CATEGORY");
        ArrayAdapter adapter = (ArrayAdapter) spCategory.getAdapter();
        int position = adapter.getPosition(category);
        spCategory.setSelection(position);
    }

    /**
     * Validate input and decide ADD vs UPDATE
     */
    private void validateForm() {
        String name = etName.getText().toString().trim();
        String valueStr = etValue.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        // Basic validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            return;
        }

        if (TextUtils.isEmpty(valueStr)) {
            etValue.setError("Required");
            return;
        }

        double value = Double.parseDouble(valueStr);
        if (value <= 0) {
            etValue.setError("Must be greater than 0");
            return;
        }

        AssetRepository repository = new FirestoreAssetRepository();

        // 🔁 EDIT MODE → UPDATE
        if (isEditMode) {
            AssetItem updatedAsset = new AssetItem(
                    assetId,
                    name,
                    category,
                    value,
                    note,
                    System.currentTimeMillis()
            );

            repository.updateAsset(updatedAsset, new AssetRepository.OnOperationCompleteListener() {
                @Override
                public void onSuccess() {
                    finish(); // back to list
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(AddEditAssetActivity.this,
                            "Failed to update asset", Toast.LENGTH_SHORT).show();
                }
            });

        }
        // ➕ ADD MODE → CREATE
        else {
            AssetItem newAsset = new AssetItem(
                    null,
                    name,
                    category,
                    value,
                    note,
                    System.currentTimeMillis()
            );

            repository.addAsset(newAsset, new AssetRepository.OnOperationCompleteListener() {
                @Override
                public void onSuccess() {
                    finish(); // back to list
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(AddEditAssetActivity.this,
                            "Failed to save asset", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
