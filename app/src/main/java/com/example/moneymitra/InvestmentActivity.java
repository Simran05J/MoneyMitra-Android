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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.adapter.InvestmentAdapter;
import com.example.moneymitra.model.InvestmentItem;
import com.example.moneymitra.repository.InvestmentRepository;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InvestmentActivity extends AppCompatActivity {

    // ================= COLORS =================
    private final int[] investmentColors = {
            Color.parseColor("#7CF7E8"),
            Color.parseColor("#C77DFF"),
            Color.parseColor("#6F8BFF"),
            Color.parseColor("#51D928"),
            Color.parseColor("#D37CF7"),
            Color.parseColor("#FFB703")
    };

    // ================= VIEWS =================
    private TextView txtTitle, txtSubtitle;
    private TextView txtTotalAmount, txtTotalLabel;
    private PieChart pieChart;
    private RecyclerView rvInvestments;
    private View btnAddInvestment, layoutSummary;
    private TextView tvEmptyState;

    // ================= DATA =================
    private final List<InvestmentItem> investmentList = new ArrayList<>();
    private InvestmentAdapter adapter;
    private InvestmentRepository repository;
    private int totalAmount = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_investment);

        repository = new InvestmentRepository();

        applyWindowInsets();
        initViews();
        setupHeaderText();
        setupRecycler();
        setupChart();
        setupFab();

        fetchInvestments();
    }

    // ================= INIT =================
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

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

        txtTotalAmount.setVisibility(View.GONE);
        txtTotalLabel.setVisibility(View.GONE);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

    }

    private void setupHeaderText() {
        SpannableString title = new SpannableString("Investment Tracker");
        title.setSpan(new StyleSpan(Typeface.BOLD), 11, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtTitle.setText(title);
        SpannableString subtitle =
                new SpannableString("Here Is Your Investment Report");

        subtitle.setSpan(
                new StyleSpan(Typeface.BOLD),
                12, // start of "Investment"
                22, // end of "Investment"
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        txtSubtitle.setText(subtitle);

    }

    // ================= RECYCLER =================
    private void setupRecycler() {
        adapter = new InvestmentAdapter(
                investmentList,
                pos -> {
                    highlightItem(pos);
                    highlightChartSlice(pos);
                    adapter.setSelectedPosition(pos);
                },
                this::showBottomSheet
        );
        rvInvestments.setLayoutManager(new LinearLayoutManager(this));
        rvInvestments.setAdapter(adapter);
    }


    // ================= CHART =================
    private void setupChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(72f);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setHoleColor(Color.BLACK);
        pieChart.setDrawCenterText(true);

        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(true);

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setNoDataText("");

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) h.getX();
                if (index >= 0 && index < investmentList.size()) {

                    pieChart.setDrawCenterText(false);

                    highlightItem(index);
                    adapter.setSelectedPosition(index);
                    rvInvestments.post(() -> rvInvestments.smoothScrollToPosition(index));

                }
            }



            @Override
            public void onNothingSelected() {


                pieChart.setDrawCenterText(true);
                restoreDefaultState();

                adapter.setSelectedPosition(-1);
            }

        });
    }

    private void updateChart() {
        if (investmentList.isEmpty()) {
            pieChart.clear();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (InvestmentItem item : investmentList) {
            entries.add(new PieEntry(item.getAmount()));
            colors.add(item.getColor());
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(2f);

        // 🔥 click pop-out animation
        dataSet.setSelectionShift(7f);

        pieChart.setData(new PieData(dataSet));
        pieChart.animateY(600);   // smooth but not flashy

        pieChart.invalidate();
    }

    // ================= FIREBASE =================
    private void fetchInvestments() {
        repository.fetchInvestments(snapshot -> {
            investmentList.clear();
            int colorIndex = 0;
            totalAmount = 0;

            for (DocumentSnapshot doc : snapshot) {
                InvestmentItem item = doc.toObject(InvestmentItem.class);
                if (item == null) continue;

                item.setId(doc.getId());
                if (item.getColor() == 0) {
                    item.setColor(investmentColors[colorIndex++ % investmentColors.length]);
                }

                totalAmount += item.getAmount();
                investmentList.add(item);
            }

            adapter.notifyDataSetChanged();
            updateChart();
            restoreDefaultState();
            updateEmptyState();

        }, e -> Toast.makeText(this, "Failed to load investments", Toast.LENGTH_SHORT).show());
    }

    // ================= UI STATE =================
    private void restoreDefaultState() {
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("₹" + totalAmount + "\nTotal Investment");
        pieChart.setCenterTextSize(20f); // 🔥 BIGGER
        pieChart.setCenterTextColor(Color.WHITE);

        txtTotalAmount.setVisibility(View.GONE);
        txtTotalLabel.setVisibility(View.GONE);
    }

    private void highlightItem(int pos) {

        pieChart.setDrawCenterText(false);
        InvestmentItem item = investmentList.get(pos);

        txtTotalAmount.setText("₹" + item.getAmount());
        txtTotalLabel.setText(item.getName());

        txtTotalAmount.setTextSize(15f);
        txtTotalLabel.setTextSize(12f);

        txtTotalAmount.setVisibility(View.VISIBLE);
        txtTotalLabel.setVisibility(View.VISIBLE);

    }
    private void highlightChartSlice(int pos) {
        if (pos >= 0 && pos < investmentList.size()) {
            pieChart.highlightValue(pos, 0, false);
        }
    }



    private void updateEmptyState() {
        boolean empty = investmentList.isEmpty();
        tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvInvestments.setVisibility(empty ? View.GONE : View.VISIBLE);
        pieChart.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutSummary.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ================= BOTTOM SHEET =================
    private void showBottomSheet(int pos) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_investment_actions, null);

        view.findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddInvestmentActivity.class);
            intent.putExtra("MODE", "EDIT");
            intent.putExtra("INVESTMENT_ID", investmentList.get(pos).getId());
            intent.putExtra("NAME", investmentList.get(pos).getName());
            intent.putExtra("AMOUNT", investmentList.get(pos).getAmount());
            intent.putExtra("COLOR", investmentList.get(pos).getColor());
            intent.putExtra("GOAL", investmentList.get(pos).getGoal());
            startActivityForResult(intent, 101);
            dialog.dismiss();
        });

        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            repository.deleteInvestment(investmentList.get(pos).getId())
                    .addOnSuccessListener(a -> fetchInvestments());
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    // ================= FAB =================
    private void setupFab() {
        btnAddInvestment.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                startActivityForResult(new Intent(this, AddInvestmentActivity.class), 101);
            }
            return true;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            fetchInvestments();
        }
    }
}
