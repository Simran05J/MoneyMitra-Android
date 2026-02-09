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

public class EmiCalculatorActivity extends AppCompatActivity {

    // Input fields
    TextInputEditText etMonthly, etReturn, etYears;

    // Result views
    TextView tvInvested, tvReturns, tvTotal;
    View cardResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_calculator);

        // Back button behaviour
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Bind input fields
        etMonthly = findViewById(R.id.etMonthly);
        etReturn = findViewById(R.id.etReturn);
        etYears = findViewById(R.id.etYears);

        // Bind result views
        tvInvested = findViewById(R.id.tvInvested);
        tvReturns = findViewById(R.id.tvReturns);
        tvTotal = findViewById(R.id.tvTotal);
        cardResult = findViewById(R.id.cardResult);

        // Change header text dynamically
        ((TextView)findViewById(R.id.tvTitle)).setText("EMI Calculator");
        ((TextView)findViewById(R.id.tvSubtitle)).setText("Calculate your monthly EMI");

        // Access TextInputLayout parents to change hints
        TextInputLayout til1 = (TextInputLayout) ((View) etMonthly.getParent()).getParent();
        TextInputLayout til2 = (TextInputLayout) ((View) etReturn.getParent()).getParent();
        TextInputLayout til3 = (TextInputLayout) ((View) etYears.getParent()).getParent();

        til1.setHint("Loan Amount (₹)");
        til2.setHint("Interest Rate (%)");
        til3.setHint("Loan Tenure (Years)");

        // Hide result if user edits input again
        hideResultOnEdit();

        // Calculate button click
        findViewById(R.id.btnCalculate).setOnClickListener(v -> calculateEMI());
    }

    // 🔢 EMI Calculation Logic
    private void calculateEMI() {

        String pStr = etMonthly.getText().toString().trim();
        String rStr = etReturn.getText().toString().trim();
        String yStr = etYears.getText().toString().trim();

        // Validation: empty fields
        if (pStr.isEmpty() || rStr.isEmpty() || yStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation: prevent zero interest rate
        if(Double.parseDouble(rStr) == 0){
            Toast.makeText(this,"Values must be greater than 0",Toast.LENGTH_SHORT).show();
            return;
        }

        double principal = Double.parseDouble(pStr);
        double annualRate = Double.parseDouble(rStr);
        int years = Integer.parseInt(yStr);

        // Convert to monthly rate and months
        double r = annualRate / 12 / 100;
        int n = years * 12;

        // EMI formula
        double emi = (principal * r * Math.pow(1 + r, n)) /
                (Math.pow(1 + r, n) - 1);

        double totalPayment = emi * n;
        double totalInterest = totalPayment - principal;

        // Show results with comma formatting
        tvInvested.setText("Monthly EMI: ₹ " + String.format("%,.0f", emi));
        tvReturns.setText("Total Interest: ₹ " + String.format("%,.0f", totalInterest));
        tvTotal.setText("Total Payment: ₹ " + String.format("%,.0f", totalPayment));

        // Show result card
        cardResult.setVisibility(View.VISIBLE);
    }

    // 👀 Hide result when inputs change
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
