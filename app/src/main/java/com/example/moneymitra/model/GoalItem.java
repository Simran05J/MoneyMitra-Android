package com.example.moneymitra.model;

public class GoalItem {

    private String goalId;
    private String goalName;
    private double targetAmount;
    private double currentAmount;
    private String linkedInvestmentId;
    private long createdAt;

    // Firestore requires empty constructor
    public GoalItem() {}

    public GoalItem(String goalId, String goalName, double targetAmount,
                    double currentAmount, String linkedInvestmentId, long createdAt) {
        this.goalId = goalId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.linkedInvestmentId = linkedInvestmentId;
        this.createdAt = createdAt;
    }

    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }

    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public String getLinkedInvestmentId() { return linkedInvestmentId; }
    public void setLinkedInvestmentId(String linkedInvestmentId) { this.linkedInvestmentId = linkedInvestmentId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
