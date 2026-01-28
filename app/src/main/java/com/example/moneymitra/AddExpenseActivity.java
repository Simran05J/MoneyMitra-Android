package com.example.moneymitra;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymitra.model.ExpenseItem;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {


    // ================= UI =================
    private EditText etExpenseTitle, etExpenseAmount, etDate;
    private MaterialAutoCompleteTextView etMainCategory;
    private TextView btnSaveExpense;

    // ================= STATE =================
    private boolean isEditMode = false;
    private long selectedDateMillis = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);


        // 🔹 Bind Views
        etExpenseTitle = findViewById(R.id.etExpenseTitle);
        etExpenseAmount = findViewById(R.id.etExpenseAmount);
        etMainCategory = findViewById(R.id.etMainCategory);
        etDate = findViewById(R.id.etDate);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);

        // ================= EDIT MODE PREFILL =================
        if (getIntent() != null && "EDIT".equals(getIntent().getStringExtra("MODE"))) {

            isEditMode = true;

            String title = getIntent().getStringExtra("EDIT_TITLE");
            double amount = getIntent().getDoubleExtra("EDIT_AMOUNT", 0);
            String category = getIntent().getStringExtra("EDIT_CATEGORY");

            // 🔥 IMPORTANT: Preserve original timestamp for correct month filtering
            long originalTimestamp = getIntent().getLongExtra("EDIT_TIMESTAMP", selectedDateMillis);
            selectedDateMillis = originalTimestamp;

            etExpenseTitle.setText(title);
            etExpenseAmount.setText(String.valueOf((int) amount));
            etMainCategory.setText(category, false);

            btnSaveExpense.setText("Update Expense");
        }

        // ================= CATEGORY DROPDOWN =================
        List<String> categories = Arrays.asList(
                "Food",
                "Transportation",
                "Home & Utilities",
                "Health",
                "Lifestyle",
                "Other"
        );

        etMainCategory.setAdapter(
                new android.widget.ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        categories
                )
        );
        etMainCategory.setInputType(0); // disable typing, dropdown only

        // ================= DEFAULT DATE = TODAY =================
        etDate.setText(
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Calendar.getInstance().getTime())
        );

        etDate.setOnClickListener(v -> openDatePicker());
        btnSaveExpense.setOnClickListener(v -> saveExpense());
    }

    // ================= DATE PICKER =================
    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    cal.set(year, month, day);
                    selectedDateMillis = cal.getTimeInMillis();

                    etDate.setText(
                            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    .format(cal.getTime())
                    );
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ================= SAVE EXPENSE =================
    private void saveExpense() {

        String title = etExpenseTitle.getText().toString().trim();
        String amountStr = etExpenseAmount.getText().toString().trim();
        String category = etMainCategory.getText().toString().trim();

        // ---- Validation ----
        if (TextUtils.isEmpty(title) ||
                TextUtils.isEmpty(amountStr) ||
                TextUtils.isEmpty(category)) {
            toast("Fill all fields");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                toast("Amount must be > 0");
                return;
            }
        } catch (Exception e) {
            toast("Invalid amount");
            return;
        }

        // ================= Extract Month & Year from Selected Date =================
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDateMillis);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        // ================= Create Expense Object =================
        ExpenseItem expense = new ExpenseItem(
                title,
                amount,
                category,
                "",
                selectedDateMillis,
                month,
                year
        );
// 🔥 PRESERVE FIRESTORE DOCUMENT ID IN EDIT MODE
        if (isEditMode) {
            String editId = getIntent().getStringExtra("EDIT_ID");
            expense.setId(editId);
        }

        // ================= Return Result =================
        Intent result = new Intent();

        if (isEditMode) {
            result.putExtra("UPDATED_EXPENSE", expense);
        } else {
            result.putExtra("expense", expense);
        }

        setResult(RESULT_OK, result);
        finish();

    }
    // ================= TOAST =================
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
