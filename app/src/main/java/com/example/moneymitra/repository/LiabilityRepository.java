package com.example.moneymitra.repository;

import com.example.moneymitra.model.LiabilityItem;
import java.util.List;

public interface LiabilityRepository {

    void addLiability(LiabilityItem item);

    void updateLiability(LiabilityItem item);

    void deleteLiability(String liabilityId);

    void fetchAllLiabilities(Callback<List<LiabilityItem>> callback);

    interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}
