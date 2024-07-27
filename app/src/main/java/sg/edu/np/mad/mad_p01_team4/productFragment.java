package sg.edu.np.mad.mad_p01_team4; // Package declaration for the project

import android.Manifest; // Import for microphone permission
import android.content.Context; // Import for context handling
import android.content.Intent; // Import for intent handling
import android.content.pm.PackageManager; // Import for package manager
import android.graphics.Color; // Import for handling colors
import android.graphics.drawable.ColorDrawable; // Import for color drawable
import android.os.Bundle; // Import for handling Activity lifecycle events
import android.speech.SpeechRecognizer; // Import for speech recognition
import android.text.Editable; // Import for editable text interface
import android.text.TextWatcher; // Import for text change listener
import android.util.Log; // Import for logging
import android.view.Gravity; // Import for setting gravity
import android.view.KeyEvent; // Import for key events
import android.view.LayoutInflater; // Import for layout inflater
import android.view.View; // Import for handling view interactions
import android.view.ViewGroup; // Import for view groups
import android.view.inputmethod.EditorInfo; // Import for handling input methods
import android.widget.Button; // Import for Button widget
import android.widget.EditText; // Import for EditText widget
import android.widget.ImageButton; // Import for ImageButton widget
import android.widget.LinearLayout; // Import for LinearLayout widget
import android.widget.PopupWindow; // Import for PopupWindow
import android.widget.RelativeLayout; // Import for RelativeLayout widget
import android.widget.Spinner; // Import for Spinner widget
import android.widget.TextView; // Import for TextView widget
import android.widget.Toast; // Import for Toast messages

import androidx.annotation.NonNull; // Import for non-null annotation
import androidx.core.app.ActivityCompat; // Import for activity compatibility
import androidx.core.content.ContextCompat; // Import for context compatibility
import androidx.core.view.GravityCompat; // Import for handling drawer gravity
import androidx.drawerlayout.widget.DrawerLayout; // Import for DrawerLayout
import androidx.fragment.app.Fragment; // Import for fragment handling
import androidx.recyclerview.widget.DefaultItemAnimator; // Import for default item animator
import androidx.recyclerview.widget.GridLayoutManager; // Import for grid layout manager in RecyclerView
import androidx.recyclerview.widget.RecyclerView; // Import for RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import for floating action button
import com.google.android.material.navigation.NavigationView; // Import for navigation view
import com.google.firebase.auth.FirebaseAuth; // Import for Firebase authentication
import com.google.firebase.auth.FirebaseUser; // Import for Firebase user
import com.google.firebase.firestore.FirebaseFirestore; // Import for Firestore database
import com.google.firebase.firestore.QueryDocumentSnapshot; // Import for Firestore query document snapshot

import java.util.ArrayList; // Import for ArrayList
import java.util.HashMap; // Import for HashMap
import java.util.List; // Import for List interface
import java.util.Locale; // Import for Locale settings
import java.util.Map; // Import for Map interface

public class productFragment extends Fragment { // Main fragment class extending Fragment

    public productFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "productpage"; // Tag for logging
    private static final int REQUEST_MIC_PERMISSION = 1;  // Constant for microphone permission request

    private FirebaseFirestore db; // Firestore database instance
    private ArrayList<Food> allFoodList; // List to store food items
    private FoodAdapter foodAdapter; // Adapter for the RecyclerView
    private TextView allRestaurantsText; // TextView for displaying all restaurants text
    private TextView sortedByText; // TextView for displaying sorted by text
    private EditText searchEditText; // EditText for search input

    private DrawerLayout drawerLayout; // DrawerLayout for navigation drawer
    private ImageButton buttonDrawer; // ImageButton for opening the drawer
    private NavigationView navigationView; // NavigationView for navigation items
    private LinearLayout voicePopup; // LinearLayout for voice popup
    private SpeechRecognitionHelper speechRecognitionHelper; // Helper class for speech recognition

    private TextView noProductTextView; // TextView for displaying no products message

