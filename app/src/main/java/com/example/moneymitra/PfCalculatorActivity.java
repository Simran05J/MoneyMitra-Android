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

public class PfCalculatorActivity extends AppCompatActivity {

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
        ((TextView)findViewById(R.id.tvTitle)).setText("PF Calculator");
        ((TextView)findViewById(R.id.tvSubtitle)).setText("Estimate your PF savings");

        // Change input hints dynamically
        TextInputLayout til1 = (TextInputLayout) ((View) etMonthly.getParent()).getParent();
        TextInputLayout til2 = (TextInputLayout) ((View) etReturn.getParent()).getParent();
        TextInputLayout til3 = (TextInputLayout) ((View) etYears.getParent()).getParent();

        til1.setHint("Monthly Contribution (₹)");
        til2.setHint("Interest Rate (%)");
        til3.setHint("Years");

        // Hide result when user edits inputs
        hideResultOnEdit();

        // Calculate button click
        findViewById(R.id.btnCalculate).setOnClickListener(v -> calculatePF());
    }

    // 🔢 PF Future Value Calculation (monthly contribution)
    private void calculatePF() {

        String mStr = etMonthly.getText().toString().trim();
        String rStr = etReturn.getText().toString().trim();
        String yStr = etYears.getText().toString().trim();

        // Validation
        if (mStr.isEmpty() || rStr.isEmpty() || yStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Double.parseDouble(rStr) == 0){
            Toast.makeText(this,"Values must be greater than 0",Toast.LENGTH_SHORT).show();
            return;
        }

        double monthlyContribution = Double.parseDouble(mStr);
        double annualRate = Double.parseDouble(rStr);
        int years = Integer.parseInt(yStr);

        // Convert to monthly rate and months
        double r = annualRate / 12 / 100;
        int n = years * 12;

        // Future value formula (same as SIP)
        double maturity = monthlyContribution * ((Math.pow(1 + r, n) - 1) / r) * (1 + r);
        double contribution = monthlyContribution * n;
        double interest = maturity - contribution;

        // Show results with comma formatting
        tvInvested.setText("Total Contribution: ₹ " + String.format("%,.0f", contribution));
        tvReturns.setText("Interest Earned: ₹ " + String.format("%,.0f", interest));
        tvTotal.setText("Maturity Amount: ₹ " + String.format("%,.0f", maturity));

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
