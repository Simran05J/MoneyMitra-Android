package com.example.moneymitra;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoanCalculatorActivity extends AppCompatActivity {

    // Input fields
    TextInputEditText etMonthly, etReturn, etYears;

    // Result views
    TextView tvInvested, tvReturns, tvTotal;
    View cardResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_calculator);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Bind inputs
        etMonthly = findViewById(R.id.etMonthly);
        etReturn = findViewById(R.id.etReturn);
        etYears = findViewById(R.id.etYears);

        // Bind result views
        tvInvested = findViewById(R.id.tvInvested);
        tvReturns = findViewById(R.id.tvReturns);
        tvTotal = findViewById(R.id.tvTotal);
        cardResult = findViewById(R.id.cardResult);

        // Change header text dynamically
        ((TextView)findViewById(R.id.tvTitle)).setText("Loan Calculator");
        ((TextView)findViewById(R.id.tvSubtitle)).setText("Estimate your loan repayment");

        // Change input hints dynamically
        TextInputLayout til1 = (TextInputLayout) ((View) etMonthly.getParent()).getParent();
        TextInputLayout til2 = (TextInputLayout) ((View) etReturn.getParent()).getParent();
        TextInputLayout til3 = (TextInputLayout) ((View) etYears.getParent()).getParent();

        til1.setHint("Loan Amount (₹)");
        til2.setHint("Interest Rate (%)");
        til3.setHint("Duration (Years)");

        // Hide result when user edits input
        hideResultOnEdit();

        // Calculate button click
        findViewById(R.id.btnCalculate).setOnClickListener(v -> calculateLoan());
    }

    // 🔢 Simple Interest Loan Calculation
    private void calculateLoan() {

        String pStr = etMonthly.getText().toString().trim();
        String rStr = etReturn.getText().toString().trim();
        String tStr = etYears.getText().toString().trim();

        // Validation
        if (pStr.isEmpty() || rStr.isEmpty() || tStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Double.parseDouble(rStr) == 0){
            Toast.makeText(this,"Values must be greater than 0",Toast.LENGTH_SHORT).show();
            return;
        }

        double principal = Double.parseDouble(pStr);
        double rate = Double.parseDouble(rStr);
        int years = Integer.parseInt(tStr);

        // Simple Interest formula
        double interest = (principal * rate * years) / 100;
        double totalPayable = principal + interest;

        // Show results with comma formatting
        tvInvested.setText("Principal: ₹ " + String.format("%,.0f", principal));
        tvReturns.setText("Total Interest: ₹ " + String.format("%,.0f", interest));
        tvTotal.setText("Total Payable: ₹ " + String.format("%,.0f", totalPayable));

        cardResult.setVisibility(View.VISIBLE);
    }

    // 👀 Hide result if user edits inputs
    private void hideResultOnEdit() {
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){
                cardResult.setVisibility(View.GONE);
            }
            public void afterTextChanged(Editable s){}
        };

        etMonthly.addTextChangedListener(watcher);
        etReturn.addTextChangedListener(watcher);
        etYears.addTextChangedListener(watcher);
    }
}
