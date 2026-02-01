package com.example.moneymitra.repository;

public interface RepositoryCallback {
    void onSuccess();
    void onError(Exception e);
}
