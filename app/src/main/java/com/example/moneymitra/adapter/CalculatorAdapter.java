package com.example.moneymitra.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.EmiCalculatorActivity;
import com.example.moneymitra.FdCalculatorActivity;
import com.example.moneymitra.LoanCalculatorActivity;
import com.example.moneymitra.PfCalculatorActivity;
import com.example.moneymitra.R;

import com.example.moneymitra.SipCalculatorActivity;
import com.example.moneymitra.model.CalculatorItem;

import java.util.List;

public class CalculatorAdapter extends RecyclerView.Adapter<CalculatorAdapter.ViewHolder> {

    List<CalculatorItem> list;

    public CalculatorAdapter(List<CalculatorItem> list) {
        this.list = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.imgIcon);
            title = itemView.findViewById(R.id.tvTitle);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calculator_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalculatorItem item = list.get(position);
        holder.title.setText(item.title);
        holder.icon.setImageResource(item.icon);

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = null;

            switch (position) {
                case 0: intent = new Intent(context, SipCalculatorActivity.class); break;
                case 1: intent = new Intent(context, EmiCalculatorActivity.class); break;
                case 2: intent = new Intent(context, FdCalculatorActivity.class); break;
                case 3: intent = new Intent(context, LoanCalculatorActivity.class); break;
                case 4: intent = new Intent(context, PfCalculatorActivity.class); break;
            }

            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}
