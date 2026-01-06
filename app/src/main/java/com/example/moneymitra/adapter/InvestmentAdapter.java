package com.example.moneymitra.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;          // ✅ ADDED
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils; // ✅ ADDED
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.R;
import com.example.moneymitra.model.InvestmentItem;

import java.util.List;

public class InvestmentAdapter
        extends RecyclerView.Adapter<InvestmentAdapter.ViewHolder> {

    public interface OnClick {
        void onClick(int position);
    }

    public interface OnLongClick {
        void onLongClick(int position);
    }

    private final List<InvestmentItem> list;
    private final OnClick click;
    private final OnLongClick longClick;

    // 🔥 selection state
    private int selectedPosition = -1;

    public InvestmentAdapter(
            List<InvestmentItem> list,
            OnClick click,
            OnLongClick longClick
    ) {
        this.list = list;
        this.click = click;
        this.longClick = longClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_investment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        InvestmentItem item = list.get(position);

        holder.txtName.setText(item.getName());
        holder.txtAmount.setText("₹" + item.getAmount());

        // 🔧 Color binding
        holder.viewColor.setBackgroundColor(item.getColor());
        if (position == selectedPosition) {
            // 🔥 color strip active animation
            holder.viewColor.animate()
                    .alpha(1f)
                    .scaleY(1.2f)
                    .setDuration(180)
                    .start();
        } else {
            // 🔥 reset for unselected items
            holder.viewColor.animate()
                    .alpha(0.4f)
                    .scaleY(1f)
                    .setDuration(180)
                    .start();
        }


        // 🔥 selection animation
        if (position == selectedPosition) {
            holder.itemView.animate()
                    .scaleX(1.03f)
                    .scaleY(1.03f)
                    .setDuration(180)
                    .start();

            holder.itemView.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        } else {
            holder.itemView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(180)
                    .start();

            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                click.onClick(pos);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                longClick.onLongClick(pos);
                return true;
            }
            return false;
        });

        // 🔥 press animation
        holder.itemView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(
                            AnimationUtils.loadAnimation(v.getContext(), R.anim.press_scale)
                    );
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.startAnimation(
                            AnimationUtils.loadAnimation(v.getContext(), R.anim.release_scale)
                    );
                    break;
            }
            return false; // allow click + long click
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // 🔥 called from InvestmentActivity to sync selection
    public void setSelectedPosition(int pos) {
        selectedPosition = pos;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtAmount;
        View viewColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtInvestmentName);
            txtAmount = itemView.findViewById(R.id.txtInvestmentAmount);
            viewColor = itemView.findViewById(R.id.viewColor);
        }
    }
}
