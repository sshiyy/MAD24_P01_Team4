package sg.edu.np.mad.mad_p01_team4; // Package declaration for the project

import android.os.Bundle; // Import for handling Activity lifecycle events
import android.text.Editable; // Import for editable text interface
import android.text.TextWatcher; // Import for text change listener
import android.util.Log; // Import for logging
import android.view.MotionEvent; // Import for handling touch events
import android.view.View; // Import for handling view interactions
import android.widget.EditText; // Import for EditText widget
import android.widget.ImageView; // Import for ImageView widget
import com.google.android.material.chip.Chip; // Import for Chip component
import com.google.android.material.chip.ChipGroup; // Import for ChipGroup component

import androidx.annotation.NonNull; // Import for non-null annotation
import androidx.appcompat.app.AppCompatActivity; // Import for AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager; // Import for linear layout manager in RecyclerView
import androidx.recyclerview.widget.RecyclerView; // Import for RecyclerView

import com.google.android.gms.tasks.OnCompleteListener; // Import for completion listener for tasks
import com.google.android.gms.tasks.Task; // Import for handling asynchronous tasks
import com.google.firebase.firestore.CollectionReference; // Import for Firestore collection reference
import com.google.firebase.firestore.DocumentSnapshot; // Import for Firestore document snapshot
import com.google.firebase.firestore.FirebaseFirestore; // Import for Firestore database
import com.google.firebase.firestore.QuerySnapshot; // Import for Firestore query snapshot
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage; // Import for Firebase Natural Language
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply; // Import for Smart Reply
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage; // Import for text message in Smart Reply
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion; // Import for Smart Reply suggestion
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult; // Import for Smart Reply result

import java.text.SimpleDateFormat; // Import for formatting date
import java.util.ArrayList; // Import for ArrayList
import java.util.Date; // Import for Date class
import java.util.HashMap; // Import for HashMap
import java.util.List; // Import for List interface
import java.util.Locale; // Import for Locale settings
import java.util.Map; // Import for Map interface
import java.util.TimeZone; // Import for TimeZone settings

public class chatbot extends AppCompatActivity { // Main activity class extending AppCompatActivity

    RecyclerView recyclerView; // RecyclerView for displaying chat messages
    EditText editText; // EditText for user input
    ArrayList<MessageModel> list; // List to store chat messages
    ChatBotAdapter adapter; // Adapter for the RecyclerView
    FirebaseFirestore db; // Firestore database instance
    FirebaseSmartReply smartReply; // Smart Reply instance
    ChipGroup chipGroup; // ChipGroup for displaying suggested replies

    private final String user = "user"; // Constant for user identifier
    private final String bot = "bot"; // Constant for bot identifier
    private final Map<String, String> keywordMapping = new HashMap<>(); // Map for keyword to document ID mapping
    private final Map<String, String> predefinedResponses = new HashMap<>(); // Map for predefined responses
    private boolean isKeywordLoaded = false; // Flag to check if keywords are loaded

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate method
        super.onCreate(savedInstanceState); // Call to super class's onCreate method
        setContentView(R.layout.activity_chatbot); // Set the layout for this activity
        recyclerView = findViewById(R.id.recycler_view); // Find RecyclerView by ID
        editText = findViewById(R.id.edit); // Find EditText by ID
        chipGroup = findViewById(R.id.chip_group); // Find ChipGroup by ID
        list = new ArrayList<>(); // Initialize the message list
        adapter = new ChatBotAdapter(this, list); // Initialize the adapter with the message list
        db = FirebaseFirestore.getInstance(); // Get Firestore instance
        smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply(); // Get Smart Reply instance

        // Initialize keyword mappings
        initializeKeywordMappings();

