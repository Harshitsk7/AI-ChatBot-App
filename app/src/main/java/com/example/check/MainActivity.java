package com.example.check;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.check.R;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.ai.generativelanguage.v1beta2.DiscussServiceClient;
import com.google.ai.generativelanguage.v1beta2.DiscussServiceSettings;
import com.google.ai.generativelanguage.v1beta2.GenerateMessageRequest;
import com.google.ai.generativelanguage.v1beta2.GenerateMessageResponse;
import com.google.ai.generativelanguage.v1beta2.Message;
import com.google.ai.generativelanguage.v1beta2.MessagePrompt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PALM_API_KEY = "AIzaSyBgrJ5UJYb-fdamS2pH5A_K2TaP6PemUQU";

    private DiscussServiceClient client;
    private List<Message> conversation;
    private LinearLayout chatContainer;
    private EditText userInputField;
//    private Button sendButton;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the DiscussServiceClient
        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-goog-api-key", PALM_API_KEY);

        InstantiatingGrpcChannelProvider provider = InstantiatingGrpcChannelProvider.newBuilder()
                .setHeaderProvider(FixedHeaderProvider.create(headers))
                .build();

        DiscussServiceSettings settings = null;
        try {
            settings = DiscussServiceSettings.newBuilder()
                    .setTransportChannelProvider(provider)
                    .setCredentialsProvider(FixedCredentialsProvider.create(null))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            client = DiscussServiceClient.create(settings);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize the conversation list
        conversation = new ArrayList<>();

        // Set up UI components
        chatContainer = findViewById(R.id.chatContainer);
        userInputField = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Set a click listener for the send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user input
                String userInput = userInputField.getText().toString().trim();

                if (!userInput.isEmpty()) {
                    // Create a message from user input
                    Message userMessage = Message.newBuilder()
                            .setAuthor("user")
                            .setContent(userInput)
                            .build();

                    // Add the user message to the conversation
                    conversation.add(userMessage);

                    // Generate a response from the chatbot
                    generateMessage();

                    // Clear the user input field
                    userInputField.setText("");

                    // Execute generateMessage on a background thread
                    new GenerateMessageTask().execute();
                }
            }
        });
    }

    // AsyncTask to execute generateMessage on a background thread
    private class GenerateMessageTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            generateMessage();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Display the conversation in the UI after generating the message
            displayConversation();
        }

        public void execute() {

        }
    }

    private void generateMessage() {
        // Create the GenerateMessageRequest
        GenerateMessageRequest request = GenerateMessageRequest.newBuilder()
                .setModel("models/chat-bison-001")
                .setPrompt(createMessagePrompt())
                .setTemperature(0.5f)
                .setCandidateCount(1)
                .build();

        // Send the request and get the response
        GenerateMessageResponse response;
        try {
            response = client.generateMessage(request);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
            return;
        }

        // Get the returned message
        Message returnedMessage = response.getCandidatesList().get(0);

        // Add the chatbot's response to the conversation
        conversation.add(returnedMessage);

        // Display the conversation in the UI
        displayConversation();
    }

    private MessagePrompt createMessagePrompt() {
        // Create the message prompt and examples
        MessagePrompt.Builder messagePromptBuilder = MessagePrompt.newBuilder();

        // Add all messages in the conversation
        for (Message message : conversation) {
            messagePromptBuilder.addMessages(message);
        }

        return messagePromptBuilder.build();
    }

    private void displayConversation() {
        chatContainer.removeAllViews();

        for (Message message : conversation) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            if (message.getAuthor().equals("user")) {
                // Sent message bubble
                TextView sentMessageTextView = new TextView(this);
                sentMessageTextView.setText(message.getContent());
                sentMessageTextView.setTextColor(getResources().getColor(android.R.color.black));
                sentMessageTextView.setBackgroundResource(R.drawable.sent_message_bubble);
                sentMessageTextView.setPadding(16, 8, 16, 8);
                layoutParams.setMargins(200, 8, 8, 8);
                sentMessageTextView.setLayoutParams(layoutParams);
                chatContainer.addView(sentMessageTextView);
            } else {
                // Received message bubble
                TextView receivedMessageTextView = new TextView(this);
                receivedMessageTextView.setText(message.getContent());
                receivedMessageTextView.setTextColor(getResources().getColor(android.R.color.black));
                receivedMessageTextView.setBackgroundResource(R.drawable.received_message_bubble);
                receivedMessageTextView.setPadding(16, 8, 16, 8);
                layoutParams.setMargins(8, 8, 200, 8);
                receivedMessageTextView.setLayoutParams(layoutParams);
                chatContainer.addView(receivedMessageTextView);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the DiscussServiceClient
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
