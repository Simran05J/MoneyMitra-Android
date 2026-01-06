package com.example.moneymitra;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private FrameLayout tabsContainer;
    private View activeTab;
    private TextView tabSignIn, tabSignUp;
    private LinearLayout layoutSignIn, layoutSignUp;

    private Button btnLogin, btnRegister;
    private View btnGoogleSignIn;

    private float translateDistance = 0f;
    private final int dpMarginForActive = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // 🔹 Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 🔹 Google Sign-In setup
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // ===== FIND VIEWS =====
        tabsContainer = findViewById(R.id.tabsContainer);
        activeTab = findViewById(R.id.activeTab);
        tabSignIn = findViewById(R.id.tabSignIn);
        tabSignUp = findViewById(R.id.tabSignUp);
        layoutSignIn = findViewById(R.id.layoutSignIn);
        layoutSignUp = findViewById(R.id.layoutSignUp);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // ===== EMAIL LOGIN =====
        btnLogin.setOnClickListener(v -> {
            EditText etLoginEmail = findViewById(R.id.etLoginEmail);
            EditText etLoginPassword = findViewById(R.id.etLoginPassword);

            String email = etLoginEmail.getText().toString().trim();
            String password = etLoginPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Email & password required", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> openDashboard())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // ===== EMAIL SIGN UP =====
        btnRegister.setOnClickListener(v -> {
            EditText etRegisterEmail = findViewById(R.id.etRegisterEmail);
            EditText etRegisterPassword = findViewById(R.id.etRegisterPassword);

            String email = etRegisterEmail.getText().toString().trim();
            String password = etRegisterPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Email & password required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> openDashboard())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // ===== GOOGLE SIGN-IN CLICK =====
        btnGoogleSignIn.setOnClickListener(v -> {
            googleSignInClient.signOut(); // force chooser (intentional)
            signInWithGoogle();
        });

        // ===== TAB LOGIC =====
        tabsContainer.post(() -> {
            int containerWidth = tabsContainer.getWidth();
            int marginPx = dpToPx(dpMarginForActive);
            int activeWidth = (containerWidth - marginPx) / 2;

            FrameLayout.LayoutParams lp =
                    (FrameLayout.LayoutParams) activeTab.getLayoutParams();
            lp.width = activeWidth;
            activeTab.setLayoutParams(lp);

            translateDistance = containerWidth / 2f;
            moveToSignIn(false);
        });

        tabSignIn.setOnClickListener(v -> moveToSignIn(true));
        tabSignUp.setOnClickListener(v -> moveToSignUp(true));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            openDashboard();
        }
    }

    // 🔹 Launch Google chooser
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // 🔹 Handle Google result + Firebase login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account =
                        task.getResult(ApiException.class);

                if (account != null && account.getIdToken() != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                }

            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> openDashboard())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void moveToSignIn(boolean animate) {
        layoutSignIn.setVisibility(View.VISIBLE);
        layoutSignUp.setVisibility(View.GONE);

        tabSignIn.setTextColor(ContextCompat.getColor(this, R.color.mm_light));
        tabSignUp.setTextColor(ContextCompat.getColor(this, R.color.black_bg));

        activeTab.animate().translationX(0).setDuration(180).start();
    }

    private void moveToSignUp(boolean animate) {
        layoutSignIn.setVisibility(View.GONE);
        layoutSignUp.setVisibility(View.VISIBLE);

        tabSignUp.setTextColor(ContextCompat.getColor(this, R.color.mm_light));
        tabSignIn.setTextColor(ContextCompat.getColor(this, R.color.black_bg));

        activeTab.animate().translationX(translateDistance).setDuration(180).start();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
