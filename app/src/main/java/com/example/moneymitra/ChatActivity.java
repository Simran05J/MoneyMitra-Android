package com.example.moneymitra;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;

import com.example.moneymitra.adapter.ChatAdapter;
import com.example.moneymitra.model.ChatMessage;
import com.example.moneymitra.repository.GeminiRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    // UI components
    RecyclerView recyclerChat;
    EditText etMessage;
    ImageView btnSend;

    // Gemini API repository
    private GeminiRepository geminiRepository;

    // Chat data
    ChatAdapter chatAdapter;
    List<ChatMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        // 🔹 Initialize Gemini repository
        geminiRepository = new GeminiRepository();

        // 🔹 Bind views
        recyclerChat = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // 🔹 Setup RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        // 🔹 Send button click
        btnSend.setOnClickListener(v -> {

            String userText = etMessage.getText().toString().trim();
            if (userText.isEmpty()) return;

            // 1️⃣ Add USER message bubble
            messageList.add(new ChatMessage(userText, true));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerChat.scrollToPosition(messageList.size() - 1);

            etMessage.setText("");

            // 2️⃣ Send message to Gemini (REAL AI CALL)
            sendMessageToAI(userText);
        });
    }

    // =====================================================
    // 🤖 FUNCTION → SEND USER MESSAGE TO GEMINI
    // =====================================================
    private void sendMessageToAI(String message) {

        // 🟡 Step 1: Show "Typing..." loading bubble
        ChatMessage loadingMessage = new ChatMessage("Typing...", false);
        messageList.add(loadingMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerChat.scrollToPosition(messageList.size() - 1);

        // 🟡 Step 2: Call Gemini API
        String financialContext = buildFinancialContext();

// 🤖 Call Gemini with user message + financial data
        geminiRepository.sendMessageToGemini(message, financialContext, new GeminiRepository.GeminiCallback() {


            @Override
            public void onSuccess(String reply) {

                runOnUiThread(() -> {

                    // ❌ Remove loading bubble
                    messageList.remove(messageList.size() - 1);
                    chatAdapter.notifyItemRemoved(messageList.size());

                    // ✅ Add REAL AI reply bubble
                    ChatMessage botMessage = new ChatMessage(reply, false);
                    messageList.add(botMessage);
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerChat.scrollToPosition(messageList.size() - 1);
                });
            }

            @Override
            public void onError(String error) {

                runOnUiThread(() -> {

                    // ❌ Remove loading bubble
                    messageList.remove(messageList.size() - 1);
                    chatAdapter.notifyItemRemoved(messageList.size());

                    // ⚠️ Show error message bubble
                    ChatMessage errorMsg = new ChatMessage("Error: " + error, false);
                    messageList.add(errorMsg);
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerChat.scrollToPosition(messageList.size() - 1);
                });
            }
        });
    }

    // =====================================================
// 🧠 Builds financial snapshot for AI prompt
// Now using REAL expense data + temporary goal placeholder
// =====================================================
    private String buildFinancialContext() {

        SharedPreferences prefs = getSharedPreferences("MoneyMitraPrefs", MODE_PRIVATE);

        // 📊 Monthly budget from Expense module
        float monthlyBudgetValue = prefs.getFloat("monthly_budget", 0f);

        // 📊 Category spending totals
        float homeValue = prefs.getFloat("expense_home", 0f);
        float foodValue = prefs.getFloat("expense_food", 0f);
        float transportValue = prefs.getFloat("expense_transport", 0f);

        // 📊 Calculate total spent
        float totalSpent = homeValue + foodValue + transportValue;

        // 💰 Convert numbers → ₹ strings
        String monthlyBudget = "₹" + (int) monthlyBudgetValue;
        String spentThisMonth = "₹" + (int) totalSpent;
        String home = "₹" + (int) homeValue;
        String food = "₹" + (int) foodValue;
        String transport = "₹" + (int) transportValue;

        // 🎯 Goal data from Goal module
        String goalName = prefs.getString("goal_name", "No active goal");
        float goalTargetValue = prefs.getFloat("goal_target_amount", 0f);
        float goalSavedValue = prefs.getFloat("goal_saved_amount", 0f);

        String goalTarget = "₹" + (int) goalTargetValue;
        String goalSaved = "₹" + (int) goalSavedValue;
        // 💹 TEMP Investment summary (next we connect Investment module)
        String totalInvestments = "₹50000";



        return "Monthly budget: " + monthlyBudget + "\n" +
                "Spent this month: " + spentThisMonth + "\n" +
                "Home & Utilities: " + home + "\n" +
                "Food: " + food + "\n" +
                "Transport: " + transport + "\n" +
                "Total Investments: " + totalInvestments + "\n" +
                "Goal: " + goalName + " Target " + goalTarget + ", Saved " + goalSaved;

    }

}
