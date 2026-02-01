package com.example.moneymitra.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymitra.R;
import com.example.moneymitra.model.GoalItem;

import java.util.List;
import java.util.Map;

public class GoalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ---------------- CONSTANTS ----------------
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // ---------------- DATA ----------------
    private List<GoalItem> goalList;
    private Map<String, Double> investmentAmountMap;
    private Map<String, String> investmentNameMap;

    // ---------------- CALLBACK ----------------
    private OnGoalLongClickListener longClickListener;

    // ---------------- INTERFACE ----------------
    public interface OnGoalLongClickListener {
        void onGoalLongClick(GoalItem goal);
    }

    // ---------------- CONSTRUCTOR ----------------
    public GoalAdapter(List<GoalItem> goalList,
                       Map<String, Double> investmentAmountMap) {
        this.goalList = goalList;
        this.investmentAmountMap = investmentAmountMap;
    }

    // ---------------- ADAPTER BASICS ----------------
    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return goalList.size() + 1; // +1 for header
    }

    public void updateList(List<GoalItem> newList) {
        this.goalList = newList;
        notifyDataSetChanged();
    }

    // ---------------- VIEW HOLDER CREATION ----------------
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_goal_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_goal, parent, false);
            return new GoalViewHolder(view);
        }
    }

    // ---------------- DATA BINDING ----------------
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) return;

        GoalItem goal = goalList.get(position - 1); // offset because header
        GoalViewHolder vh = (GoalViewHolder) holder;

        // ---------- Investment tag ----------
        String investmentId = goal.getLinkedInvestmentId();
        if (investmentId != null &&
                investmentNameMap != null &&
                investmentNameMap.containsKey(investmentId)) {

            vh.tvInvestmentTag.setText(investmentNameMap.get(investmentId));
        } else {
            vh.tvInvestmentTag.setText("Link to an investment");
        }

        // ---------- Title & target ----------
        vh.tvTitle.setText(goal.getGoalName());
        vh.tvTarget.setText("₹" + goal.getTargetAmount());

        // ---------- Progress calculation ----------
        double achieved = investmentAmountMap.getOrDefault(investmentId, 0.0);
        double percent = goal.getTargetAmount() > 0
                ? (achieved / goal.getTargetAmount()) * 100
                : 0;

        vh.tvAchieved.setText("₹" + achieved);
        vh.tvPercent.setText(String.format("%.1f%%", percent));

        vh.progressBar.setMax(1000); // allows 0.1%
        vh.progressBar.setProgress(0);

        int scaledPercent = (int) (percent * 10);

        ObjectAnimator anim = ObjectAnimator.ofInt(
                vh.progressBar,
                "progress",
                0,
                scaledPercent
        );

        anim.setDuration(1000);
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.start();
// ---------- Animation ----------
        vh.itemView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(AnimationUtils.loadAnimation(
                        v.getContext(), R.anim.press_scale));
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.startAnimation(AnimationUtils.loadAnimation(
                        v.getContext(), R.anim.release_scale));
            }
            return false;
        });


        // ---------- LONG PRESS ----------
        vh.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onGoalLongClick(goal);
            }
            return true;
        });
    }

    // ---------------- SETTERS ----------------
    public void setInvestmentNameMap(Map<String, String> map) {
        this.investmentNameMap = map;
        notifyDataSetChanged();
    }

    public void setOnGoalLongClickListener(OnGoalLongClickListener listener) {
        this.longClickListener = listener;
    }

    // ---------------- VIEW HOLDERS ----------------
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvTarget, tvAchieved, tvPercent, tvInvestmentTag;
        ProgressBar progressBar;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvGoalTitle);
            tvTarget = itemView.findViewById(R.id.tvGoalTarget);
            tvAchieved = itemView.findViewById(R.id.tvGoalAchieved);
            tvPercent = itemView.findViewById(R.id.tvGoalPercent);
            tvInvestmentTag = itemView.findViewById(R.id.tvInvestmentTag);
            progressBar = itemView.findViewById(R.id.progressGoal);
        }
    }
}
