package com.example.navigation;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class ChatBotDialog extends DialogFragment {

    private EditText userInput;
    private Button sendButton;

    private LinearLayout chatContainer;
    private int getSignalColor(double dbm) {
        if (dbm >= -80) return Color.parseColor("#4CAF50"); // Green: good
        if (dbm >= -95) return Color.parseColor("#FFC107"); // Yellow: fair
        return Color.parseColor("#F44336");                 // Red: poor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_chatbot, container, false);

        chatContainer = view.findViewById(R.id.chat_container);

        userInput = view.findViewById(R.id.user_input);
        sendButton = view.findViewById(R.id.send_button);

        sendButton.setOnClickListener(v -> {
            String message = userInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendUserMessage(message);
            }
        });

        return view;
    }

    private void sendUserMessage(String message) {
        TextView userMsg = new TextView(getContext());
        userMsg.setText("You: " + message);
        userMsg.setTextColor(Color.BLACK);
        userMsg.setPadding(8, 8, 8, 8);
        chatContainer.addView(userMsg);

        // Call MainActivity’s method to fetch chatbot response
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.getChatbotResponse(message);
        }
    }

    // Optional: create a method the activity can use to send back chatbot response
    public void showBotResponse(String responseText) {
        if (chatContainer == null || getContext() == null) return;

        String[] lines = responseText.split("\n");

        for (String line : lines) {
            TextView msgView = new TextView(getContext());
            msgView.setPadding(16, 16, 16, 16);
            msgView.setTextColor(Color.BLACK);
            msgView.setTextSize(16);

            if (line.startsWith("http") && line.contains("maps.google.com")) {
                SpannableString spannable = new SpannableString("📍 Navigate to location");
                spannable.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(line));
                        intent.setPackage("com.google.android.apps.maps");
                        startActivity(intent);
                    }
                }, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                msgView.setText(spannable);
                msgView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                msgView.setText("Bot: " + line);
            }

            chatContainer.addView(msgView);
        }
    }


    public void showRecommendations(List<LocationRecommendation> recommendations) {
        if (chatContainer == null || recommendations == null) return;

        chatContainer.removeAllViews(); // Clear previous

        for (LocationRecommendation rec : recommendations) {
            Button btn = new Button(getContext());
            btn.setText(rec.getLocation() + " (" + rec.getAvgSignalStrength() + " dBm)");
            btn.setAllCaps(false);
            btn.setPadding(16, 16, 16, 16);
            btn.setBackgroundColor(getSignalColor(rec.getAvgSignalStrength()));

            // Handle click to notify MainActivity
            btn.setOnClickListener(v -> {
                Log.d("Chatbot", "Clicked on: " + rec.getLocation());
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.addMarkerAndZoom(rec);
                }
            });

            chatContainer.addView(btn);
        }
    }

}