    private Map<Integer, Class<? extends Fragment>> fragmentMap; // Map to store fragment classes

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.productpage, container, false); // Inflate the layout for this fragment

        drawerLayout = view.findViewById(R.id.drawer_layout); // Find DrawerLayout by ID
        buttonDrawer = view.findViewById(R.id.buttonDrawerToggle); // Find ImageButton by ID
        navigationView = view.findViewById(R.id.navigationView); // Find NavigationView by ID
        searchEditText = view.findViewById(R.id.searchEditText); // Find EditText by ID
        voicePopup = view.findViewById(R.id.voice_popup); // Find LinearLayout by ID
        noProductTextView = view.findViewById(R.id.noProductTextView); // Find TextView by ID
        ImageButton voiceButton = view.findViewById(R.id.search_voice_btn); // Find ImageButton by ID

        voiceButton.setOnClickListener(v -> startVoiceRecognition()); // Set click listener for voice button
        checkMicrophonePermission(); // Check microphone permission

        buttonDrawer.setOnClickListener(v -> drawerLayout.open()); // Set click listener for drawer button

        initializeFragmentMap(); // Initialize fragment map

        navigationView.setNavigationItemSelectedListener(menuItem -> { // Set navigation item selected listener
            int itemId = menuItem.getItemId(); // Get selected item ID
            Log.d(TAG, "Navigation item clicked: " + itemId);
            displaySelectedFragment(itemId); // Display selected fragment
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer
            return true;
        });

        db = FirebaseFirestore.getInstance(); // Get Firestore instance
        allFoodList = new ArrayList<>(); // Initialize the food list
        foodAdapter = new FoodAdapter(new ArrayList<>(), getContext(), R.layout.custom_itemlist_small); // Initialize the adapter with the food list

        setUpRecyclerView(view, R.id.productrecyclerView, foodAdapter); // Set up RecyclerView

        allRestaurantsText = view.findViewById(R.id.allRestaurantsText); // Find TextView by ID
        sortedByText = view.findViewById(R.id.sortedByText); // Find TextView by ID

        fetchFoodItems(); // Fetch food items from Firestore

        ImageButton filbtn = view.findViewById(R.id.filterIcon); // Find filter button by ID
        filbtn.setOnClickListener(v -> showFilterPopup()); // Set click listener for filter button

        RelativeLayout cartbutton = view.findViewById(R.id.cart_button); // Find cart button by ID
        cartbutton.setOnClickListener(v -> { // Set click listener for cart button
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new cartFragment()) // Replace current fragment with cart fragment
                    .addToBackStack(null)
                    .commit();
        });

        FloatingActionButton floatingActionButton = view.findViewById(R.id.chatbot_button); // Find floating action button by ID
        floatingActionButton.setOnClickListener(v -> { // Set click listener for floating action button
            Intent intent = new Intent(getActivity(), chatbot.class); // Create intent for chatbot activity
            startActivity(intent); // Start chatbot activity
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> { // Set editor action listener for search EditText
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch(v.getText().toString()); // Perform search when search action is triggered
                return true;
            }
            return false;
        });

        searchEditText.addTextChangedListener(new TextWatcher() { // Add text watcher to search EditText
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString()); // Perform search as text changes
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view; // Return the inflated view
    }

    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MIC_PERMISSION); // Request microphone permission if not granted
        } else {
            initializeSpeechRecognitionHelper(); // Initialize speech recognition helper if permission is granted
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissMicPopup(); // Dismiss microphone popup on pause
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissMicPopup(); // Dismiss microphone popup on view destroy
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognitionHelper != null) {
            speechRecognitionHelper.destroy(); // Destroy speech recognition helper on destroy
        }
    }

    private void initializeSpeechRecognitionHelper() {
        Log.d(TAG, "Initializing SpeechRecognitionHelper");
        speechRecognitionHelper = new SpeechRecognitionHelper(getContext(), new SpeechRecognitionHelper.SpeechRecognitionListener() {
            @Override
            public void onReadyForSpeech() {
                showMicPopup(); // Show microphone popup when ready for speech
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
                dismissMicPopup(); // Dismiss microphone popup on error
                Log.e(TAG, "Speech recognition error: " + error);
                String errorMessage = getErrorText(error); // Get error message based on error code
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show(); // Show error message
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    startVoiceRecognition(); // Restart voice recognition if no match error occurs
                }
            }

            @Override
            public void onResults(String result) {
                Log.d(TAG, "Recognized result: " + result);
                updateRecognizedText(result); // Update recognized text
                handleVoiceCommand(result); // Handle voice command
                dismissMicPopup(); // Dismiss microphone popup
            }

            @Override
            public void onPartialResults(String partialResult) {
                Log.d(TAG, "Partial result: " + partialResult);
                updateRecognizedText(partialResult); // Update recognized text with partial result
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void startVoiceRecognition() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MIC_PERMISSION); // Request microphone permission if not granted
        } else {
            if (speechRecognitionHelper != null) {
                Log.d(TAG, "Starting voice recognition");
                speechRecognitionHelper.startListening(); // Start listening for voice input
                showMicPopup(); // Show microphone popup
            } else {
                Log.e(TAG, "SpeechRecognitionHelper is not initialized");
            }
        }
    }

    private void showMicPopup() {
        voicePopup.setVisibility(View.VISIBLE); // Show microphone popup
    }

    private void dismissMicPopup() {
        if (voicePopup != null) {
            voicePopup.setVisibility(View.GONE); // Hide microphone popup
        }
    }

    private void updateRecognizedText(String text) {
        Log.d(TAG, "Attempting to update recognized text: " + text);
        if (voicePopup != null && voicePopup.getVisibility() == View.VISIBLE) {
            TextView recognizedTextView = voicePopup.findViewById(R.id.recognized_text); // Find recognized text view by ID
            if (recognizedTextView != null) {
                Log.d(TAG, "Updating recognized text: " + text);
                recognizedTextView.setText(text); // Set recognized text
            } else {
                Log.e(TAG, "recognizedTextView is null");
            }
        } else {
            Log.e(TAG, "voicePopup is not visible or is null");
        }
    }

    private void handleVoiceCommand(String command) {
        Log.d(TAG, "Handling voice command: " + command);
        command = command.toLowerCase(Locale.ROOT); // Convert command to lower case

        Fragment selectedFragment = null; // Initialize selected fragment

        if (command.contains("account") || command.contains("profile") || command.contains("account")) {
            Log.d(TAG, "Navigating to account fragment");
            selectedFragment = new profileFragment(); // Navigate to profile fragment
        } else if (command.contains("shopping") || command.contains("cart") || command.contains("checkout")) {
            Log.d(TAG, "Navigating to cart fragment");
            selectedFragment = new cartFragment(); // Navigate to cart fragment
        } else if (command.contains("product") || command.contains("Home") || command.contains("Food")) {
            Log.d(TAG, "Navigating to product fragment");
            selectedFragment = new productFragment(); // Navigate to product fragment
        } else if (command.contains("points") || command.contains("rewards")) {
            Log.d(TAG, "Navigating to points fragment");
            selectedFragment = new pointsFragment(); // Navigate to points fragment
        } else {
            Log.d(TAG, "Unrecognized command: " + command);
        }

        if (selectedFragment != null) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment) // Replace current fragment with selected fragment
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void setUpRecyclerView(View view, int recyclerViewId, RecyclerView.Adapter<?> adapter) {
        RecyclerView recyclerView = view.findViewById(recyclerViewId); // Find RecyclerView by ID
        int numberOfColumns = 2; // Set the number of columns for the grid layout
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), numberOfColumns); // Create a grid layout manager
        recyclerView.setLayoutManager(layoutManager); // Set layout manager to RecyclerView
        recyclerView.setItemAnimator(new DefaultItemAnimator()); // Set default item animator
        recyclerView.setAdapter(adapter); // Set adapter to RecyclerView
    }

    private void fetchFoodItems() {
        db.collection("Food_Items") // Reference to Food_Items collection in Firestore
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allFoodList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Food food = document.toObject(Food.class); // Convert document to Food object
                            food.setModifications((List<Map<String, Object>>) document.get("modifications")); // Set modifications
                            allFoodList.add(food); // Add food item to list
                        }
                        updateAllAdapters(allFoodList); // Update adapters with the fetched food items
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void updateAllAdapters(ArrayList<Food> foodList) {
        foodAdapter.updateList(foodList); // Update adapter with the new food list
    }

    private void showFilterPopup() {
        try {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); // Get layout inflater
            View popupView = inflater.inflate(R.layout.activity_flitering_page, null); // Inflate filter popup layout
            int width = LinearLayout.LayoutParams.MATCH_PARENT; // Set popup width to match parent
            int height = LinearLayout.LayoutParams.MATCH_PARENT; // Set popup height to match parent
            boolean focusable = true; // Set popup focusable
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable); // Create popup window
            View mainLayout = getActivity().findViewById(android.R.id.content).getRootView(); // Get main layout
            popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0); // Show popup window at center
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Set transparent background

            Button applyButton = popupView.findViewById(R.id.applyButton); // Find apply button by ID
            applyButton.setOnClickListener(v -> {
                Spinner categorySpinner = popupView.findViewById(R.id.spinnerCategory); // Find category spinner by ID
                Spinner priceSpinner = popupView.findViewById(R.id.spinnerPrice); // Find price spinner by ID
                String selectedCategory = categorySpinner.getSelectedItem().toString(); // Get selected category
                String selectedPriceRange = priceSpinner.getSelectedItem().toString(); // Get selected price range

                applyFilter(selectedCategory, selectedPriceRange); // Apply filter with selected category and price range
                popupWindow.dismiss(); // Dismiss popup window
            });

            Button closeButton = popupView.findViewById(R.id.cancelButton); // Find close button by ID
            closeButton.setOnClickListener(v -> popupWindow.dismiss()); // Set click listener to dismiss popup window
        } catch (Exception e) {
            Log.e(TAG, "Error showing filter popup", e);
        }
    }

    private final Map<String, double[]> priceRangeMap = new HashMap<String, double[]>() {{
        put("$0 - $10", new double[]{0, 10.0});
        put("$10 - $20", new double[]{10.0, 20.0});
        put("$20 and above", new double[]{20.0, Double.MAX_VALUE});
        put("All", new double[]{0, Double.MAX_VALUE});
    }}; // Map to store price ranges

    private final Map<String, String> priceRangeSymbolMap = new HashMap<String, String>() {{
        put("$0 - $10", "$0 - $10");
        put("$10 - $20", "$10 - $20");
        put("$20 and above", "$20 and above");
        put("All", "");
    }}; // Map to store price range symbols

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

        updateAllAdapters(filteredList); // Update adapters with the filtered food items

        if (filteredList.isEmpty()) {
            noProductTextView.setVisibility(View.VISIBLE); // Show no products message if the list is empty
        } else {
            noProductTextView.setVisibility(View.GONE); // Hide no products message if the list is not empty
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
        updateAllAdapters(allFoodList); // Clear filter and update adapter with all food items
    }

    private void performSearch(String query) {
        ArrayList<Food> filteredList = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase().trim();

        for (Food food : allFoodList) {
            if (food.getName().toLowerCase().contains(lowercaseQuery)) {
                filteredList.add(food); // Add food item to filtered list if it matches the search query
            }
        }

        foodAdapter.updateList(filteredList); // Update adapter with the filtered food items
    }

    private void clearSearch() {
        searchEditText.setText("");
        updateAllAdapters(allFoodList); // Clear search and update adapter with all food items
        allRestaurantsText.setText("All Categories");
        sortedByText.setText("sorted by category");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MIC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Microphone permission granted");
                initializeSpeechRecognitionHelper(); // Initialize speech recognition helper if permission is granted
            } else {
                Toast.makeText(getContext(), "Microphone permission is required for speech recognition", Toast.LENGTH_SHORT).show(); // Show permission required message
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
    } // Initialize map to store fragment classes

    private void displaySelectedFragment(int itemId) {
        Class<? extends Fragment> fragmentClass = fragmentMap.get(itemId); // Get fragment class based on item ID
        if (fragmentClass != null) {
            try {
                Fragment selectedFragment = fragmentClass.newInstance(); // Create a new instance of the fragment
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment) // Replace current fragment with selected fragment
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
        return message; // Return error message based on error code
    }
}