        // Initialize predefined responses
        initializePredefinedResponses();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this); // Create a linear layout manager
        recyclerView.setAdapter(adapter); // Set adapter to RecyclerView
        recyclerView.setLayoutManager(linearLayoutManager); // Set layout manager to RecyclerView

        sendWelcomeMessage(); // Send a welcome message to the user

        editText.setOnTouchListener(new View.OnTouchListener() { // Set touch listener on EditText
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) { // Check for touch release action
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                        String message = editText.getText().toString().trim(); // Get text from EditText
                        if (!message.isEmpty()) {
                            list.add(new MessageModel(message, user, getCurrentTime())); // Add user message to list
                            adapter.notifyDataSetChanged(); // Notify adapter of data change
                            recyclerView.smoothScrollToPosition(list.size() - 1); // Scroll to the last message
                            handleUserMessage(message); // Handle user message
                            editText.setText(""); // Clear EditText
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
                updateSuggestions(s.toString()); // Update suggestions based on text input
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
        CollectionReference faqsRef = db.collection("FAQs"); // Reference to FAQs collection in Firestore
        faqsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() { // Fetch documents from collection
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String keyword = document.getString("keyword"); // Get keyword field
                        String documentId = document.getId(); // Get document ID
                        if (keyword != null && !keyword.isEmpty()) {
                            String[] words = keyword.toLowerCase().split("\\s+"); // Split keyword into words
                            for (String word : words) {
                                keywordMapping.put(word, documentId); // Map word to document ID
                            }
                            Log.d("Firestore", "Keyword: " + keyword + " Document ID: " + documentId);
                        }
                    }
                    isKeywordLoaded = true; // Set flag to true when keywords are loaded
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
        predefinedResponses.put("thanks", "You're welcome!");
        predefinedResponses.put("thank you", "You're welcome!");
    }

    private void sendWelcomeMessage() {
        String welcomeMessage = "Welcome To Enchante ! How can I assist you today?";
        list.add(new MessageModel(welcomeMessage, bot, getCurrentTime())); // Add welcome message to list
        adapter.notifyDataSetChanged(); // Notify adapter of data change
        recyclerView.smoothScrollToPosition(list.size() - 1); // Scroll to the last message

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
        chipGroup.removeAllViews(); // Remove all existing chips
        for (String suggestion : suggestions) {
            Chip chip = new Chip(chatbot.this); // Create a new Chip
            chip.setText(suggestion); // Set text on the Chip
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.add(new MessageModel(suggestion, user, getCurrentTime())); // Add user message to list
                    adapter.notifyDataSetChanged(); // Notify adapter of data change
                    recyclerView.smoothScrollToPosition(list.size() - 1); // Scroll to the last message
                    handleUserMessage(suggestion); // Handle user message
                }
            });
            chipGroup.addView(chip); // Add Chip to ChipGroup
        }
    }

    private void handleUserMessage(String message) {
        if (!isKeywordLoaded) {
            list.add(new MessageModel("Keywords are still loading. Please try again shortly.", bot, getCurrentTime())); // Inform user if keywords are not loaded
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(list.size() - 1);
            return;
        }

        String lowerCaseMessage = message.toLowerCase().trim(); // Convert message to lower case and trim
        if (predefinedResponses.containsKey(lowerCaseMessage)) {
            list.add(new MessageModel(predefinedResponses.get(lowerCaseMessage), bot, getCurrentTime())); // Respond with predefined response
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(list.size() - 1);
            return;
        }

        String documentId = getDocumentIdForMessage(lowerCaseMessage); // Get document ID for message
        if (documentId != null) {
            fetchFAQAnswer(documentId); // Fetch FAQ answer if document ID is found
        } else {
            // Check for typos and find the closest matching keyword
            String closestKeyword = findClosestKeyword(lowerCaseMessage);
            if (closestKeyword != null) {
                suggestCorrection(closestKeyword); // Suggest correction for closest keyword
            } else {
                // Use Smart Reply to generate suggestions
                generateSmartReply(lowerCaseMessage, message); // Generate Smart Reply suggestions
            }
        }

        // Update suggestions based on the user's input
        updateSuggestions(lowerCaseMessage);
    }

    private void suggestCorrection(String suggestedKeyword) {
        list.add(new MessageModel("Did you mean '" + suggestedKeyword + "'?", bot, getCurrentTime())); // Suggest correction
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(list.size() - 1);

        // Automatically fetch the FAQ answer for the suggested keyword
        fetchFAQAnswer(keywordMapping.get(suggestedKeyword));
    }

    private String getDocumentIdForMessage(String message) {
        String[] words = message.toLowerCase().split("\\s+"); // Split message into words
        for (String word : words) {
            if (keywordMapping.containsKey(word)) {
                return keywordMapping.get(word); // Return document ID if keyword is found
            }
        }
        return null;
    }

    private void updateSuggestions(String message) {
        chipGroup.removeAllViews(); // Remove all existing chips
        String[] suggestions = {"How can I place my order ?",
                "Can i customize my order ?",
                "How to track my order",
                "What are the payment methods available ?",
                "How to forget password ?",
                "Is the security good for my payment ?",
                "Where to update account settings ?"};
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().contains(message.toLowerCase())) {
                Chip chip = new Chip(chatbot.this); // Create a new Chip
                chip.setText(suggestion); // Set text on the Chip
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        list.add(new MessageModel(suggestion, user, getCurrentTime())); // Add user message to list
                        adapter.notifyDataSetChanged(); // Notify adapter of data change
                        recyclerView.smoothScrollToPosition(list.size() - 1); // Scroll to the last message
                        handleUserMessage(suggestion); // Handle user message
                    }
                });
                chipGroup.addView(chip); // Add Chip to ChipGroup
            }
        }
    }

    private void generateSmartReply(String lowerCaseMessage, String originalMessage) {
        List<FirebaseTextMessage> conversation = new ArrayList<>(); // List to store conversation messages
        for (MessageModel msg : list) {
            if (msg.getSender().equals(user)) {
                conversation.add(FirebaseTextMessage.createForLocalUser(msg.getMessage(), System.currentTimeMillis())); // Add user message to conversation
            } else {
                conversation.add(FirebaseTextMessage.createForRemoteUser(msg.getMessage(), System.currentTimeMillis(), bot)); // Add bot message to conversation
            }
        }

        smartReply.suggestReplies(conversation) // Generate Smart Reply suggestions
                .addOnCompleteListener(new OnCompleteListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SmartReplySuggestionResult> task) {
                        if (task.isSuccessful()) {
                            SmartReplySuggestionResult result = task.getResult();
                            if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                                list.add(new MessageModel("I don't understand the question.", bot, getCurrentTime())); // Respond if language is not supported
                            } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                List<SmartReplySuggestion> suggestions = result.getSuggestions();
                                if (!suggestions.isEmpty()) {
                                    boolean validSuggestionFound = false;
                                    chipGroup.removeAllViews(); // Remove all existing chips
                                    for (SmartReplySuggestion suggestion : suggestions) {
                                        String reply = suggestion.getText();
                                        if (!isInvalidSmartReply(reply)) {
                                            Chip chip = new Chip(chatbot.this); // Create a new Chip
                                            chip.setText(reply); // Set text on the Chip
                                            chip.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    list.add(new MessageModel(reply, user, getCurrentTime())); // Add user message to list
                                                    adapter.notifyDataSetChanged(); // Notify adapter of data change
                                                    recyclerView.smoothScrollToPosition(list.size() - 1); // Scroll to the last message
                                                    handleUserMessage(reply); // Handle user message
                                                }
                                            });
                                            chipGroup.addView(chip); // Add Chip to ChipGroup
                                            validSuggestionFound = true;
                                        }
                                    }
                                    if (!validSuggestionFound) {
                                        list.add(new MessageModel("I don't understand the question.", bot, getCurrentTime())); // Respond if no valid suggestions
                                        Log.d("SmartReply", "No valid suggestions available.");
                                    }
                                } else {
                                    list.add(new MessageModel("I don't understand the question.", bot, getCurrentTime())); // Respond if no suggestions available
                                    Log.d("SmartReply", "No suggestions available.");
                                }
                            }
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(list.size() - 1);
                        } else {
                            Log.e("SmartReply", "Smart Reply task failed", task.getException());
                            list.add(new MessageModel("I don't understand the question.", bot, getCurrentTime())); // Respond if Smart Reply task fails
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(list.size() - 1);
                        }
                    }
                });
    }

    private boolean isInvalidSmartReply(String reply) {
        String lowerReply = reply.toLowerCase();
        return lowerReply.contains("ok") || lowerReply.contains("okay") || lowerReply.contains("yes") || lowerReply.contains("no") || lowerReply.contains("emoji"); // Check for invalid replies
    }

    private void fetchFAQAnswer(String documentId) {
        CollectionReference faqsRef = db.collection("FAQs"); // Reference to FAQs collection in Firestore
        faqsRef.document(documentId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String answer = task.getResult().getString("answer"); // Get answer from document
                            list.add(new MessageModel(answer, bot, getCurrentTime())); // Add answer to list
                        } else {
                            list.add(new MessageModel("Failed to fetch the answer.", bot, getCurrentTime())); // Respond if fetching fails
                        }
                        adapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(list.size() - 1);
                    }
                });
    }

    private int getLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1]; // Create DP table

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j; // Initialize first row
                } else if (j == 0) {
                    dp[i][j] = i; // Initialize first column
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)); // Calculate Levenshtein distance
                }
            }
        }

        return dp[s1.length()][s2.length()]; // Return Levenshtein distance
    }

    private String findClosestKeyword(String message) {
        String closestKeyword = null;
        int minDistance = Integer.MAX_VALUE;

        for (String keyword : keywordMapping.keySet()) {
            int distance = getLevenshteinDistance(message.toLowerCase(), keyword.toLowerCase()); // Calculate distance
            if (distance < minDistance) {
                minDistance = distance;
                closestKeyword = keyword;
            }
        }

        // Consider a keyword only if the distance is below or equal to a certain threshold (e.g., 2)
        return minDistance <= 2 ? closestKeyword : null;
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault()); // Format time
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore")); // Set time zone
        return sdf.format(new Date()); // Return current time
    }

}
