package sg.edu.np.mad.mad_p01_team4;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class chatbot extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText editText;
    ArrayList<MessageModel> list;
    ChatBotAdapter adapter;
    FirebaseFirestore db;

    private final String user = "user";
    private final String bot = "bot";
    private final Map<String, String> keywordMapping = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        recyclerView = findViewById(R.id.recycler_view);
        editText = findViewById(R.id.edit);
        list = new ArrayList<>();
        adapter = new ChatBotAdapter(this, list);
        db = FirebaseFirestore.getInstance();

        // Initialize keyword mappings
        initializeKeywordMappings();

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
                            list.add(new MessageModel(message, user));
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
        // Add mappings from keywords to document IDs
        keywordMapping.put("pay", "how_to_pay");
        keywordMapping.put("payment methods", "payment_methods");
        keywordMapping.put("order", "how_to_order_food");
        keywordMapping.put("track order", "track_order");
        keywordMapping.put("refund policy", "refund_policy");
        keywordMapping.put("contact support", "contact_support");
        // Add more mappings as needed
    }

    private void sendWelcomeMessage() {
        String welcomeMessage = "Welcome! How can I assist you today? You can ask me questions like 'how to pay' or 'what are the payment methods'.";
        list.add(new MessageModel(welcomeMessage, bot));
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(list.size() - 1);
    }

    private void handleUserMessage(String message) {
        String documentId = getDocumentIdForMessage(message);
        if (documentId != null) {
            fetchFAQAnswer(documentId);
        } else {
            list.add(new MessageModel("I don't understand the question.", bot));
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(list.size() - 1);
        }
    }

    private String getDocumentIdForMessage(String message) {
        for (String keyword : keywordMapping.keySet()) {
            if (message.toLowerCase().contains(keyword)) {
                return keywordMapping.get(keyword);
            }
        }
        return null;
    }

    private void fetchFAQAnswer(String documentId) {
        CollectionReference faqsRef = db.collection("FAQs");
        faqsRef.document(documentId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String answer = task.getResult().getString("answer");
                            list.add(new MessageModel(answer, bot));
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(list.size() - 1);
                        } else {
                            list.add(new MessageModel("Failed to fetch the answer.", bot));
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(list.size() - 1);
                        }
                    }
                });
    }
}
