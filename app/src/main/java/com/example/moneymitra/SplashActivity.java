package com.example.moneymitra; // change if your package is different

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 900; // ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_MoneyMitra_Splash);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.ivLogo);

        // Play animation if resource exists
        try {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.splash_scale);
            if (anim != null) {
                ivLogo.startAnimation(anim);
            }
        } catch (Exception ignored) {
            // ignore if animation resource missing
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, AuthActivity.class);

                startActivity(intent);
                finish();
            }
        }, SPLASH_DELAY);
    }
}
