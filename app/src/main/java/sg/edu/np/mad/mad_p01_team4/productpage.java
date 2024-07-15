package sg.edu.np.mad.mad_p01_team4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class productpage extends AppCompatActivity {

    private static final String TAG = "productpage";
    private static final int REQUEST_MIC_PERMISSION = 1;

    private FirebaseFirestore db;
    private ArrayList<Food> allFoodList;
    private FoodAdapter foodAdapter;
    private TextView allRestaurantsText;
    private TextView sortedByText;
    private EditText searchEditText;
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawer;
    private NavigationView navigationView;
    private PopupWindow micPopupWindow;
    private SpeechRecognitionHelper speechRecognitionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.productpage);

        drawerLayout = findViewById(R.id.drawer_layout);
        View buttonDrawerView = findViewById(R.id.buttonDrawerToggle);
        if (buttonDrawerView instanceof ImageButton) {
            buttonDrawer = (ImageButton) buttonDrawerView;
        }
        navigationView = findViewById(R.id.navigationView);
        buttonDrawer = findViewById(R.id.buttonDrawerToggle);

        searchEditText = findViewById(R.id.searchEditText);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch(v.getText().toString());
                return true;
            }
            return false;
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MIC_PERMISSION);
        } else {
            initializeSpeechRecognitionHelper();
        }

        buttonDrawer.setOnClickListener(v -> drawerLayout.open());

        View headerView = navigationView.getHeaderView(0);
        ImageView userImage = headerView.findViewById(R.id.userImage);
        TextView textusername = headerView.findViewById(R.id.textusername);

        userImage.setOnClickListener(v -> Toast.makeText(productpage.this, textusername.getText(), Toast.LENGTH_SHORT).show());

        navigationView.setNavigationItemSelectedListener(menuItem -> {
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
                Intent intent = new Intent(productpage.this, profilePage.class);
                startActivity(intent);
            }

            if (itemId == R.id.navMap) {
                Toast.makeText(productpage.this, "Map Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(productpage.this, activity_maps.class);
                startActivity(intent);
            }

            drawerLayout.close();
            return false;
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
        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(productpage.this, chatbot.class);
            startActivity(intent);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton voiceButton = findViewById(R.id.search_voice_btn);
        voiceButton.setOnClickListener(v -> startVoiceRecognition());
    }

    private void initializeSpeechRecognitionHelper() {
        speechRecognitionHelper = new SpeechRecognitionHelper(this, new SpeechRecognitionHelper.SpeechRecognitionListener() {
            @Override
            public void onReadyForSpeech() {
                showMicPopup();
            }

            @Override
            public void onBeginningOfSpeech() {
                // Optional: Show some indication that speech recognition has started
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Optional: Handle changes in the input signal
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Optional: Handle received buffer
            }

            @Override
            public void onEndOfSpeech() {
                // Optional: Show some indication that speech recognition has ended
            }

            @Override
            public void onError(int error) {
                dismissMicPopup();
                Log.e(TAG, "Speech recognition error: " + error);
            }

            @Override
            public void onResults(String result) {
                Log.d(TAG, "Recognized result: " + result);
                updateRecognizedText(result); // Update the Popup's TextView with the recognized text
                handleVoiceCommand(result); // Handle voice command and dismiss popup
            }

            @Override
            public void onPartialResults(String partialResult) {
                Log.d(TAG, "Partial result: " + partialResult);
                updateRecognizedText(partialResult); // Update the Popup's TextView with partial results
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Optional: Handle events
            }
        });
    }

    private void startVoiceRecognition() {
        speechRecognitionHelper.startListening();
        showMicPopup(); // Show the popup when starting voice recognition
    }

    private void showMicPopup() {
        try {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_mic_activity, null);
            int width = ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            boolean focusable = true;
            micPopupWindow = new PopupWindow(popupView, width, height, focusable);
            View mainLayout = findViewById(android.R.id.content).getRootView();
            micPopupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
            micPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } catch (Exception e) {
            Log.e(TAG, "Error showing mic popup", e);
        }
    }

    private void dismissMicPopup() {
        if (micPopupWindow != null && micPopupWindow.isShowing()) {
            micPopupWindow.dismiss();
        }
    }

    private void updateRecognizedText(String text) {
        if (micPopupWindow != null && micPopupWindow.isShowing()) {
            View popupView = micPopupWindow.getContentView();
            TextView recognizedTextView = popupView.findViewById(R.id.recognized_text);
            if (recognizedTextView != null) {
                Log.d(TAG, "Updating recognized text: " + text); // Log text to verify update
                recognizedTextView.setText(text);
            } else {
                Log.e(TAG, "recognizedTextView is null");
            }
        } else {
            Log.e(TAG, "micPopupWindow is not showing");
        }
    }

    private void handleVoiceCommand(String command) {
        command = command.toLowerCase(Locale.ROOT);
        dismissMicPopup(); // Close the popup before navigating
        if (command.contains("account page")) {
            Intent intent = new Intent(productpage.this, profilePage.class);
            startActivity(intent);
        } else if (command.contains("cart page")) {
            Intent intent = new Intent(productpage.this, cartpage.class);
            startActivity(intent);
        } else if (command.contains("menu page")) {
            Intent intent = new Intent(productpage.this, productpage.class);
            startActivity(intent);
        } // Add more commands as needed
    }

    private void setUpRecyclerView(int recyclerViewId, FoodAdapter adapter) {
        RecyclerView recyclerView = findViewById(recyclerViewId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
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
                            food.setModifications((List<Map<String, Object>>) document.get("modifications")); // Set modifications
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

    private void performSearch(String query) {
        ArrayList<Food> filteredList = new ArrayList<>();

        String lowercaseQuery = query.toLowerCase().trim();

        for (Food food : allFoodList) {
            if (food.getName().toLowerCase().contains(lowercaseQuery)) {
                filteredList.add(food);
            }
        }

        foodAdapter.updateList(filteredList);
    }

    private void clearSearch() {
        searchEditText.setText("");
        updateAllAdapters(allFoodList);
        allRestaurantsText.setText("All Categories");
        sortedByText.setText("sorted by category");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognitionHelper != null) {
            speechRecognitionHelper.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MIC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Microphone permission granted");
                initializeSpeechRecognitionHelper();
            } else {
                Toast.makeText(this, "Microphone permission is required for speech recognition", Toast.LENGTH_SHORT).show();
            }
        }
    }
}