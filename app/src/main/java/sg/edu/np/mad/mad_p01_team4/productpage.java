package sg.edu.np.mad.mad_p01_team4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class productpage extends AppCompatActivity {

    private static final String TAG = "productpage";

    private FirebaseFirestore db;
    private ArrayList<Food> allFoodList;
    private FoodAdapter foodAdapter;
    private TextView allRestaurantsText;
    private TextView sortedByText;

    DrawerLayout drawerLayout;
    ImageButton buttonDrawer;
    NavigationView navigationView;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.productpage);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        buttonDrawer = findViewById(R.id.buttonDrawerToggle);

        // Check and request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        buttonDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });

        View headerView = navigationView.getHeaderView(0);
        ImageView userImage = headerView.findViewById(R.id.userImage);
        TextView textusername = headerView.findViewById(R.id.textusername);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(productpage.this, textusername.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.navMenu) {
                    Toast.makeText(productpage.this, "Menu Clicked", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(productpage.this, productpage.class);
                    startActivity(intent);
                }

                if (itemId == R.id.navCart) {
                    Toast.makeText(productpage.this, "Cart Clicked", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(productpage.this, cartpage.class);
                    startActivity(intent);
                }

                if (itemId == R.id.navFavourite) {
                    Toast.makeText(productpage.this, "Favourite Clicked", Toast.LENGTH_SHORT).show();
                }

                if (itemId == R.id.navOngoingOrders) {
                    Toast.makeText(productpage.this, "Ongoing Orders Clicked", Toast.LENGTH_SHORT).show();
                }

                if (itemId == R.id.navHistory) {
                    Toast.makeText(productpage.this, "Order History Clicked", Toast.LENGTH_SHORT).show();
                }

                if (itemId == R.id.navAccount) {
                    Toast.makeText(productpage.this, "Account Clicked", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(productpage.this, ProfilePage.class);
                    startActivity(intent);
                }

                drawerLayout.close();
                return false;
            }
        });

        db = FirebaseFirestore.getInstance();
        allFoodList = new ArrayList<>();

        foodAdapter = new FoodAdapter(new ArrayList<>(), this);
        setUpRecyclerView(R.id.productrecyclerView, foodAdapter);

        allRestaurantsText = findViewById(R.id.allRestaurantsText);
        sortedByText = findViewById(R.id.sortedByText);

        fetchFoodItems();

        ImageButton filbtn = findViewById(R.id.filterIcon);
        filbtn.setOnClickListener(v -> showFilterPopup());

        RelativeLayout cartbutton = findViewById(R.id.cart_button);
        cartbutton.setOnClickListener(v -> {
            Intent intent = new Intent(productpage.this, cartpage.class);
            startActivity(intent);
        });

        FloatingActionButton floatingActionButton = findViewById(R.id.chatbot_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(productpage.this, chatbot.class);
                startActivity(intent);
            }
        });

        // Initialize SpeechRecognizer
        initializeSpeechRecognizer();

        // Set OnClickListener for the voice search button
        ImageButton searchVoiceButton = findViewById(R.id.search_voice_btn);
        searchVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(productpage.this, "Voice button clicked", Toast.LENGTH_SHORT).show();
                startListening();
            }
        });
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(productpage.this, "Listening...", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                Log.d(TAG, "onRmsChanged: " + rmsdB);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.d(TAG, "onBufferReceived");
            }

            @Override
            public void onEndOfSpeech() {
                Toast.makeText(productpage.this, "End of speech", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onEndOfSpeech");
            }

            @Override
            public void onError(int error) {
                String errorMessage = getErrorText(error);
                Toast.makeText(productpage.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onError: " + errorMessage);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    for (String result : matches) {
                        Log.d(TAG, "Result: " + result);
                        if (result.toLowerCase().contains("pay") || result.toLowerCase().contains("make payment")) {
                            Intent intent = new Intent(productpage.this, cartpage.class);
                            startActivity(intent);
                            return;
                        }
                    }
                    Toast.makeText(productpage.this, "Command not recognized", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.d(TAG, "onPartialResults");
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.d(TAG, "onEvent: " + eventType);
            }
        });
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognizer busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "Speech timeout";
            default:
                return "Didn't understand, please try again.";
        }
    }

    private void startListening() {
        speechRecognizer.startListening(speechRecognizerIntent);
        Toast.makeText(productpage.this, "Started listening", Toast.LENGTH_SHORT).show();
    }

    private void setUpRecyclerView(int recyclerViewId, FoodAdapter adapter) {
        RecyclerView recyclerView = findViewById(recyclerViewId);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void fetchFoodItems() {
        db.collection("Food_Items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allFoodList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Food food = document.toObject(Food.class);
                            allFoodList.add(food);
                        }
                        updateAllAdapters(allFoodList);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void updateAllAdapters(ArrayList<Food> foodList) {
        foodAdapter.updateList(foodList);
    }

    private void showFilterPopup() {
        try {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.activity_flitering_page, null);
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.MATCH_PARENT;
            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
            View mainLayout = findViewById(android.R.id.content).getRootView();
            popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            Button applyButton = popupView.findViewById(R.id.applyButton);
            applyButton.setOnClickListener(v -> {
                Spinner categorySpinner = popupView.findViewById(R.id.spinnerCategory);
                Spinner priceSpinner = popupView.findViewById(R.id.spinnerPrice);
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                String selectedPriceRange = priceSpinner.getSelectedItem().toString();

                applyFilter(selectedCategory, selectedPriceRange);
                popupWindow.dismiss();
            });

            Button closeButton = popupView.findViewById(R.id.cancelButton);
            closeButton.setOnClickListener(v -> popupWindow.dismiss());
        } catch (Exception e) {
            Log.e(TAG, "Error showing filter popup", e);
        }
    }

    private void applyFilter(String selectedCategory, String selectedPriceRange) {
        ArrayList<Food> filteredList = new ArrayList<>();
        double[] priceRange = getPriceRange(selectedPriceRange);
        double minPrice = priceRange[0];
        double maxPrice = priceRange[1];

        for (Food food : allFoodList) {
            boolean matchesCategory = selectedCategory.equals("All") || food.getCategory().equals(selectedCategory);
            boolean matchesPrice = food.getPrice() >= minPrice && food.getPrice() <= maxPrice;

            if ((selectedCategory.equals("All") && matchesPrice) || (matchesCategory && matchesPrice)) {
                filteredList.add(food);
            }
        }

        updateAllAdapters(filteredList);

        if (selectedCategory.equals("All") && selectedPriceRange.equals("All")) {
            allRestaurantsText.setText("All");
            sortedByText.setText("sorted by category");
        } else if (selectedCategory.equals("All")) {
            allRestaurantsText.setText("All Categories");
            sortedByText.setText("sorted by " + getPriceRangeSymbol(selectedPriceRange));
        } else if (selectedPriceRange.equals("All")) {
            allRestaurantsText.setText(selectedCategory);
            sortedByText.setText("sorted by " + selectedCategory);
        } else {
            allRestaurantsText.setText(selectedCategory);
            sortedByText.setText("sorted by " + selectedCategory + " " + getPriceRangeSymbol(selectedPriceRange));
        }
    }

    private double[] getPriceRange(String priceRange) {
        switch (priceRange) {
            case "$":
                return new double[]{0, 10.0};
            case "$$":
                return new double[]{10.0, 20.0};
            case "$$$":
                return new double[]{20.0, Double.MAX_VALUE};
            default:
                return new double[]{0, Double.MAX_VALUE};
        }
    }

    private String getPriceRangeSymbol(String priceRange) {
        switch (priceRange) {
            case "$":
                return "$";
            case "$$":
                return "$$";
            case "$$$":
                return "$$$";
            default:
                return "";
        }
    }

    private void clearFilter() {
        allRestaurantsText.setText("All Category");
        sortedByText.setText("sorted by category");
        updateAllAdapters(allFoodList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
