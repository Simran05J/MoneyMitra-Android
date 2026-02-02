package com.example.moneymitra.repository;

import com.example.moneymitra.model.AssetItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FirestoreAssetRepository implements AssetRepository {

    private final FirebaseFirestore firestore;
    private final String userId;

    public FirestoreAssetRepository() {
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private String assetCollectionPath() {
        return "users/" + userId + "/assets";
    }

    @Override
    public void addAsset(AssetItem asset, OnOperationCompleteListener listener) {
        String assetId = firestore.collection(assetCollectionPath()).document().getId();
        asset.setAssetId(assetId);

        firestore.collection(assetCollectionPath())
                .document(assetId)
                .set(asset.toMap())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void getAssets(OnAssetsFetchedListener listener) {
        firestore.collection(assetCollectionPath())
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<AssetItem> assets = new ArrayList<>();
                    snapshot.getDocuments().forEach(doc -> {
                        AssetItem item = doc.toObject(AssetItem.class);
                        if (item != null) {
                            assets.add(item);
                        }
                    });
                    listener.onSuccess(assets);
                })
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void updateAsset(AssetItem asset, OnOperationCompleteListener listener) {
        firestore.collection(assetCollectionPath())
                .document(asset.getAssetId())
                .update(asset.toMap())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    @Override
    public void deleteAsset(String assetId, OnOperationCompleteListener listener) {
        firestore.collection(assetCollectionPath())
                .document(assetId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }
}
