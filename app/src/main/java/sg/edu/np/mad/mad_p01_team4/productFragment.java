package sg.edu.np.mad.mad_p01_team4;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class productFragment extends Fragment {

    // Default constructor - every fragment needs it
    public productFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "productpage";
    private static final int REQUEST_MIC_PERMISSION = 1;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private ArrayList<Food> allFoodList;
    private FoodAdapter foodAdapter;
    private FoodAdapter popularFoodAdapter;
    private TextView allRestaurantsText;
    private TextView sortedByText;
    private EditText searchEditText;
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawer;
    private NavigationView navigationView;
    private LinearLayout voicePopup;
    private SpeechRecognitionHelper speechRecognitionHelper;

    private TextView noProductTextView;

    // Map to store the relationship between menu item IDs and fragment classes
    private Map<Integer, Class<? extends Fragment>> fragmentMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.productpage, container, false);

        drawerLayout = view.findViewById(R.id.drawer_layout);
        buttonDrawer = view.findViewById(R.id.buttonDrawerToggle);
        navigationView = view.findViewById(R.id.navigationView);
        searchEditText = view.findViewById(R.id.searchEditText);
        voicePopup = view.findViewById(R.id.voice_popup); // Initialize the voice popup
        noProductTextView = view.findViewById(R.id.noProductTextView); //for apply filter - when no products text view

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch(v.getText().toString());
                return true;
            }
            return false;
        });

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MIC_PERMISSION);
        } else {
            initializeSpeechRecognitionHelper();
        }

        buttonDrawer.setOnClickListener(v -> drawerLayout.open());


        initializeFragmentMap();

        // Set up the navigation drawer
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            Log.d(TAG, "Navigation item clicked: " + itemId); // Log the item ID
            displaySelectedFragment(itemId);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        db = FirebaseFirestore.getInstance();

        allFoodList = new ArrayList<>();

        foodAdapter = new FoodAdapter(new ArrayList<>(), getContext());
        setUpRecyclerView(view, R.id.productrecyclerView, foodAdapter);


        allRestaurantsText = view.findViewById(R.id.allRestaurantsText);
        sortedByText = view.findViewById(R.id.sortedByText);

        fetchFoodItems();

        ImageButton filbtn = view.findViewById(R.id.filterIcon);
        filbtn.setOnClickListener(v -> showFilterPopup());

        RelativeLayout cartbutton = view.findViewById(R.id.cart_button);
        cartbutton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new cartFragment())
                    .addToBackStack(null)
                    .commit();
        });

        FloatingActionButton floatingActionButton = view.findViewById(R.id.chatbot_button);
        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), chatbot.class);
            startActivity(intent);
        });

        // Set OnClickListener for viewAllButton
        Button viewAllButton = view.findViewById(R.id.viewallbutton);
        viewAllButton.setOnClickListener(v -> {
            // Navigate to the ViewAllActivity
            Intent intent = new Intent(getActivity(), viewallproduct.class);
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

        ImageButton voiceButton = view.findViewById(R.id.search_voice_btn);
        voiceButton.setOnClickListener(v -> startVoiceRecognition());

        return view;
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

    private void initializeSpeechRecognitionHelper() {
        speechRecognitionHelper = new SpeechRecognitionHelper(getContext(), new SpeechRecognitionHelper.SpeechRecognitionListener() {
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
                dismissMicPopup();
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

        if (command.contains("account")) {
            Log.d(TAG, "Navigating to account fragment");
            selectedFragment = new profileFragment();
        } else if (command.contains("shopping")) {
            Log.d(TAG, "Navigating to cart fragment");
            selectedFragment = new cartFragment();
        } else if (command.contains("product")) {
            Log.d(TAG, "Navigating to product fragment");
            selectedFragment = new productFragment();
        } else if (command.contains("points")) {
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

    private void setUpRecyclerView(View view, int recyclerViewId, FoodAdapter adapter) {
        RecyclerView recyclerView = view.findViewById(recyclerViewId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
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
        put("$", new double[]{0, 10.0});
        put("$$", new double[]{10.0, 20.0});
        put("$$$", new double[]{20.0, Double.MAX_VALUE});
        put("All", new double[]{0, Double.MAX_VALUE});
    }};

    private final Map<String, String> priceRangeSymbolMap = new HashMap<String, String>() {{
        put("$", "$");
        put("$$", "$$");
        put("$$$", "$$$");
        put("All", "");
    }};



    private void applyFilter(String selectedCategory, String selectedPriceRange) {
        ArrayList<Food> filteredList = new ArrayList<>();
        double[] priceRange = new double[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            priceRange = priceRangeMap.getOrDefault(selectedPriceRange, new double[]{0, Double.MAX_VALUE});
        }
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
    public void onDestroy() {
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
                Toast.makeText(getContext(), "Microphone permission is required for speech recognition", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Initialize the fragment map
    private void initializeFragmentMap() {
        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.navMenu, productFragment.class);
        fragmentMap.put(R.id.navCart, cartFragment.class);
        fragmentMap.put(R.id.navAccount, profileFragment.class);
        fragmentMap.put(R.id.navMap, mapFragment.class);
        fragmentMap.put(R.id.navPoints, pointsFragment.class);
        fragmentMap.put(R.id.navFavourite, FavoritesFragment.class);
        // Add more mappings as needed
    }


    // Dynamically display the selected fragment based on the menu item ID
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


}
