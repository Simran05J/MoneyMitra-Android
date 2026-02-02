package com.example.moneymitra;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.adapter.AssetAdapter;
import com.example.moneymitra.model.AssetItem;
import com.example.moneymitra.repository.AssetRepository;
import com.example.moneymitra.repository.FirestoreAssetRepository;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AssetsActivity extends AppCompatActivity {

    // UI
    private RecyclerView rvAssets;

    // Adapter + data
    private AssetAdapter adapter;
    private final List<AssetItem> assetList = new ArrayList<>();
    private TextView tvTotalAssetsValue;
    private TextView tvEmptyAssets;

// Pie Chart
    private PieChart pieAssets;
    private final int[] assetColors = {
            Color.parseColor("#7CF7E8"),
            Color.parseColor("#C77DFF"),
            Color.parseColor("#6F8BFF"),
            Color.parseColor("#51D928"),
            Color.parseColor("#D37CF7"),
            Color.parseColor("#FFB703")
    };


    // Firestore layer
    private AssetRepository assetRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assets);

        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());


        //  Bind PieChart
        pieAssets = findViewById(R.id.pieAssets);
        setupEmptyPieChart();
        setupPieChartSelection();

        // Bind RecyclerView
        tvTotalAssetsValue = findViewById(R.id.tvTotalAssetsValue);
        rvAssets = findViewById(R.id.rvAssets);
        tvEmptyAssets = findViewById(R.id.tvEmptyAssets);


        // 2️⃣ Setup adapter (long-press only for now)
        adapter = new AssetAdapter(assetList, this::showAssetActions);


        rvAssets.setLayoutManager(new LinearLayoutManager(this));
        rvAssets.setAdapter(adapter);

        // 3️⃣ Init repository (Firestore)
        assetRepository = new FirestoreAssetRepository();

        // 4️⃣ Add button navigation (Add mode)
        ImageView btnAdd = findViewById(R.id.btnAddAsset);

        btnAdd.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.press_scale));
            startActivity(new Intent(AssetsActivity.this, AddEditAssetActivity.class));
        });

    }

    /**
     * 🔥 MOST IMPORTANT PART
     * This runs:
     * - first time activity opens
     * - when returning from Add/Edit screen
     * That’s why list refresh WORKS now.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadAssets();
    }

    /**
     * Fetch assets from Firestore
     * and update RecyclerView
     */
    private void loadAssets() {
        assetRepository.getAssets(new AssetRepository.OnAssetsFetchedListener() {
            @Override
            public void onSuccess(List<AssetItem> assets) {
                assetList.clear();
                assetList.addAll(assets);

                adapter.updateList(assetList);
                updateTotalAssets();
                updateAssetsPieChart();

                if (assetList.isEmpty()) {

                    // EMPTY STATE
                    pieAssets.setVisibility(View.GONE);
                    rvAssets.setVisibility(View.GONE);

                    tvEmptyAssets.setVisibility(View.VISIBLE);
                    tvEmptyAssets.startAnimation(
                            AnimationUtils.loadAnimation(
                                    AssetsActivity.this,
                                    R.anim.empty_fade_in
                            )
                    );

                } else {

                    // DATA STATE
                    tvEmptyAssets.setVisibility(View.GONE);

                    pieAssets.setVisibility(View.VISIBLE);
                    rvAssets.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ASSETS_FETCH_ERROR", e.getMessage(), e);
            }
        });
    }
    //----------Total Assets---------
    private void updateTotalAssets() {
        double total = 0;

        for (AssetItem asset : assetList) {
            total += asset.getValue();
        }

        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        String formatted = formatter.format(total);
        tvTotalAssetsValue.setText("₹ " + formatted);

    }
//pie chart
private void updateAssetsPieChart() {
    // 1️⃣ Group assets by category
    Map<String, Double> categorySumMap = new HashMap<>();

    for (AssetItem asset : assetList) {
        String category = asset.getCategory();
        double value = asset.getValue();

        if (categorySumMap.containsKey(category)) {
            categorySumMap.put(category,
                    categorySumMap.get(category) + value);
        } else {
            categorySumMap.put(category, value);
        }
    }

    // 2️⃣ Convert to PieEntries
    ArrayList<PieEntry> entries = new ArrayList<>();

    for (Map.Entry<String, Double> entry : categorySumMap.entrySet()) {
        entries.add(new PieEntry(
                entry.getValue().floatValue(),
                entry.getKey()
        ));
    }

    // 3️⃣ Handle empty case
    if (entries.isEmpty()) {
        pieAssets.clear();
        pieAssets.invalidate();
        return;
    }

    // 4️⃣ Create dataset
    PieDataSet dataSet = new PieDataSet(entries, "");
    dataSet.setSliceSpace(2f);
    dataSet.setSelectionShift(5f);

    // 5️⃣ Colors (reuse Expense-style palette)

    ArrayList<Integer> colors = new ArrayList<>();

    int colorIndex = 0;
    for (int i = 0; i < entries.size(); i++) {
        colors.add(assetColors[colorIndex]);
        colorIndex = (colorIndex + 1) % assetColors.length;
    }

    dataSet.setColors(colors);

    // 6️⃣ Set data to chart
    PieData data = new PieData(dataSet);
    data.setDrawValues(false);

    pieAssets.setData(data);
    pieAssets.invalidate();
    pieAssets.animateY(900);

}
    private void updateChartCenterText(double total) {
        String centerText = "Assets\n₹ " +
                NumberFormat.getNumberInstance(new Locale("en", "IN"))
                        .format(total);

        pieAssets.setCenterText(centerText);
        pieAssets.setCenterTextSize(14f);
        pieAssets.setCenterTextColor(Color.WHITE);
    }


    private void setupEmptyPieChart() {
        pieAssets.getDescription().setEnabled(false);

        pieAssets.setNoDataText("");
        pieAssets.setNoDataTextColor(Color.TRANSPARENT);

        pieAssets.setDrawHoleEnabled(true);
        pieAssets.setHoleRadius(68f);
        pieAssets.setTransparentCircleRadius(72f);

        pieAssets.setHoleColor(Color.BLACK);
        pieAssets.setTransparentCircleColor(Color.BLACK);

        pieAssets.setDrawEntryLabels(false);
        pieAssets.getLegend().setEnabled(false);

        pieAssets.setRotationEnabled(false);
        pieAssets.setHighlightPerTapEnabled(true);
        pieAssets.setUsePercentValues(false);
    }

    // on slice click
    private void setupPieChartSelection() {
        pieAssets.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry entry = (PieEntry) e;

                String category = entry.getLabel();
                String value = NumberFormat
                        .getNumberInstance(new Locale("en", "IN"))
                        .format(entry.getValue());

                pieAssets.setCenterText(category + "\n₹ " + value);
                pieAssets.setCenterTextSize(14f);
                pieAssets.setCenterTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected() {
                pieAssets.setCenterText(""); // blank center
            }
        });
    }

    // Bottom sheet Actions
    private void showAssetActions(AssetItem asset) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater()
                .inflate(R.layout.bottom_sheet_asset_actions, null);

        view.findViewById(R.id.tvEditAsset).setOnClickListener(v -> {
            Intent intent = new Intent(AssetsActivity.this, AddEditAssetActivity.class);
            intent.putExtra("MODE", "EDIT");
            intent.putExtra("ASSET_ID", asset.getAssetId());
            intent.putExtra("ASSET_NAME", asset.getName());
            intent.putExtra("ASSET_CATEGORY", asset.getCategory());
            intent.putExtra("ASSET_VALUE", asset.getValue());
            intent.putExtra("ASSET_NOTE", asset.getNote());
            startActivity(intent);
            dialog.dismiss();
        });
         //Delete logic
        view.findViewById(R.id.tvDeleteAsset).setOnClickListener(v -> {
            assetRepository.deleteAsset(asset.getAssetId(),
                    new AssetRepository.OnOperationCompleteListener() {
                        @Override
                        public void onSuccess() {
                            dialog.dismiss();
                            loadAssets(); // refresh list
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("ASSET_DELETE_ERROR", e.getMessage(), e);
                        }
                    });
        });

        dialog.setContentView(view);
        dialog.show();
    }

}
