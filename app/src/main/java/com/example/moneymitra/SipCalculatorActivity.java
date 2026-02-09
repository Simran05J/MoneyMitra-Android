package com.example.moneymitra;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class SipCalculatorActivity extends AppCompatActivity {

    // Input fields
    TextInputEditText etMonthly, etReturn, etYears;

    // Result text views
    TextView tvInvested, tvReturns, tvTotal;

    // Result card container (hidden initially)
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

        // Update header text for this calculator
        ((TextView)findViewById(R.id.tvTitle)).setText("SIP Calculator");
        ((TextView)findViewById(R.id.tvSubtitle)).setText("Calculate your SIP returns");

        // Calculate button click
        findViewById(R.id.btnCalculate).setOnClickListener(v -> calculateSIP());

        // Hide result if user edits input again
        hideResultOnEdit();
    }

    // 🔢 SIP calculation logic
    private void calculateSIP() {

        // Read input values
        String mStr = etMonthly.getText().toString().trim();
        String rStr = etReturn.getText().toString().trim();
        String yStr = etYears.getText().toString().trim();

        // Validation: empty fields
        if (mStr.isEmpty() || rStr.isEmpty() || yStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation: prevent zero investment
        if(Double.parseDouble(mStr) == 0){
            Toast.makeText(this,"Values must be greater than 0",Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert input to numbers
        double monthlyInvestment = Double.parseDouble(mStr);
        double annualRate = Double.parseDouble(rStr);
        int years = Integer.parseInt(yStr);

        // Convert annual rate to monthly rate
        double r = annualRate / 12 / 100;
        int n = years * 12; // total months

        // SIP future value formula
        double futureValue = monthlyInvestment * ((Math.pow(1 + r, n) - 1) / r) * (1 + r);

        double investedAmount = monthlyInvestment * n;
        double estimatedReturns = futureValue - investedAmount;

        // Show results with Indian number formatting
        tvInvested.setText("Invested Amount: ₹ " + String.format("%,.0f", investedAmount));
        tvReturns.setText("Estimated Returns: ₹ " + String.format("%,.0f", estimatedReturns));
        tvTotal.setText("Total Value: ₹ " + String.format("%,.0f", futureValue));

        // Make result card visible
        cardResult.setVisibility(View.VISIBLE);
    }

    //  Hide result card if user edits inputs again
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
