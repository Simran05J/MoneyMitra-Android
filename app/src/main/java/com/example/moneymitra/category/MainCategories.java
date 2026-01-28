package com.example.moneymitra.category;

import com.example.moneymitra.R;

import java.util.Arrays;
import java.util.List;

public class MainCategories {

    // Category names (single source of truth)
    public static final String HOME_UTILITIES = "Home & Utilities";
    public static final String TRANSPORTATION = "Transportation";
    public static final String FOOD = "Food";
    public static final String HEALTH = "Health";
    public static final String LIFESTYLE = "Lifestyle";
    public static final String OTHER = "Other";

    // List of all categories (ordering matters for UI)
    public static List<String> getAll() {
        return Arrays.asList(
                HOME_UTILITIES,
                TRANSPORTATION,
                FOOD,
                HEALTH,
                LIFESTYLE,
                OTHER
        );
    }

    // Icon mapping for category cards
    public static int getIconForCategory(String category) {
        switch (category) {
            case HOME_UTILITIES:
                return R.drawable.ic_home;

            case TRANSPORTATION:
                return R.drawable.ic_transport;

            case FOOD:
                return R.drawable.ic_food;

            case HEALTH:
                return R.drawable.ic_health;

            case LIFESTYLE:
                return R.drawable.ic_lifestyle;

            case OTHER:
            default:
                return R.drawable.ic_other;
        }
    }

}
