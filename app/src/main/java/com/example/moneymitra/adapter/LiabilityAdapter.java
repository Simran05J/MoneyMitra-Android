package com.example.moneymitra.adapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.R;
import com.example.moneymitra.model.LiabilityItem;

import java.util.ArrayList;
import java.util.List;

/**
 * LiabilityAdapter
 *
 * Responsibility:
 * - Bind liability data to RecyclerView
 * - Detect LONG PRESS on an item
 * - Notify Activity which item was long-pressed
 *
 * IMPORTANT:
 * - Adapter owns listeners
 * - ViewHolder is DUMB (no business logic)
 */
public class LiabilityAdapter extends RecyclerView.Adapter<LiabilityAdapter.LiabilityViewHolder> {

    // -------------------------
    // DATA SOURCE
    // -------------------------
    private final List<LiabilityItem> items = new ArrayList<>();

    // -------------------------
    // LONG CLICK CALLBACK
    // -------------------------
    public interface OnItemLongClickListener {
        void onItemLongClick(LiabilityItem item);
    }

    private OnItemLongClickListener longClickListener;

    /**
     * Called by Activity to listen for long-press events
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    // -------------------------
    // LIST UPDATE
    // -------------------------
    public void updateList(List<LiabilityItem> newList) {
        items.clear();
        if (newList != null) {
            items.addAll(newList);
        }
        notifyDataSetChanged();
    }

    // -------------------------
    // VIEW HOLDER CREATION
    // -------------------------
    @NonNull
    @Override
    public LiabilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_liability, parent, false);
        return new LiabilityViewHolder(view);
    }

    // -------------------------
    // BIND DATA + LONG PRESS
    // -------------------------
    @Override
    public void onBindViewHolder(@NonNull LiabilityViewHolder holder, int position) {

        // 1️⃣ Current item from list
        LiabilityItem item = items.get(position);

        // 2️⃣ Set liability name (LEFT SIDE, bold)
        holder.tvName.setText(item.getName());

        // 3️⃣ Set amount (RIGHT SIDE)
        // Minus sign dikhate hain because liability = money going out
        holder.tvAmount.setText("- ₹" + item.getTotalAmount());

        // 4️⃣ Set category (small text below name)
        holder.tvCategory.setText(item.getCategory());

        // 5️⃣ EMI is OPTIONAL
        // Agar EMI hai → show
        // Agar EMI null hai → hide view
        if (item.getEmi() != null) {
            holder.tvEmi.setVisibility(View.VISIBLE);
            holder.tvEmi.setText("EMI: ₹" + item.getEmi());
        } else {
            holder.tvEmi.setVisibility(View.GONE);
        }

        holder.itemView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(AnimationUtils.loadAnimation(
                            v.getContext(), R.anim.press_scale));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.startAnimation(AnimationUtils.loadAnimation(
                            v.getContext(), R.anim.release_scale));
                    break;
            }
            return false; // long-press + click ko block mat karo
        });


        // 6️⃣ Long press → open bottom sheet (Edit / Delete)
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(item);
            }
            return true; // VERY IMPORTANT: long-press consume karna
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    // -------------------------
    // VIEW HOLDER (DUMB VIEW)
    // -------------------------
    static class LiabilityViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvAmount, tvCategory, tvEmi;

        public LiabilityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvEmi = itemView.findViewById(R.id.tvEmi);

        }
    }
}
