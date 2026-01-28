package com.example.moneymitra.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.R;
import com.example.moneymitra.model.ExpenseItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseRecordAdapter extends RecyclerView.Adapter<ExpenseRecordAdapter.VH> {

    private final List<ExpenseItem> list;
    private final SimpleDateFormat df =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    // EDIT CLICK LISTENER (optional)
    public interface OnExpenseActionListener {
        void onEdit(ExpenseItem item);
    }
    private OnExpenseActionListener listener;

    // LONG CLICK LISTENER (for bottom sheet)
    public interface OnExpenseLongClickListener {
        void onLongClick(ExpenseItem item);
    }
    private OnExpenseLongClickListener longClickListener;

    public void setOnExpenseLongClickListener(OnExpenseLongClickListener listener) {
        this.longClickListener = listener;
    }

    // EDIT MODE CONSTRUCTOR
    public ExpenseRecordAdapter(List<ExpenseItem> list, OnExpenseActionListener listener){
        this.list = list;
        this.listener = listener;
    }

    // NORMAL MODE CONSTRUCTOR
    public ExpenseRecordAdapter(List<ExpenseItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_record, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ExpenseItem e = list.get(pos);

        h.tvTitle.setText(e.getTitle());
        h.tvAmount.setText("₹" + (int) e.getAmount());
        h.tvDate.setText(df.format(e.getTimestamp()));

        // LONG PRESS FOR BOTTOM SHEET
        h.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(e);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount, tvDate;

        VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvAmount = v.findViewById(R.id.tvAmount);
            tvDate = v.findViewById(R.id.tvDate);
        }
    }
}
