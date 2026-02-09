package com.example.moneymitra;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.R;
import com.example.moneymitra.adapter.CalculatorAdapter;
import com.example.moneymitra.model.CalculatorItem;

import java.util.ArrayList;
import java.util.List;

public class CalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        RecyclerView rv = findViewById(R.id.rvCalculators);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        List<CalculatorItem> list = new ArrayList<>();

        list.add(new CalculatorItem("SIP Calculator", R.drawable.ic_calculator));
        list.add(new CalculatorItem("EMI Calculator", R.drawable.ic_calculator));
        list.add(new CalculatorItem("FD Calculator", R.drawable.ic_calculator));
        list.add(new CalculatorItem("Loan Calculator", R.drawable.ic_calculator));
        list.add(new CalculatorItem("PF Calculator", R.drawable.ic_calculator));

        CalculatorAdapter adapter = new CalculatorAdapter(list);
        rv.setAdapter(adapter);


        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

    }
}
