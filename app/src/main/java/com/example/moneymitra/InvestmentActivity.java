package com.example.moneymitra;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.adapter.InvestmentAdapter;
import com.example.moneymitra.model.InvestmentItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class InvestmentActivity extends AppCompatActivity {

    // ================= CONSTANT =================
    private static final int ADD_INVESTMENT_REQUEST = 101;

    // ================= COLOR PALETTE =================
    private final int[] investmentColors = {
            Color.parseColor("#7CF7E8"),
            Color.parseColor("#C77DFF"),
            Color.parseColor("#6F8BFF"),
            Color.parseColor("#51D928"),
            Color.parseColor("#D37CF7"),
            Color.parseColor("#FFB703"),
            Color.parseColor("#FB7185"),
            Color.parseColor("#38BDF8"),
            Color.parseColor("#A3E635"),
            Color.parseColor("#F472B6")
    };
    private int colorIndex = 0;

    // ================= VIEWS =================
    private TextView txtTitle, txtSubtitle;
    private TextView txtTotalAmount, txtTotalLabel;
    private PieChart pieChart;
    private RecyclerView rvInvestments;
    private View btnAddInvestment;
    private View layoutSummary;
    private TextView tvEmptyState;

    // ================= DATA =================
    private InvestmentAdapter investmentAdapter;
    private final List<InvestmentItem> investmentList = new ArrayList<>();
    private int selectedLongPressPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_investment);

        applyWindowInsets();
        initViews();

        setupHeaderText();
        setupDonutChart();
        setupRecyclerView();
        setupChartClickListener();
        setupFab();
        updateEmptyState();
    }

    // ================= WINDOW INSETS =================
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    // ================= INIT =================
    private void initViews() {
        txtTitle = findViewById(R.id.txtTitle);
        txtSubtitle = findViewById(R.id.txtSubtitle);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        txtTotalLabel = findViewById(R.id.txtTotalLabel);
        pieChart = findViewById(R.id.pieChart);
        rvInvestments = findViewById(R.id.rvInvestments);
        btnAddInvestment = findViewById(R.id.btnAddInvestment);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        layoutSummary = findViewById(R.id.layoutSummary);
    }

    // ================= HEADER =================
    private void setupHeaderText() {
        SpannableString title = new SpannableString("Investment Tracker");
        title.setSpan(new StyleSpan(Typeface.BOLD), 11, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtTitle.setText(title);

        SpannableString subtitle = new SpannableString("Here Is Your Investment Report");
        subtitle.setSpan(new StyleSpan(Typeface.BOLD), 8, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtSubtitle.setText(subtitle);
    }

    // ================= PIE CHART =================
    private void setupDonutChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(70f);
        pieChart.setHoleColor(Color.BLACK);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.getLegend().setEnabled(false);
    }

    private void updateDonutChart() {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (InvestmentItem item : investmentList) {
            entries.add(new PieEntry(item.getAmount(), item.getName()));
            colors.add(item.getColor());
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        pieChart.setData(new PieData(dataSet));
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }

    private void updateTotalAmount() {
        int total = 0;
        for (InvestmentItem item : investmentList) total += item.getAmount();
        txtTotalAmount.setText("₹" + total);
        txtTotalLabel.setText("Total Investment");
    }

    // ================= RECYCLER =================
    private void setupRecyclerView() {
        investmentAdapter = new InvestmentAdapter(
                investmentList,
                this::onItemClicked,
                this::onItemLongPressed
        );
        rvInvestments.setLayoutManager(new LinearLayoutManager(this));
        rvInvestments.setAdapter(investmentAdapter);
    }

    private void updateEmptyState() {
        boolean empty = investmentList.isEmpty();
        tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvInvestments.setVisibility(empty ? View.GONE : View.VISIBLE);
        pieChart.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutSummary.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ================= LIST ↔ CHART =================
    private void onItemClicked(int position) {
        InvestmentItem item = investmentList.get(position);
        txtTotalAmount.setText("₹" + item.getAmount());
        txtTotalLabel.setText(item.getName());
        pieChart.highlightValue(position, 0);
    }

    private void setupChartClickListener() {
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    PieEntry entry = (PieEntry) e;
                    txtTotalAmount.setText("₹" + (int) entry.getValue());
                    txtTotalLabel.setText(entry.getLabel());
                }
            }

            @Override
            public void onNothingSelected() {
                updateTotalAmount();
            }
        });
    }

    // ================= LONG PRESS =================
    private void onItemLongPressed(int position) {
        selectedLongPressPosition = position;
        showBottomSheet();
    }

    private void showBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(
                R.layout.bottom_sheet_investment_actions, null);

        view.findViewById(R.id.btnEdit).setOnClickListener(v -> {
            openEditInvestment();
            dialog.dismiss();
        });

        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            deleteInvestment();
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void openEditInvestment() {
        if (selectedLongPressPosition < 0) return;

        InvestmentItem item = investmentList.get(selectedLongPressPosition);

        Intent intent = new Intent(this, AddInvestmentActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("position", selectedLongPressPosition);
        intent.putExtra("investment_name", item.getName());
        intent.putExtra("investment_amount", item.getAmount());

        startActivityForResult(intent, ADD_INVESTMENT_REQUEST);
    }

    private void deleteInvestment() {
        if (selectedLongPressPosition < 0) return;

        investmentList.remove(selectedLongPressPosition);
        investmentAdapter.notifyItemRemoved(selectedLongPressPosition);
        selectedLongPressPosition = -1;

        updateTotalAmount();
        updateDonutChart();
        updateEmptyState();
    }

    // ================= FAB =================
    private void setupFab() {
        btnAddInvestment.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.press_scale));
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.release_scale));
                startActivityForResult(
                        new Intent(this, AddInvestmentActivity.class),
                        ADD_INVESTMENT_REQUEST
                );
            }
            return true;
        });
    }

    // ================= RESULT =================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != ADD_INVESTMENT_REQUEST || resultCode != RESULT_OK || data == null)
            return;

        String name = data.getStringExtra("investment_name");
        int amount = data.getIntExtra("investment_amount", 0);
        String mode = data.getStringExtra("mode");

        if (name == null || amount <= 0) return;

        if ("edit".equals(mode)) {
            int position = data.getIntExtra("position", -1);
            if (position < 0) return;

            InvestmentItem item = investmentList.get(position);
            item.setName(name);
            item.setAmount(amount);

            investmentAdapter.notifyItemChanged(position);
        } else {
            int color = investmentColors[colorIndex % investmentColors.length];
            colorIndex++;

            investmentList.add(new InvestmentItem(name, amount, color));
            investmentAdapter.notifyItemInserted(investmentList.size() - 1);
        }

        updateTotalAmount();
        updateDonutChart();
        updateEmptyState();
    }
}
