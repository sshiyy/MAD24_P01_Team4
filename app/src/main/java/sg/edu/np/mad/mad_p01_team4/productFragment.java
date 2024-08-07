package sg.edu.np.mad.mad_p01_team4;

import android.Manifest;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class productFragment extends Fragment {

    public productFragment() {
        // Required empty public constructor
    }

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
    private LinearLayout voicePopup;
    private SpeechRecognitionHelper speechRecognitionHelper;

    private TextView noProductTextView;

    private Map<Integer, Class<? extends Fragment>> fragmentMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.productpage, container, false);

        // initializing
        drawerLayout = view.findViewById(R.id.drawer_layout);
        buttonDrawer = view.findViewById(R.id.buttonDrawerToggle);
        navigationView = view.findViewById(R.id.navigationView);
        searchEditText = view.findViewById(R.id.searchEditText);
        voicePopup = view.findViewById(R.id.voice_popup);
        noProductTextView = view.findViewById(R.id.noProductTextView);
        ImageButton voiceButton = view.findViewById(R.id.search_voice_btn);

        // voice button click listener
        voiceButton.setOnClickListener(v -> startVoiceRecognition());
        checkMicrophonePermission();

        // drawer button listener
        buttonDrawer.setOnClickListener(v -> drawerLayout.open());

        initializeFragmentMap();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            Log.d(TAG, "Navigation item clicked: " + itemId);
            displaySelectedFragment(itemId);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        db = FirebaseFirestore.getInstance();
        allFoodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(new ArrayList<>(), getContext(), R.layout.custom_itemlist_small); // Use the default layout for products


        setUpRecyclerView(view, R.id.productrecyclerView, foodAdapter);

        allRestaurantsText = view.findViewById(R.id.allRestaurantsText);
        sortedByText = view.findViewById(R.id.sortedByText);

        fetchFoodItems();

        // filter button click listener
        ImageButton filbtn = view.findViewById(R.id.filterIcon);
        filbtn.setOnClickListener(v -> showFilterPopup());

        // cart button click listener
        RelativeLayout cartbutton = view.findViewById(R.id.cart_button);
        cartbutton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new cartFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // floating action button click listener
        FloatingActionButton floatingActionButton = view.findViewById(R.id.chatbot_button);
        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), chatbot.class);
            startActivity(intent);
        });



        // search edit text
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch(v.getText().toString());
                return true;
            }
            return false;
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

        return view;
    }

    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MIC_PERMISSION);
        } else {
            initializeSpeechRecognitionHelper();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissMicPopup();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissMicPopup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognitionHelper != null) {
            speechRecognitionHelper.destroy();
        }
    }

    private void initializeSpeechRecognitionHelper() {
        Log.d(TAG, "Initializing SpeechRecognitionHelper");
        speechRecognitionHelper = new SpeechRecognitionHelper(getContext(), new SpeechRecognitionHelper.SpeechRecognitionListener() {
            @Override
            public void onReadyForSpeech() {
                showMicPopup();
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                dismissMicPopup();
                Log.e(TAG, "Speech recognition error: " + error);
                String errorMessage = getErrorText(error);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    startVoiceRecognition();
                }
            }

            @Override
            public void onResults(String result) {
                Log.d(TAG, "Recognized result: " + result);
                updateRecognizedText(result);
                handleVoiceCommand(result);
                dismissMicPopup();
            }

            @Override
            public void onPartialResults(String partialResult) {
                Log.d(TAG, "Partial result: " + partialResult);
                updateRecognizedText(partialResult);
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void startVoiceRecognition() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MIC_PERMISSION);
        } else {
            if (speechRecognitionHelper != null) {
                Log.d(TAG, "Starting voice recognition");
                speechRecognitionHelper.startListening();
                showMicPopup();
            } else {
                Log.e(TAG, "SpeechRecognitionHelper is not initialized");
            }
        }
    }

    private void showMicPopup() {
        voicePopup.setVisibility(View.VISIBLE);
    }

    private void dismissMicPopup() {
        if (voicePopup != null) {
            voicePopup.setVisibility(View.GONE);
        }
    }

    private void updateRecognizedText(String text) {
        Log.d(TAG, "Attempting to update recognized text: " + text);
        if (voicePopup != null && voicePopup.getVisibility() == View.VISIBLE) {
            TextView recognizedTextView = voicePopup.findViewById(R.id.recognized_text);
            if (recognizedTextView != null) {
                Log.d(TAG, "Updating recognized text: " + text);
                recognizedTextView.setText(text);
            } else {
                Log.e(TAG, "recognizedTextView is null");
            }
        } else {
            Log.e(TAG, "voicePopup is not visible or is null");
        }
    }

    private void handleVoiceCommand(String command) {
        Log.d(TAG, "Handling voice command: " + command);
        command = command.toLowerCase(Locale.ROOT);

        Fragment selectedFragment = null;

        if (command.contains("account")|| command.contains("profile")|| command.contains("account")) {
            Log.d(TAG, "Navigating to account fragment");
            selectedFragment = new profileFragment();
        } else if (command.contains("shopping") || command.contains("cart")|| command.contains("checkout")) {
            Log.d(TAG, "Navigating to cart fragment");
            selectedFragment = new cartFragment();
        } else if (command.contains("product")|| command.contains("Home")|| command.contains("Food")) {
            Log.d(TAG, "Navigating to product fragment");
            selectedFragment = new productFragment();
        } else if (command.contains("points")|| command.contains("rewards")) {
            Log.d(TAG, "Navigating to points fragment");
            selectedFragment = new pointsFragment();
        } else {
            Log.d(TAG, "Unrecognized command: " + command);
        }

        if (selectedFragment != null) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // method to set up and layout recyclerview
    private void setUpRecyclerView(View view, int recyclerViewId, RecyclerView.Adapter<?> adapter) {
        RecyclerView recyclerView = view.findViewById(recyclerViewId);
        int numberOfColumns = 2; // Set the number of columns for the grid layout
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), numberOfColumns);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    // fetch food items from food items collection
    private void fetchFoodItems() {
        db.collection("Food_Items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allFoodList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Food food = document.toObject(Food.class);
                            food.setModifications((List<Map<String, Object>>) document.get("modifications"));
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
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.activity_flitering_page, null);
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.MATCH_PARENT;
            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
            View mainLayout = getActivity().findViewById(android.R.id.content).getRootView();
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

    private final Map<String, double[]> priceRangeMap = new HashMap<String, double[]>() {{
        put("$0 - $10", new double[]{0, 10.0});
        put("$10 - $20", new double[]{10.0, 20.0});
        put("$20 and above", new double[]{20.0, Double.MAX_VALUE});
        put("All", new double[]{0, Double.MAX_VALUE});
    }};

    private final Map<String, String> priceRangeSymbolMap = new HashMap<String, String>() {{
        put("$0 - $10", "$0 - $10");
        put("$10 - $20", "$10 - $20");
        put("$20 and above", "$20 and above");
        put("All", "");
    }};

    private void applyFilter(String selectedCategory, String selectedPriceRange) {
        ArrayList<Food> filteredList = new ArrayList<>();
        double[] priceRange = priceRangeMap.getOrDefault(selectedPriceRange, new double[]{0, Double.MAX_VALUE});
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

        if (filteredList.isEmpty()) {
            noProductTextView.setVisibility(View.VISIBLE);
        } else {
            noProductTextView.setVisibility(View.GONE);
        }

        String priceRangeSymbol = priceRangeSymbolMap.getOrDefault(selectedPriceRange, "");

        if (selectedCategory.equals("All") && selectedPriceRange.equals("All")) {
            allRestaurantsText.setText("All");
            sortedByText.setText("sorted by category");
        } else if (selectedCategory.equals("All")) {
            allRestaurantsText.setText("All Categories");
            sortedByText.setText("sorted by " + priceRangeSymbol);
        } else if (selectedPriceRange.equals("All")) {
            allRestaurantsText.setText(selectedCategory);
            sortedByText.setText("sorted by " + selectedCategory);
        } else {
            allRestaurantsText.setText(selectedCategory);
            sortedByText.setText("sorted by " + selectedCategory + " " + priceRangeSymbol);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MIC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Microphone permission granted");
                initializeSpeechRecognitionHelper();
            } else {
                Toast.makeText(getContext(), "Microphone permission is required for speech recognition", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeFragmentMap() {
        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.navMenu, productFragment.class);
        fragmentMap.put(R.id.navCart, cartFragment.class);
        fragmentMap.put(R.id.navAccount, profileFragment.class);
        fragmentMap.put(R.id.navMap, mapFragment.class);
        fragmentMap.put(R.id.navPoints, pointsFragment.class);
        fragmentMap.put(R.id.navFavourite, FavoritesFragment.class);
        fragmentMap.put(R.id.navOngoingOrders, ongoingFragment.class);
        fragmentMap.put(R.id.navHistory, orderhistoryFragment.class);
        fragmentMap.put(R.id.navFavourite, FavoritesFragment.class);
    }

    private void displaySelectedFragment(int itemId) {
        Class<? extends Fragment> fragmentClass = fragmentMap.get(itemId);
        if (fragmentClass != null) {
            try {
                Fragment selectedFragment = fragmentClass.newInstance();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null)
                        .commit();
                Log.d(TAG, "Fragment transaction committed for: " + fragmentClass.getSimpleName());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                Log.e(TAG, "Error instantiating fragment: " + e.getMessage());
            } catch (java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }
        } else {
            Log.e(TAG, "Unknown navigation item selected: " + itemId);
        }
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error, please try again.";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client error, please try again.";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions, please enable microphone access.";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error, please check your connection and try again.";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout, please try again.";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "Match not found, please try again.";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "Recognition service is busy, please try again.";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Server error, please try again.";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input, please try again.";
                break;
            default:
                message = "Unknown error, please try again.";
                break;
        }
        return message;
    }
}
