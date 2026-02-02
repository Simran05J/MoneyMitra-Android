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
import com.example.moneymitra.model.AssetItem;

import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.AssetViewHolder> {

    public interface OnAssetLongClickListener {
        void onLongClick(AssetItem asset);
    }

    private List<AssetItem> assetList;
    private OnAssetLongClickListener longClickListener;

    public AssetAdapter(List<AssetItem> assetList,
                        OnAssetLongClickListener longClickListener) {
        this.assetList = assetList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public AssetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asset, parent, false);
        return new AssetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssetViewHolder holder, int position) {
        AssetItem asset = assetList.get(position);

        holder.tvName.setText(asset.getName());
        holder.tvCategory.setText(asset.getCategory());
        holder.tvValue.setText("₹ " + asset.getValue());

        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onLongClick(asset);
            return true;
        });
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
            return false; // VERY IMPORTANT (do not consume event)
        });

    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }

    public void updateList(List<AssetItem> newList) {
        assetList = newList;
        notifyDataSetChanged();
    }

    static class AssetViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvCategory, tvValue;

        public AssetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAssetName);
            tvCategory = itemView.findViewById(R.id.tvAssetCategory);
            tvValue = itemView.findViewById(R.id.tvAssetValue);
        }
    }
}
