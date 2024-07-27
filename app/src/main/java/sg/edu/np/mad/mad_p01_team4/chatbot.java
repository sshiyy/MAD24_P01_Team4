package sg.edu.np.mad.mad_p01_team4;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class chatbot extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText editText;
    ArrayList<MessageModel> list;
    ChatBotAdapter adapter;
    FirebaseFirestore db;
    FirebaseSmartReply smartReply;
    ChipGroup chipGroup;

    private final String user = "user";
    private final String bot = "bot";
    private final Map<String, String> keywordMapping = new HashMap<>();
    private final Map<String, String> predefinedResponses = new HashMap<>();
    private boolean isKeywordLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        recyclerView = findViewById(R.id.recycler_view);
        editText = findViewById(R.id.edit);
        chipGroup = findViewById(R.id.chip_group);
        list = new ArrayList<>();
        adapter = new ChatBotAdapter(this, list);
        db = FirebaseFirestore.getInstance();
        smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();

        // Initialize keyword mappings
        initializeKeywordMappings();

        // Initialize predefined responses
        initializePredefinedResponses();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        sendWelcomeMessage();

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                        String message = editText.getText().toString().trim();
                        if (!message.isEmpty()) {
                            list.add(new MessageModel(message, user, getCurrentTime()));
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(list.size() - 1);
                            handleUserMessage(message);
                            editText.setText("");
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        // Add TextWatcher to EditText
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSuggestions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Close button functionality
        ImageView closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }

    private void initializeKeywordMappings() {
        CollectionReference faqsRef = db.collection("FAQs");
        faqsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String keyword = document.getString("keyword");
                        String documentId = document.getId(); // Get the document ID
                        if (keyword != null && !keyword.isEmpty()) {
                            String[] words = keyword.toLowerCase().split("\\s+");
                            for (String word : words) {
                                keywordMapping.put(word, documentId);
                            }
                            Log.d("Firestore", "Keyword: " + keyword + " Document ID: " + documentId);
                        }
                    }
                    isKeywordLoaded = true;
                    Log.d("Firestore", "Keywords successfully loaded");
                } else {
                    Log.w("Firestore", "Error getting keywords.", task.getException());
                }
            }
        });
    }

    private void initializePredefinedResponses() {
        predefinedResponses.put("hi", "Hi, nice to meet you! Do you need help?");
        predefinedResponses.put("hello", "Hello! How can I assist you today?");
        predefinedResponses.put("yes", "Great! What do you need help with?");
        predefinedResponses.put("no", "Alright. If you have any questions, feel free to ask.");
        predefinedResponses.put("thanks", "You're welcome!");
        predefinedResponses.put("thank you", "You're welcome!");
    }

    private void sendWelcomeMessage() {
        String welcomeMessage = "Welcome! How can I assist you today? You can ask me questions like 'how to pay' or 'what are the payment methods'.";
        list.add(new MessageModel(welcomeMessage, bot, getCurrentTime()));
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(list.size() - 1);

        // Add predefined suggestions to the ChipGroup
        String[] suggestions = {
                "How can I order?",
                "Customize order",
                "Track my order",
                "What are the payment methods?",
                "How to forget password?",
                "Is my payment info secure?",
                "Update account settings"
        };
        chipGroup.removeAllViews();
        for (String suggestion : suggestions) {
            Chip chip = new Chip(chatbot.this);
            chip.setText(suggestion);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.add(new MessageModel(suggestion, user, getCurrentTime()));
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(list.size() - 1);
                    handleUserMessage(suggestion);
                }
            });
            chipGroup.addView(chip);
        }
    }

    private void handleUserMessage(String message) {
        if (!isKeywordLoaded) {
            list.add(new MessageModel("Keywords are still loading. Please try again shortly.", bot, getCurrentTime()));
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(list.size() - 1);
            return;
        }

        String lowerCaseMessage = message.toLowerCase().trim();
        if (predefinedResponses.containsKey(lowerCaseMessage)) {
            list.add(new MessageModel(predefinedResponses.get(lowerCaseMessage), bot, getCurrentTime()));
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(list.size() - 1);
            return;
        }

        String documentId = getDocumentIdForMessage(lowerCaseMessage);
        if (documentId != null) {
            fetchFAQAnswer(documentId);
        } else {
            // Check for typos and find the closest matching keyword
            String closestKeyword = findClosestKeyword(lowerCaseMessage);
            if (closestKeyword != null) {
                list.add(new MessageModel("Did you mean '" + closestKeyword + "'?", bot, getCurrentTime()));
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(list.size() - 1);
            } else {
                // If no keyword is found, generate Smart Reply suggestions
                generateSmartReply(lowerCaseMessage);
            }
        }

        // Update suggestions based on the user's input
        updateSuggestions(lowerCaseMessage);
    }

    private String getDocumentIdForMessage(String message) {
        String[] words = message.toLowerCase().split("\\s+");
        for (String word : words) {
            if (keywordMapping.containsKey(word)) {
                return keywordMapping.get(word);
            }
        }
        return null;
    }


    private void updateSuggestions(String message) {
        chipGroup.removeAllViews();
        String[] suggestions = {"How can I order?",
                "Customize order",
                "Track my order",
                "What are the payment methods?",
                "How to forget password?",
                "Is my payment info secure?",
                "Update account settings"};
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().contains(message.toLowerCase())) {
                Chip chip = new Chip(chatbot.this);
                chip.setText(suggestion);
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        list.add(new MessageModel(suggestion, user, getCurrentTime()));
                        adapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(list.size() - 1);
                        handleUserMessage(suggestion);
                    }
                });
                chipGroup.addView(chip);
            }
        }
    }

    private void generateSmartReply(String message) {
        List<FirebaseTextMessage> conversation = new ArrayList<>();
        for (MessageModel msg : list) {
            if (msg.getSender().equals(user)) {
                conversation.add(FirebaseTextMessage.createForLocalUser(msg.getMessage(), System.currentTimeMillis()));
            } else {
                conversation.add(FirebaseTextMessage.createForRemoteUser(msg.getMessage(), System.currentTimeMillis(), bot));
            }
        }

        smartReply.suggestReplies(conversation)
                .addOnCompleteListener(new OnCompleteListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SmartReplySuggestionResult> task) {
                        if (task.isSuccessful()) {
                            SmartReplySuggestionResult result = task.getResult();
                            if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                                list.add(new MessageModel("I don't understand the question.", bot, getCurrentTime()));
                            } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                List<SmartReplySuggestion> suggestions = result.getSuggestions();
                                if (!suggestions.isEmpty()) {
                                    boolean validSuggestionFound = false;
                                    for (SmartReplySuggestion suggestion : suggestions) {
                                        String reply = suggestion.getText();
                                        if (!isInvalidSmartReply(reply)) {
                                            Chip chip = new Chip(chatbot.this);
                                            chip.setText(reply);
                                            chip.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    list.add(new MessageModel(reply, user, getCurrentTime()));
                                                    adapter.notifyDataSetChanged();
                                                    recyclerView.smoothScrollToPosition(list.size() - 1);
                                                    handleUserMessage(reply);
                                                }
                                            });
                                            chipGroup.addView(chip);
                                            validSuggestionFound = true;
                                        }
                                    }
                                    if (!validSuggestionFound) {
                                        list.add(new MessageModel("I don't understand the question.", bot, getCurrentTime()));
                                        Log.d("SmartReply", "No valid suggestions available.");
                                    }
                                } else {
                                    list.add(new MessageModel("I don't understand the question.", bot, getCurrentTime()));
                                    Log.d("SmartReply", "No suggestions available.");
                                }
                            }
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(list.size() - 1);
                        } else {
                            Log.e("SmartReply", "Smart Reply task failed", task.getException());
                            list.add(new MessageModel("I don't understand the question.", bot, getCurrentTime()));
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(list.size() - 1);
                        }
                    }
                });
    }

    private boolean isInvalidSmartReply(String reply) {
        String lowerReply = reply.toLowerCase();
        return lowerReply.contains("ok") || lowerReply.contains("okay") || lowerReply.contains("yes") || lowerReply.contains("no") || lowerReply.contains("emoji");
    }


    private void fetchFAQAnswer(String documentId) {
        CollectionReference faqsRef = db.collection("FAQs");
        faqsRef.document(documentId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String answer = task.getResult().getString("answer");
                            list.add(new MessageModel(answer, bot, getCurrentTime()));
                        } else {
                            list.add(new MessageModel("Failed to fetch the answer.", bot, getCurrentTime()));
                        }
                        adapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(list.size() - 1);
                    }
                });
    }

    private int getLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private String findClosestKeyword(String message) {
        String closestKeyword = null;
        int minDistance = Integer.MAX_VALUE;

        for (String keyword : keywordMapping.keySet()) {
            int distance = getLevenshteinDistance(message.toLowerCase(), keyword.toLowerCase());
            if (distance < minDistance) {
                minDistance = distance;
                closestKeyword = keyword;
            }
        }

        // Consider a keyword only if the distance is below or equal to a certain threshold (e.g., 2)
        return minDistance <= 2 ? closestKeyword : null;
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        return sdf.format(new Date());
    }

}
