package com.example.moneymitra;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ===== PROFILE IMAGE =====
        ImageView imgProfile = findViewById(R.id.imgProfile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Uri photoUri = user.getPhotoUrl();

            if (photoUri != null) {
                android.util.Log.d("PROFILE_PHOTO", "Photo URL = " + photoUri.toString());

                Glide.with(this)
                        .load(photoUri)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(imgProfile);
            } else {
                android.util.Log.d("PROFILE_PHOTO", "Photo URL is NULL");
            }
        } else {
            android.util.Log.d("PROFILE_PHOTO", "User is NULL");
        }


        // ===== FIND VIEWS =====
        MaterialCardView cardInvestment = findViewById(R.id.cardInvestment);
        MaterialCardView cardGoal = findViewById(R.id.cardGoal);
        MaterialCardView cardLiability = findViewById(R.id.cardLiability);
        MaterialCardView cardCalculator = findViewById(R.id.cardCalculator);
        MaterialCardView cardAsset = findViewById(R.id.cardAsset);
        MaterialCardView cardExpense = findViewById(R.id.cardExpense);

        ImageView ivChatbot = findViewById(R.id.ivChatbot);
        ImageView ivProfile = findViewById(R.id.imgProfile);
        TextView tvTagline = findViewById(R.id.tvTagline);

        // ===== TAGLINE COLOR =====
        String text = "Your One Stop Solution\nfor Smart Finances";
        SpannableString spannable = new SpannableString(text);

        int start = text.indexOf("Finances");
        if (start != -1) {
            spannable.setSpan(
                    new ForegroundColorSpan(Color.parseColor("#6FF2E7")),
                    start,
                    start + "Finances".length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        tvTagline.setText(spannable);

        // ===== PRESS ANIMATIONS =====
        applyPressAnimation(cardInvestment);
        applyPressAnimation(cardGoal);
        applyPressAnimation(cardLiability);
        applyPressAnimation(cardCalculator);
        applyPressAnimation(cardAsset);
        applyPressAnimation(cardExpense);
        applyPressAnimation(ivChatbot);
        applyPressAnimation(ivProfile);

        // ===== CLICKS =====
        cardInvestment.setOnClickListener(v ->
                startActivity(new Intent(this, InvestmentActivity.class)));

        ivProfile.setOnClickListener(v -> showProfileBottomSheet());
    }

    // ===== PROFILE BOTTOM SHEET =====
    private void showProfileBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_profile, null);
        dialog.setContentView(view);

        TextView tvName = view.findViewById(R.id.tvUserName);
        TextView tvEmail = view.findViewById(R.id.tvUserEmail);
        TextView btnLogout = view.findViewById(R.id.btnLogout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            tvEmail.setText(user.getEmail());
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            dialog.dismiss();

            Intent intent = new Intent(DashboardActivity.this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        dialog.show();
    }

    // ===== PRESS SCALE EFFECT =====
    private void applyPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(120).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
            }
            return false;
        });
    }
}
