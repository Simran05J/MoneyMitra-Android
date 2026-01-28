package com.example.moneymitra;

import com.example.moneymitra.repository.ExpenseRepository;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.data.Entry;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.moneymitra.adapter.ExpenseRecordAdapter;
import com.example.moneymitra.model.ExpenseItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpenseCategoryDetailActivity extends AppCompatActivity {
    private ExpenseRepository expenseRepo;

    /* ================= UI ================= */

    private ImageView btnBack, btnSort;
    private TextView tvCategoryTitle;
    private TextView tvSpent, tvLeft, tvBudgetAmount;
    private View layoutEditBudget;

    private RecyclerView rvExpenseRecords;
    private PieChart budgetPieChart;

    /* ================= DATA ================= */

    private String categoryName;
    private float categoryBudget;
    private float currentTotalSpent = 0f;

    private int selectedMonth;
    private int selectedYear;
    private TextView tvChartTitle;

    private TimeFilter currentFilter = TimeFilter.MONTH;

    private boolean hasVibratedForOverspend = false;

    private ActivityResultLauncher<Intent> editExpenseLauncher;

    /* ================= LIFECYCLE ================= */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_category_detail);
        expenseRepo = ExpenseRepository.getInstance();


        bindViews();
        setupPieChartStyle();

        categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        selectedMonth = getIntent().getIntExtra("MONTH", Calendar.getInstance().get(Calendar.MONTH));
        selectedYear = getIntent().getIntExtra("YEAR", Calendar.getInstance().get(Calendar.YEAR));

        tvCategoryTitle.setText(categoryName);
        categoryBudget = getSavedCategoryBudget();

        rvExpenseRecords.setLayoutManager(new LinearLayoutManager(this));
        expenseRepo.loadExpenses(list -> runOnUiThread(this::applyMonthlyFilter));

        btnBack.setOnClickListener(v -> finish());
        btnSort.setOnClickListener(v -> showSortBottomSheet());
        layoutEditBudget.setOnClickListener(v -> showBudgetBottomSheet());
        editExpenseLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                ExpenseItem updated = (ExpenseItem) result.getData().getSerializableExtra("UPDATED_EXPENSE");
                                updateExpenseInList(updated);
                            }
                        });

    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSort = findViewById(R.id.btnSort);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);

        tvSpent = findViewById(R.id.tvSpent);
        tvLeft = findViewById(R.id.tvLeft);
        tvBudgetAmount = findViewById(R.id.tvBudgetAmount);
        layoutEditBudget = findViewById(R.id.layoutEditBudget);

        rvExpenseRecords = findViewById(R.id.rvExpenseRecords);
        budgetPieChart = findViewById(R.id.budgetPieChart);
        tvChartTitle = findViewById(R.id.tvChartTitle);

    }
    /* ================= Centre Text Animation ================= */
    private void animateCenterTextChange(String text, int color) {
        budgetPieChart.animateY(300);// smooth chart bounce
        budgetPieChart.setCenterText(text);
        budgetPieChart.setCenterTextColor(color);
    }
    /* ================= over spend animation ================= */
    private void startOverspendGlow() {
        budgetPieChart.clearAnimation();

        android.view.animation.AlphaAnimation pulse =
                new android.view.animation.AlphaAnimation(0.6f, 1f);
        pulse.setDuration(800);
        pulse.setRepeatMode(android.view.animation.Animation.REVERSE);
        pulse.setRepeatCount(android.view.animation.Animation.INFINITE);

        budgetPieChart.startAnimation(pulse);
    }
    private void stopOverspendGlow() {
        budgetPieChart.clearAnimation();
    }
    private void vibrateOnce() {
        android.os.Vibrator v = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            v.vibrate(200);
        }
    }


    /* ================= FILTER ================= */

    private void applyMonthlyFilter() {
        currentFilter = TimeFilter.MONTH;

        List<ExpenseItem> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        float total = 0f;

        for (ExpenseItem item : expenseRepo.getAllExpenses()) {

            if (item.getMainCategory() == null || !item.getMainCategory().equals(categoryName)) continue;

            cal.setTimeInMillis(item.getTimestamp());
            if (cal.get(Calendar.MONTH) == selectedMonth
                    && cal.get(Calendar.YEAR) == selectedYear) {
                result.add(item);
                total += item.getAmount();
            }
        }

        currentTotalSpent = total;
        ExpenseRecordAdapter adapter = new ExpenseRecordAdapter(result);
        adapter.setOnExpenseLongClickListener(item -> showExpenseActionBottomSheet(item));
        rvExpenseRecords.setAdapter(adapter);

        updateBudgetUI();
        updateBudgetPieChart();
        updateChartTitle();
    }

    private void applyTodayFilter() {
        currentFilter = TimeFilter.TODAY;

        List<ExpenseItem> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        float total = 0f;

        for (ExpenseItem item : expenseRepo.getAllExpenses())
        {
            if (item.getMainCategory() == null || !item.getMainCategory().equals(categoryName)) continue;

            cal.setTimeInMillis(item.getTimestamp());
            if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    && cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                result.add(item);
                total += item.getAmount();
            }
        }

        currentTotalSpent = total;
        ExpenseRecordAdapter adapter = new ExpenseRecordAdapter(result);
        adapter.setOnExpenseLongClickListener(item -> showExpenseActionBottomSheet(item));
        rvExpenseRecords.setAdapter(adapter);


        updateBudgetUI();
        updateBudgetPieChart();
        updateChartTitle();
    }
    private void applyWeeklyFilter() {
        currentFilter = TimeFilter.WEEK;

        List<ExpenseItem> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        float total = 0f;

        for (ExpenseItem item : expenseRepo.getAllExpenses())
        {
            if (item.getMainCategory() == null || !item.getMainCategory().equals(categoryName)) continue;

            cal.setTimeInMillis(item.getTimestamp());
            if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    && cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)) {
                result.add(item);
                total += item.getAmount();
            }
        }

        currentTotalSpent = total;
        ExpenseRecordAdapter adapter = new ExpenseRecordAdapter(result, item -> {

            Intent i = new Intent(this, AddExpenseActivity.class);
            i.putExtra("MODE", "EDIT");
            i.putExtra("EDIT_TITLE", item.getTitle());
            i.putExtra("EDIT_AMOUNT", item.getAmount());
            i.putExtra("EDIT_TIMESTAMP", item.getTimestamp());
            i.putExtra("EDIT_CATEGORY", item.getMainCategory());

            editExpenseLauncher.launch(i);

        });

        adapter.setOnExpenseLongClickListener(item -> showExpenseActionBottomSheet(item));

        rvExpenseRecords.setAdapter(adapter);



        updateBudgetUI();
        updateBudgetPieChart();
        updateChartTitle();
    }



    /* ================= PIE CHART ================= */


    private void setupPieChartStyle() {
        budgetPieChart.setDrawHoleEnabled(true);
        budgetPieChart.setHoleRadius(70f);
        budgetPieChart.setTransparentCircleRadius(0f);
        budgetPieChart.setHoleColor(Color.BLACK);
        budgetPieChart.setDrawEntryLabels(false);
        budgetPieChart.setRotationEnabled(false);
        budgetPieChart.getLegend().setEnabled(false);

        Description d = new Description();
        d.setText("");
        budgetPieChart.setDescription(d);

        budgetPieChart.setDrawCenterText(true);
        budgetPieChart.setCenterTextColor(Color.WHITE);
        budgetPieChart.setCenterTextSize(14f);

        budgetPieChart.setExtraOffsets(0f, 0f, 0f, 0f);

    }
    private void updateChartTitle() {
        switch (currentFilter) {
            case TODAY:
                tvChartTitle.setText("Spent today");
                break;
            case WEEK:
                tvChartTitle.setText("Spent this week");
                break;
            case MONTH:
                tvChartTitle.setText("Spent this month");
                break;
        }
        tvChartTitle.setVisibility(View.VISIBLE);
    }



    private void updateBudgetPieChart() {

        // 🔴 EMPTY STATE
        if (categoryBudget <= 0 && currentTotalSpent <= 0) {
            budgetPieChart.setVisibility(View.GONE);
            tvChartTitle.setVisibility(View.VISIBLE);
            tvChartTitle.setText(
                    "Nothing spent in " + categoryName + " " +
                            (currentFilter == TimeFilter.TODAY ? "today" :
                                    currentFilter == TimeFilter.WEEK ? "this week" :
                                            "this month")
            );
            return;

        }

        // 🟢 NORMAL STATE
        budgetPieChart.setVisibility(View.VISIBLE);
        tvChartTitle.setVisibility(View.VISIBLE);

        List<PieEntry> entries = new ArrayList<>();

        if (categoryBudget <= 0) {
            budgetPieChart.setVisibility(View.VISIBLE);
            animateCenterTextChange("No budget set", Color.GRAY);
            return;
        }

        float used = Math.min(currentTotalSpent, categoryBudget);
        float remainingRing = Math.max(categoryBudget - currentTotalSpent, 0);

        entries.add(new PieEntry(used, "Used"));
        entries.add(new PieEntry(remainingRing, "Remaining"));


        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawValues(false);

        if (currentTotalSpent > categoryBudget) {
            dataSet.setColors(
                    Color.RED,
                    Color.parseColor("#2C2C2C")
            );
        } else {
            dataSet.setColors(
                    Color.parseColor("#6FF2E7"),
                    Color.parseColor("#2C2C2C")
            );
        }

        dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        budgetPieChart.setData(data);

        float remaining = categoryBudget - currentTotalSpent;
        final float percentUsed = Math.min((currentTotalSpent / categoryBudget) * 100f, 100f);

        if (remaining >= 0) {
            stopOverspendGlow();
            hasVibratedForOverspend = false; // reset when safe again

            animateCenterTextChange(
                    "₹" + (int) remaining + " Remaining\n" + (int) percentUsed + "% used",
                    Color.WHITE
            );

        } else {
            startOverspendGlow();

            if (!hasVibratedForOverspend) {
                vibrateOnce();
                hasVibratedForOverspend = true;
            }

            animateCenterTextChange(
                    "₹" + (int) Math.abs(remaining) + " Overspent\n100% used",
                    Color.RED
            );
        }




        budgetPieChart.setOnChartValueSelectedListener(
                new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {

                        PieEntry pe = (PieEntry) e;

                        if (pe.getLabel().equals("Used")) {
                            animateCenterTextChange(
                                    "₹" + (int) currentTotalSpent + "\nUsed",
                                    Color.WHITE
                            );
                        } else {
                            animateCenterTextChange(
                                    "₹" + (int) categoryBudget + "\nBudget",
                                    Color.WHITE
                            );
                        }

                    }

                    @Override
                    public void onNothingSelected() {
                        float remaining = categoryBudget - currentTotalSpent;

                        if (remaining >= 0) {
                            animateCenterTextChange(
                                    "₹" + (int) remaining + " Remaining\n" + (int) percentUsed + "% used",
                                    Color.WHITE
                            );
                        } else {
                            animateCenterTextChange(
                                    "₹" + (int) Math.abs(remaining) + " Overspent\n100% used",
                                    Color.RED
                            );
                        }

                    }

                }
        );

        budgetPieChart.invalidate();
    }

    /* ================= BUDGET UI ================= */

    private void updateBudgetUI() {

        tvSpent.setText("₹" + (int) currentTotalSpent);
        tvBudgetAmount.setText("₹" + (int) categoryBudget);

        float remaining = categoryBudget - currentTotalSpent;
        tvLeft.setText("₹" + (int) remaining);

        if (remaining <= 0) {
            tvLeft.setTextColor(getColor(R.color.budget_over));
        } else if (categoryBudget > 0 && remaining / categoryBudget < 0.4f) {
            tvLeft.setTextColor(getColor(R.color.budget_warning));
        } else {
            tvLeft.setTextColor(getColor(R.color.budget_safe));
        }
    }

    /* ================= SORT SHEET ================= */

    private void showSortBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_sort_expense, null);

        view.findViewById(R.id.optionToday)
                .setOnClickListener(v -> {
                    applyTodayFilter();
                    dialog.dismiss();
                });

        view.findViewById(R.id.optionWeek)
                .setOnClickListener(v -> {
                    applyWeeklyFilter();
                    dialog.dismiss();
                });

        view.findViewById(R.id.optionMonth)
                .setOnClickListener(v -> {
                    applyMonthlyFilter();
                    dialog.dismiss();
                });


        dialog.setContentView(view);
        dialog.show();
    }

    /* ================= EDIT BUDGET ================= */

    private void showBudgetBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater()
                .inflate(R.layout.bottom_sheet_set_budget, null);

        TextInputEditText etAmount = view.findViewById(R.id.etBudgetAmount);
        Button btnSave = view.findViewById(R.id.btnSaveBudget);

        etAmount.setText(String.valueOf((int) categoryBudget));

        btnSave.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.press_scale));
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.release_scale));
            }
            return false;
        });

        btnSave.setOnClickListener(v -> {
            if (etAmount.getText() == null || etAmount.getText().toString().isEmpty()) return;

            categoryBudget = Float.parseFloat(etAmount.getText().toString());
            saveCategoryBudget(categoryBudget);

            Intent result = new Intent();
            result.putExtra("CATEGORY_NAME", categoryName);
            result.putExtra("UPDATED_BUDGET", categoryBudget);
            setResult(RESULT_OK, result);

            dialog.dismiss();
            finish();
        });

        dialog.setContentView(view);
        dialog.show();
    }
    //============actions==============//
    private void showExpenseActionBottomSheet(ExpenseItem item) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_expense_actions, null);
        dialog.setContentView(view);

        TextView tvEdit = view.findViewById(R.id.tvEditExpense);
        TextView tvDelete = view.findViewById(R.id.tvDeleteExpense);

        tvEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, AddExpenseActivity.class);
            i.putExtra("MODE", "EDIT");
            i.putExtra("EDIT_TITLE", item.getTitle());
            i.putExtra("EDIT_AMOUNT", item.getAmount());
            i.putExtra("EDIT_TIMESTAMP", item.getTimestamp());
            i.putExtra("EDIT_CATEGORY", item.getMainCategory());
            i.putExtra("EDIT_ID", item.getId()); // 🔥 IMPORTANT


            editExpenseLauncher.launch(i);
            dialog.dismiss();
        });

        tvDelete.setOnClickListener(v -> {
            expenseRepo.deleteExpense(item);
            expenseRepo.getAllExpenses().remove(item);
            applyMonthlyFilter();
            setResult(RESULT_OK);
            Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }


    /* ================= STORAGE ================= */

    private float getSavedCategoryBudget() {
        String key = categoryName + "_" + selectedMonth + "_" + selectedYear;
        return getSharedPreferences("category_budgets", MODE_PRIVATE)
                .getFloat(key, 0f);
    }
    private void saveCategoryBudget(float amount) {
        String key = categoryName + "_" + selectedMonth + "_" + selectedYear;
        getSharedPreferences("category_budgets", MODE_PRIVATE)
                .edit()
                .putFloat(key, amount)
                .apply();
    }

    private void updateExpenseInList(ExpenseItem updated) {

        expenseRepo.updateExpense(updated); // Firestore update

        // 🔥 Update local list
        for (int i = 0; i < expenseRepo.getAllExpenses().size(); i++) {
            if (expenseRepo.getAllExpenses().get(i).getId().equals(updated.getId())) {
                expenseRepo.getAllExpenses().set(i, updated);
                break;
            }
        }

        applyMonthlyFilter();
        setResult(RESULT_OK);
    }




    private enum TimeFilter {
        TODAY,
        WEEK,
        MONTH
    }

}
