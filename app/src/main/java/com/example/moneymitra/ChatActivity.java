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

import com.example.moneymitra.repository.InvestmentRepository;
import com.example.moneymitra.model.InvestmentItem;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.moneymitra.repository.GoalRepository;
import com.example.moneymitra.repository.FirestoreGoalRepository;
import com.example.moneymitra.model.GoalItem;

import com.example.moneymitra.repository.ExpenseRepository;
import com.example.moneymitra.model.ExpenseItem;

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
    private ExpenseRepository expenseRepository;
    private List<ExpenseItem> expenseItems = new ArrayList<>();
    private InvestmentRepository investmentRepository;
    private List<InvestmentItem> investmentItems = new ArrayList<>();
    private GoalRepository goalRepository;
    private List<GoalItem> goalItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        // 🔹 Initialize Gemini repository
        geminiRepository = new GeminiRepository();

        expenseRepository = ExpenseRepository.getInstance();

        investmentRepository = new InvestmentRepository();

        goalRepository = new FirestoreGoalRepository();

        expenseRepository.loadExpenses(list -> {
            expenseItems = list;
        });

        // ================= LOAD INVESTMENTS FROM FIREBASE =================
        investmentRepository.fetchInvestments(
                snapshot -> {
                    investmentItems.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        InvestmentItem item = doc.toObject(InvestmentItem.class);
                        item.setId(doc.getId());
                        investmentItems.add(item);
                    }
                },
                e -> {
                    // Optional: log error
                }
        );
        // ================= LOAD GOALS FROM FIREBASE =================
        goalRepository.getGoals(new GoalRepository.GoalCallback() {
            @Override
            public void onSuccess(List<GoalItem> goals) {
                goalItems = goals;
            }

            @Override
            public void onError(Exception e) {
                // optional log
            }
        });

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
        float totalSpent = 0f;

        for (ExpenseItem item : expenseItems) {
            totalSpent += item.getAmount();
        }

        // 💰 Convert numbers → ₹ strings
        String monthlyBudget = "₹" + (int) monthlyBudgetValue;
        String spentThisMonth = "₹" + (int) totalSpent;
        String home = "Calculated from expenses";
        String food = "Calculated from expenses";
        String transport = "Calculated from expenses";

        // 🎯 Goal data from Goal module
        String goalName = "No active goal";
        String goalTarget = "₹0";
        String goalSaved = "₹0";

        if (!goalItems.isEmpty()) {

            // For now we take the first goal (later we can improve)
            GoalItem goal = goalItems.get(0);

            goalName = goal.getGoalName();

            double target = goal.getTargetAmount();

            double saved = 0;

// Find linked investment amount
            String linkedId = goal.getLinkedInvestmentId();

            for (InvestmentItem inv : investmentItems) {
                if (inv.getId().equals(linkedId)) {
                    saved = inv.getAmount();
                    break;
                }
            }

            goalTarget = "₹" + (int) target;
            goalSaved = "₹" + (int) saved;
        }
        // 💹 Investment summary
        float totalInvestmentValue = 0f;

        for (InvestmentItem item : investmentItems) {
            totalInvestmentValue += item.getAmount();
        }

        String totalInvestments = "₹" + (int) totalInvestmentValue;


        return "USER FINANCIAL DATA FROM MONEYMITRA APP:\n\n" +

                "Monthly Budget: " + monthlyBudget + "\n" +
                "Total Spent This Month: " + spentThisMonth + "\n\n" +

                "Category Breakdown:\n" +
                "- Home & Utilities: " + home + "\n" +
                "- Food: " + food + "\n" +
                "- Transport: " + transport + "\n\n" +

                "Investments Total: " + totalInvestments + "\n\n" +

                "Savings Goal:\n" +
                "Goal Name: " + goalName + "\n" +
                "Target Amount: " + goalTarget + "\n" +
                "Saved So Far: " + goalSaved + "\n\n" +

                "Use this real financial data to answer the user.";

    }

}
