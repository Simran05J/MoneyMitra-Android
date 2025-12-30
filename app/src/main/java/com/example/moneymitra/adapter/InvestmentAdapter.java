package com.example.moneymitra.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        // 🔥 THIS WAS MISSING — COLOR BINDING
        holder.viewColor.setBackgroundColor(item.getColor());

        holder.itemView.setOnClickListener(v ->
                click.onClick(holder.getAdapterPosition())
        );

        holder.itemView.setOnLongClickListener(v -> {
            longClick.onLongClick(holder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtAmount;
        View viewColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtInvestmentName);
            txtAmount = itemView.findViewById(R.id.txtInvestmentAmount);

            // 🔥 COLOR INDICATOR VIEW
            viewColor = itemView.findViewById(R.id.viewColor);
        }
    }
}
