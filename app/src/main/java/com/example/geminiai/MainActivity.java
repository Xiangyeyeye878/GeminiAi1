package com.example.geminiai;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    String apiKey = "AIzaSyB8izLGxCxBsbaNfRq2_9cNDLNdxyAB96I";
    boolean useChat = false;
    //    String apiKey = BuildConfig.apikey;
    TextView textView;
    EditText editText;
    Button button;
    private final String LOG = "MainActivity1";

    GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
    GenerativeModelFutures model = GenerativeModelFutures.from(gm);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editTextText);
        button = findViewById(R.id.button);

        button.setOnClickListener(v -> {
            String input = editText.getText().toString();
            if (!input.isEmpty()) {
                sendMessage(input,model);
            }
        });
    }

    private void sendMessage(String userMessageText,GenerativeModelFutures model) {


        // 單次對話寫法
        /*
        Content content = new Content.Builder().addText("Write a story about a music backpack").build();
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                Log.d(LOG,"取得text成功");
                String resultText = result.getText();
              // 無法在非主線程編輯ui物件
                runOnUiThread(() -> textView.setText(resultText));
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(LOG,"取得失敗");
                t.printStackTrace();
            }
        },executor);
         */

        //多輪對話寫法
        //Create previous chat rule for context
//        Content.Builder SystemContentBuilder = new Content.Builder();
//        SystemContentBuilder.setRole("system");
//        SystemContentBuilder.addText("You are an English conversation chatbot, responsible for providing scenarios for conversations, such as at customs, asking for directions, or making a payment. You will only respond in English.");
//        Content SystemContent = SystemContentBuilder.build();
        // Create previous chat history for context
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText("How much does the bag cost?");
        Content userContent = userContentBuilder.build();

        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("You are an English conversation chatbot, responsible for providing scenarios for conversations, such as at customs, asking for directions, or making a payment. You will only respond in English.");
        Content modelContent = modelContentBuilder.build();

        List<Content> history = Arrays.asList(userContent, modelContent);
        // Initialize the chat
        ChatFutures chat = model.startChat(history);

        //Create a new user message
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(userMessageText);
        Content userMessage = userMessageBuilder.build();


        //注意
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userMessage);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                Log.d(LOG, "get text success");
                String resultText = result.getText();
                // 無法在非主線程編輯ui物件
                runOnUiThread(() -> textView.setText(resultText));
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(LOG, "get text fail" + t.toString());
                t.printStackTrace();
            }
        }, executor);
    }
}