package com.example.moneymitra.repository;

import com.example.moneymitra.model.AssetItem;
import java.util.List;

public interface AssetRepository {

    interface OnAssetsFetchedListener {
        void onSuccess(List<AssetItem> assets);
        void onFailure(Exception e);
    }

    interface OnOperationCompleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    void addAsset(AssetItem asset, OnOperationCompleteListener listener);

    void getAssets(OnAssetsFetchedListener listener);

    void updateAsset(AssetItem asset, OnOperationCompleteListener listener);

    void deleteAsset(String assetId, OnOperationCompleteListener listener);
}
