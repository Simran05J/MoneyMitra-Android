package com.example.moneymitra.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.R;
import com.example.moneymitra.model.CategoryExpenseItem;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private final Context context;
    private final List<CategoryExpenseItem> categoryList;
    private final OnCategoryClickListener listener;

    // 🔹 Click listener interface
    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    // 🔹 SINGLE constructor (clean)
    public ExpenseAdapter(Context context,
                          List<CategoryExpenseItem> categoryList,
                          OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryExpenseItem item = categoryList.get(position);
        holder.rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.press_scale));
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.release_scale));
            }
            return false;
        });


        holder.tvCategoryName.setText(item.getCategoryName());
        float spent = item.getExpense();
        float budget = item.getBudget();

        if (budget > 0) {
            holder.tvCategoryTotal.setText(
                    "₹" + (int) spent + " / ₹" + (int) budget
            );
        } else {
            holder.tvCategoryTotal.setText("₹" + (int) spent);
        }

        holder.ivCategoryIcon.setImageResource(item.getIconRes());

        // 🔥 SAME click for card + arrow
        View.OnClickListener clickListener = v ->
                listener.onCategoryClick(item.getCategoryName());

        holder.rootView.setOnClickListener(clickListener);
        holder.ivNext.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View rootView;
        ImageView ivCategoryIcon, ivNext;
        TextView tvCategoryName, tvCategoryTotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            rootView = itemView; // 👈 full card clickable
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivNext = itemView.findViewById(R.id.ivNext);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryTotal = itemView.findViewById(R.id.tvCategoryTotal);
        }
    }
}
