package com.example.moneymitra.repository;

import com.example.moneymitra.model.GoalItem;
import java.util.List;

public interface GoalRepository {

    interface GoalCallback {
        void onSuccess(List<GoalItem> goals);
        void onError(Exception e);
    }

    void addGoal(GoalItem goal, RepositoryCallback callback);

    void getGoals(GoalCallback callback);

    void deleteGoal(String goalId, RepositoryCallback callback);
    void updateGoal(GoalItem goal, RepositoryCallback callback);

}
