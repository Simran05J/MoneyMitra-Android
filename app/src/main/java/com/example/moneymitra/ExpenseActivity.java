package com.example.moneymitra;
import com.example.moneymitra.repository.ExpenseRepository;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.adapter.ExpenseAdapter;
import com.example.moneymitra.category.MainCategories;
import com.example.moneymitra.model.CategoryExpenseItem;
import com.example.moneymitra.model.ExpenseItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseActivity extends AppCompatActivity {

    /* ===================== DATA ===================== */

    private ExpenseRepository expenseRepo;


    /* ===================== UI ===================== */

    private RecyclerView rvExpenses;
    private PieChart expenseDonutChart;
    private TextView tvEmptyExpenses;
    private TextView tvMonthLabel;
    private TextView tvMonthAmount;
    private TextView tvMonthBudget;
    private TextView tvMonthBalance;
    private TextView tvBudgetWarning;

    private View btnPrevMonth;
    private View btnNextMonth;

    /* ===================== STATE ===================== */

    private float totalExpense = 0f;
    private float currentMonthBudget = 0f;

    private int selectedMonth;
    private int selectedYear;

    private TimeFilter currentFilter = TimeFilter.MONTH;
    private ActivityResultLauncher<Intent> editBudgetLauncher;
    private ActivityResultLauncher<Intent> categoryDetailLauncher;
    private ActivityResultLauncher<Intent> addExpenseLauncher;
    /* ===================== CATEGORY COLORS ===================== */

    private static final Map<String, Integer> CATEGORY_COLOR_MAP =
            new HashMap<String, Integer>() {{
                put(MainCategories.HOME_UTILITIES, Color.parseColor("#6FF2E7"));
                put(MainCategories.TRANSPORTATION, Color.parseColor("#D37CF7"));
                put(MainCategories.FOOD, Color.parseColor("#51D928"));
                put(MainCategories.HEALTH, Color.parseColor("#FFB703"));
                put(MainCategories.LIFESTYLE, Color.parseColor("#FF6F61"));
                put(MainCategories.OTHER, Color.parseColor("#4D96FF"));
            }};

    /* ===================== LIFECYCLE ===================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        expenseRepo = ExpenseRepository.getInstance();

// 🔥 LOAD EXPENSES FROM FIREBASE
        expenseRepo.loadExpenses(list -> runOnUiThread(this::refreshUI));


        Calendar now = Calendar.getInstance();
        selectedMonth = now.get(Calendar.MONTH);
        selectedYear = now.get(Calendar.YEAR);

        tvMonthLabel = findViewById(R.id.tvSelectedMonth);
        tvMonthAmount = findViewById(R.id.tvMonthAmount);
        tvMonthBudget = findViewById(R.id.tvMonthBudget);
        tvMonthBalance = findViewById(R.id.tvMonthBalance);
        tvBudgetWarning = findViewById(R.id.tvBudgetWarning);

        expenseDonutChart = findViewById(R.id.expenseDonutChart);
        rvExpenses = findViewById(R.id.rvExpenses);
        tvEmptyExpenses = findViewById(R.id.tvEmptyExpenses);

        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        setupDonutChartStyle();

        btnPrevMonth.setOnClickListener(v -> {
            moveToPreviousMonth();
            refreshUI();
        });

        btnNextMonth.setOnClickListener(v -> {
            if (currentFilter == TimeFilter.MONTH) {
                moveToNextMonth();
                refreshUI();
            }
        });

        tvMonthLabel.setOnClickListener(v -> showTimeFilterSheet());
        findViewById(R.id.layoutMonthBudget)
                .setOnClickListener(v -> showBudgetBottomSheet());


        addExpenseLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                ExpenseItem expense =
                                        (ExpenseItem) result.getData().getSerializableExtra("expense");
                                if (expense != null) {
                                    expenseRepo.addExpense(expense);
                                    // reload from Firebase after save
                                    expenseRepo.loadExpenses(list -> runOnUiThread(this::refreshUI));

                                }
                            }
                        });

        FloatingActionButton fab = findViewById(R.id.fabAddExpense);
        fab.setOnClickListener(v ->
                addExpenseLauncher.launch(new Intent(this, AddExpenseActivity.class)));
        /* 🔥 EDIT BUDGET RESULT */
        editBudgetLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                recomputeMonthlyBudget();
                                refreshUI();
                            }
                        });


        categoryDetailLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                refreshUI();
                            }
                        });

        checkMonthChange();
        refreshUI();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);

            } else {
                NotificationScheduler.scheduleDailyReminder(this);
            }

        } else {
            NotificationScheduler.scheduleDailyReminder(this);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            NotificationScheduler.scheduleDailyReminder(this);
        }
    }
    private boolean isBudgetNotificationShown(String monthKey) {
        SharedPreferences prefs = getSharedPreferences("budget_notify", MODE_PRIVATE);
        return prefs.getBoolean(monthKey, false);
    }

    private void markBudgetNotificationShown(String monthKey) {
        SharedPreferences prefs = getSharedPreferences("budget_notify", MODE_PRIVATE);
        prefs.edit().putBoolean(monthKey, true).apply();
    }


    private void recomputeMonthlyBudget() {
        currentMonthBudget = 0f;
        for (String c : MainCategories.getAll()) {
            currentMonthBudget += getCategoryBudget(c);
        }
    }

    /* ===================== BUDGET BOTTOM SHEET ===================== */

    private void showBudgetBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.expense_set_budget, null);

        MaterialAutoCompleteTextView actCategory = view.findViewById(R.id.actCategory);
        EditText etAmount = view.findViewById(R.id.etCategoryBudget);
        TextView btnSave = view.findViewById(R.id.btnSaveBudget);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        MainCategories.getAll());

        actCategory.setAdapter(adapter);
        actCategory.setOnClickListener(v -> actCategory.showDropDown());

        btnSave.setOnClickListener(v -> {

            String category = actCategory.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            if (category.isEmpty()) {
                actCategory.setError("Select category");
                return;
            }
            if (amountStr.isEmpty()) {
                etAmount.setError("Enter amount");
                return;
            }

            float amount = Float.parseFloat(amountStr);
            saveCategoryBudget(category, amount);


            currentMonthBudget = 0f;
            for (String c : MainCategories.getAll()) {
                currentMonthBudget += getCategoryBudget(c);
            }


            dialog.dismiss();
            refreshUI();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    // montly budget reset logic
    private void checkMonthChange() {
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);

        int savedMonth = getSharedPreferences("app_state", MODE_PRIVATE)
                .getInt("last_month", -1);
        int savedYear = getSharedPreferences("app_state", MODE_PRIVATE)
                .getInt("last_year", -1);

        if (savedMonth != currentMonth || savedYear != currentYear) {
            onNewMonthStarted(currentMonth, currentYear);
        }

        getSharedPreferences("app_state", MODE_PRIVATE).edit()
                .putInt("last_month", currentMonth)
                .putInt("last_year", currentYear)
                .apply();
    }
    private void onNewMonthStarted(int month, int year) {
        selectedMonth = month;
        selectedYear = year;

        Toast.makeText(this, "New month started 🌙", Toast.LENGTH_SHORT).show();

        refreshUI(); // UI resets automatically
    }
    /* ===================== UI REFRESH ===================== */
    private void refreshUI() {
        recomputeMonthlyBudget();
        List<ExpenseItem> visibleExpenses = getFilteredExpenses();

        Map<String, Float> categoryTotals = new HashMap<>();
        for (String c : MainCategories.getAll()) categoryTotals.put(c, 0f);

        totalExpense = 0f;

        for (ExpenseItem item : visibleExpenses) {
            float amt = (float) item.getAmount();
            totalExpense += amt;
            categoryTotals.put(
                    item.getMainCategory(),
                    categoryTotals.get(item.getMainCategory()) + amt
            );
        }

        tvMonthLabel.setText(getHeaderLabelText());
        tvMonthAmount.setText("₹" + (int) totalExpense);
        tvMonthBudget.setText("₹" + (int) currentMonthBudget);

        float balance = currentMonthBudget - totalExpense;
        tvMonthBalance.setText("₹" + (int) balance);

// 🔥 Over Budget Notification (Only once per month)
        String monthKey = selectedMonth + "-" + selectedYear;

        if (currentMonthBudget > 0 && totalExpense > currentMonthBudget
                && !isBudgetNotificationShown(monthKey)) {

            BudgetNotificationHelper.showOverBudgetNotification(this);
            markBudgetNotificationShown(monthKey);
        }



        if (balance < 0) {
            tvMonthBalance.setTextColor(Color.RED);
            tvBudgetWarning.setVisibility(View.VISIBLE);
        } else {
            tvMonthBalance.setTextColor(Color.BLACK);
            tvBudgetWarning.setVisibility(View.GONE);
        }

        if (totalExpense <= 0f) {
            expenseDonutChart.setVisibility(View.GONE);

            tvEmptyExpenses.setText(
                    currentFilter == TimeFilter.TODAY
                            ? "No expenses today"
                            : currentFilter == TimeFilter.WEEK
                            ? "No expenses this week"
                            : "No expenses this month"
            );
            tvEmptyExpenses.setVisibility(View.VISIBLE);
        } else {
            expenseDonutChart.setVisibility(View.VISIBLE);
            tvEmptyExpenses.setVisibility(View.GONE);
        }



        List<CategoryExpenseItem> cardList = new ArrayList<>();

        for (String category : MainCategories.getAll()) {

            float expense = categoryTotals.get(category);
            float budget = getCategoryBudget(category);


            if (budget <= 0f && expense <= 0f) continue;

            cardList.add(
                    new CategoryExpenseItem(
                            category,
                            expense,
                            budget,
                            MainCategories.getIconForCategory(category)
                    )
            );
        }

        rvExpenses.setAdapter(
                new ExpenseAdapter(this, cardList, category -> {

                    float categoryBudget = getCategoryBudget(category);


                    Intent i = new Intent(this, ExpenseCategoryDetailActivity.class);
                    i.putExtra("CATEGORY_NAME", category);
                    i.putExtra("MONTH", selectedMonth);
                    i.putExtra("YEAR", selectedYear);
                    categoryDetailLauncher.launch(i);


                })
        );

        setupDonutChartData(categoryTotals);
    }

    /* ===================== FILTER HELPERS ===================== */
    private String getHeaderLabelText() {
        Calendar now = Calendar.getInstance();

        if (currentFilter == TimeFilter.TODAY)
            return android.text.format.DateFormat
                    .format("dd MMM yyyy", now).toString();

        if (currentFilter == TimeFilter.WEEK)
            return "This Week";

        String monthName =
                new java.text.DateFormatSymbols().getMonths()[selectedMonth];

        return monthName + " " + selectedYear;
    }

    private void moveToPreviousMonth() {
        selectedMonth--;
        if (selectedMonth < Calendar.JANUARY) {
            selectedMonth = Calendar.DECEMBER;
            selectedYear--;
        }
    }

    private void moveToNextMonth() {
        Calendar now = Calendar.getInstance();

        if (selectedYear > now.get(Calendar.YEAR)) return;
        if (selectedYear == now.get(Calendar.YEAR)
                && selectedMonth >= now.get(Calendar.MONTH)) return;

        selectedMonth++;
        if (selectedMonth > Calendar.DECEMBER) {
            selectedMonth = Calendar.JANUARY;
            selectedYear++;
        }
    }

    private void showTimeFilterSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.expense_time_filter, null);

        TextView tvToday = view.findViewById(R.id.tvFilterToday);
        TextView tvWeek = view.findViewById(R.id.tvFilterWeek);
        TextView tvMonth = view.findViewById(R.id.tvFilterMonth);

        tvToday.setOnClickListener(v -> {
            currentFilter = TimeFilter.TODAY;
            dialog.dismiss();
            refreshUI();
        });

        tvWeek.setOnClickListener(v -> {
            currentFilter = TimeFilter.WEEK;
            dialog.dismiss();
            refreshUI();
        });

        tvMonth.setOnClickListener(v -> {
            currentFilter = TimeFilter.MONTH;
            dialog.dismiss();
            refreshUI();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    /* ===================== CHART ===================== */

    private void setupDonutChartStyle() {
        expenseDonutChart.setDrawHoleEnabled(true);
        expenseDonutChart.setHoleRadius(58f);
        expenseDonutChart.setHoleColor(Color.BLACK);
        expenseDonutChart.setDrawEntryLabels(false);
        expenseDonutChart.setRotationEnabled(false);
        expenseDonutChart.getLegend().setEnabled(false);

        Description d = new Description();
        d.setText("");
        expenseDonutChart.setDescription(d);

        expenseDonutChart.setDrawCenterText(true);
        expenseDonutChart.setCenterTextSize(16f);
        expenseDonutChart.setCenterTextColor(Color.WHITE);
        expenseDonutChart.setCenterTextTypeface(
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        );
    }

    private void setupDonutChartData(Map<String, Float> categoryTotals) {

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (String c : MainCategories.getAll()) {
            Float v = categoryTotals.get(c);
            if (v == null || v <= 0f) continue;
            entries.add(new PieEntry(v, c));
            colors.add(CATEGORY_COLOR_MAP.get(c));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSelectionShift(8f);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        expenseDonutChart.setData(data);

        expenseDonutChart.setCenterText(
                "₹" + (int) totalExpense + "\nTotal Expenses"
        );

        expenseDonutChart.invalidate();

        // 🔥 ONLY NEW THING: slice select behavior
        expenseDonutChart.setOnChartValueSelectedListener(
                new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        PieEntry pe = (PieEntry) e;
                        expenseDonutChart.setCenterText(
                                pe.getLabel() + "\n₹" + (int) pe.getValue()
                        );
                    }

                    @Override
                    public void onNothingSelected() {
                        expenseDonutChart.setCenterText(
                                "₹" + (int) totalExpense + "\nTotal Expenses"
                        );
                    }
                }
        );
    }

    /* ===================== FILTER CORE ===================== */

    private List<ExpenseItem> getFilteredExpenses() {

        List<ExpenseItem> filtered = new ArrayList<>();
        Calendar now = Calendar.getInstance();

        for (ExpenseItem item : expenseRepo.getAllExpenses())
        {

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(item.getTimestamp());

            boolean match = false;

            switch (currentFilter) {
                case TODAY:
                    match = cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                            && cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);
                    break;
                case WEEK:
                    match = cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                            && cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR);
                    break;
                case MONTH:
                    match = cal.get(Calendar.YEAR) == selectedYear
                            && cal.get(Calendar.MONTH) == selectedMonth;
                    break;
            }

            if (match) filtered.add(item);
        }

        return filtered;
    }
    private void saveCategoryBudget(String category, float amount) {
        String key = category + "_" + selectedMonth + "_" + selectedYear;
        getSharedPreferences("category_budgets", MODE_PRIVATE)
                .edit()
                .putFloat(key, amount)
                .apply();
    }


    private float getCategoryBudget(String category) {
        String key = category + "_" + selectedMonth + "_" + selectedYear;
        return getSharedPreferences("category_budgets", MODE_PRIVATE)
                .getFloat(key, 0f);
    }

    private enum TimeFilter {
        TODAY,
        WEEK,
        MONTH
    }

}
