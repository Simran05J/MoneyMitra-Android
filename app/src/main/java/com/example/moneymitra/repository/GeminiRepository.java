package com.example.moneymitra.repository;

import android.util.Log;
import okhttp3.Response;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class GeminiRepository {

    private static final String TAG = "GeminiRepo";

    // 🔑 PUT YOUR API KEY HERE
    private static final String API_KEY = "";

    // 🌐 Gemini endpoint
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=";

    // ⏱ Rate limiting (avoid quota errors)
    private static final long MIN_REQUEST_INTERVAL = 4000; // 4 seconds
    private long lastRequestTime = 0;

    private OkHttpClient client = new OkHttpClient();

    // =====================================================
    // 🔁 CALLBACK INTERFACE
    // This allows repository → Activity communication
    // =====================================================
    public interface GeminiCallback {
        void onSuccess(String reply);
        void onError(String error);
    }

    // =====================================================
    // 🧪 OLD TEST FUNCTION (KEEP FOR DEBUGGING)
    // =====================================================
    public void testApiCall() {

        applyRateLimit();

        try {
            JSONObject part = new JSONObject();
            part.put("text", "Say hello from MoneyMitra AI");

            JSONArray partsArray = new JSONArray();
            partsArray.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", partsArray);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(content);

            JSONObject body = new JSONObject();
            body.put("contents", contentsArray);

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + API_KEY)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API FAILED: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "API SUCCESS: " + response.body().string());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // 🤖 MAIN FUNCTION — SEND USER MESSAGE TO GEMINI
    // =====================================================
    public void sendMessageToGemini(String userMessage, String financialContext, GeminiCallback callback)
    {

        applyRateLimit();

        try {
            // 🧱 Build JSON request dynamically using user message
            // 🧠 SYSTEM PROMPT — MoneyMitra personality
            String systemPrompt =
                    "You are MoneyMitra AI, a smart personal finance advisor inside an Android app.\n\n" +

                            "STRICT RULES:\n" +
                            "1. Always use the provided financial data. Never assume missing data.\n" +
                            "2. If values are low or zero, explicitly mention it.\n" +
                            "3. Give specific numeric insights (₹ amounts, comparisons).\n" +
                            "4. Do NOT give generic advice like 'save money' or 'budget better'.\n" +
                            "5. Answer in 2-4 lines max.\n\n" +

                            "BEHAVIOR:\n" +
                            "- If user overspending → warn clearly.\n" +
                            "- If investments are low → suggest improvement.\n" +
                            "- If goal progress is slow → suggest exact action.\n" +
                            "- If data is good → acknowledge positively but briefly.\n\n" +

                            "Your job is to analyze the data and act like a real financial advisor, not a chatbot.";
// Combine system + user message
            String finalPrompt =
                    systemPrompt +
                            "\n\n--- USER FINANCIAL SNAPSHOT ---\n" +
                            financialContext +
                            "\n\n--- TASK ---\n" +
                            "Based on the financial snapshot above, answer the user question.\n" +
                            "Give specific, actionable financial advice.\n" +
                            "\nUser question: " + userMessage;


            JSONObject part = new JSONObject();
            part.put("text", finalPrompt);


            JSONArray partsArray = new JSONArray();
            partsArray.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", partsArray);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(content);

            JSONObject body = new JSONObject();
            body.put("contents", contentsArray);

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();


            // 🚀 API CALL
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String res = response.body().string();
                    Log.d("GEMINI_RAW_RESPONSE", res);


                    try {
                        // 🧠 PARSE GEMINI JSON RESPONSE
                        JSONObject jsonObject = new JSONObject(res);

// 🛡️ Safe parsing (handles empty or different responses)
                        if (jsonObject.has("candidates")) {

                            JSONArray candidates = jsonObject.getJSONArray("candidates");

                            if (candidates.length() > 0) {

                                JSONObject firstCandidate = candidates.getJSONObject(0);

                                if (firstCandidate.has("content")) {

                                    JSONObject content = firstCandidate.getJSONObject("content");

                                    if (content.has("parts")) {

                                        JSONArray parts = content.getJSONArray("parts");

// 🔥 Gemini 2.5 returns multiple text parts — we must combine them
                                        StringBuilder fullReply = new StringBuilder();

                                        for (int i = 0; i < parts.length(); i++) {
                                            JSONObject partObj = parts.getJSONObject(i);
                                            if (partObj.has("text")) {
                                                fullReply.append(partObj.getString("text"));
                                            }
                                        }

                                        if (fullReply.length() > 0) {
                                            callback.onSuccess(fullReply.toString());
                                            return;
                                        }

                                    }
                                }
                            }
                        }

// If we reach here → response format unexpected
                        callback.onError("AI returned empty response");


                    } catch (Exception e) {
                        callback.onError("Parsing error");
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Request build failed");
        }
    }

    // =====================================================
    // ⏱ RATE LIMIT FUNCTION (reuse everywhere)
    // =====================================================
    private void applyRateLimit() {
        long timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime;
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
            try {
                Thread.sleep(MIN_REQUEST_INTERVAL - timeSinceLastRequest);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }
}
