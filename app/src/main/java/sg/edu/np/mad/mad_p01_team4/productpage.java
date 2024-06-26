package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class productpage extends AppCompatActivity {

    private static final String TAG = "productpage";

    private FirebaseFirestore db;
    private ArrayList<Food> allFoodList;
    private FoodAdapter foodAdapter;
    private TextView allRestaurantsText;
    private TextView sortedByText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.productpage);

        db = FirebaseFirestore.getInstance();
        allFoodList = new ArrayList<>();

        foodAdapter = new FoodAdapter(new ArrayList<>(), this);
        setUpRecyclerView(R.id.productrecyclerView, foodAdapter);


        allRestaurantsText = findViewById(R.id.allRestaurantsText);
        sortedByText = findViewById(R.id.sortedByText);

        fetchFoodItems();

        // Setup filter buttons to enter filter page
        ImageButton filbtn = findViewById(R.id.filterIcon);
        filbtn.setOnClickListener(v -> showFilterPopup());

        CardView filterbutton = findViewById(R.id.filtercard);
        filterbutton.setOnClickListener(v -> showFilterPopup());

        // Setup cart button to navigate to cart page
        ImageButton cartbutton = findViewById(R.id.cart_button);
        cartbutton.setOnClickListener(v -> {
            Intent intent = new Intent(productpage.this, cartpage.class);
            startActivity(intent);
        });

        // Setup points button to navigate to points page
        ImageButton starbutton = findViewById(R.id.points);
        starbutton.setOnClickListener(v -> {
            Intent intent = new Intent(productpage.this, Points_Page.class);
            startActivity(intent);
        });

        // Setup profile button to enter profile/account page
        ImageButton profilebtn = findViewById(R.id.account);
        profilebtn.setOnClickListener(v -> {
            Intent intent = new Intent(productpage.this, ProfilePage.class);
            startActivity(intent);
        });


        ImageView homeButton = findViewById(R.id.home);
        homeButton.setOnClickListener(v -> startActivity(new Intent(productpage.this, productpage.class)));


        // Setup clear filter button
        ImageButton crossicon = findViewById(R.id.crossicon);
        crossicon.setOnClickListener(v -> clearFilter());
    }

    // To set recyclerview to grid format
    private void setUpRecyclerView(int recyclerViewId, FoodAdapter adapter) {
        RecyclerView recyclerView = findViewById(recyclerViewId);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }




    // For retrieving food items from database

    private void fetchFoodItems() {
        db.collection("Food_Items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allFoodList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) { // usage of documentsnapshot to fetch the products
                            Food food = document.toObject(Food.class);
                            allFoodList.add(food);
                        }
                        updateAllAdapters(allFoodList);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException()); //checking it in the Logcat
                    }
                });
    }

    private void updateAllAdapters(ArrayList<Food> foodList) {
        foodAdapter.updateList(foodList);
    } // update the adapter for the products to be displayed

//function method for filter pop up
    private void showFilterPopup() {
        try {
            // Inflate the popup_filter.xml layout
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.activity_flitering_page, null);

            // Create a PopupWindow object
            int width = LinearLayout.LayoutParams.MATCH_PARENT; //the sizing of the popup
            int height = LinearLayout.LayoutParams.MATCH_PARENT;
            boolean focusable = true; // Allows taps outside the PopupWindow to dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable); // final is to create a constant variable

            // Show the popup window
            View mainLayout = findViewById(android.R.id.content).getRootView(); // Get the root view of the current activity
            popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //for the bg of the filter.xml to be transparent

            // Setup apply button click event
            Button applyButton = popupView.findViewById(R.id.applyButton);
            applyButton.setOnClickListener(v -> {
                // Get selected category and price range
                Spinner categorySpinner = popupView.findViewById(R.id.spinnerCategory);
                Spinner priceSpinner = popupView.findViewById(R.id.spinnerPrice);
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                String selectedPriceRange = priceSpinner.getSelectedItem().toString();

                // Apply filter
                applyFilter(selectedCategory, selectedPriceRange);

                // Dismiss the popup window
                popupWindow.dismiss();
            });

            // Setup close button click event
            Button closeButton = popupView.findViewById(R.id.cancelButton);
            closeButton.setOnClickListener(v -> popupWindow.dismiss());
        } catch (Exception e) {
            Log.e(TAG, "Error showing filter popup", e);
        }
    }


    //method for filter criteria
    private void applyFilter(String selectedCategory, String selectedPriceRange) {
        ArrayList<Food> filteredList = new ArrayList<>();

        // Get min and max prices from the selected price range
        double[] priceRange = getPriceRange(selectedPriceRange);
        double minPrice = priceRange[0];
        double maxPrice = priceRange[1];

        // Loop through all products and filter based on selected category and price range
        for (Food food : allFoodList) {
            boolean matchesCategory = selectedCategory.equals("All") || food.getCategory().equals(selectedCategory);
            boolean matchesPrice = food.getPrice() >= minPrice && food.getPrice() <= maxPrice;

            if ((selectedCategory.equals("All") && matchesPrice) || (matchesCategory && matchesPrice)) {
                filteredList.add(food);
            }
        }

        // Update RecyclerView adapters with filtered list
        updateAllAdapters(filteredList);

        // Update the filter title
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
        // Convert price range string to min and max price values
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
        // Convert price range string to symbol
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



    // a method for clearing the filter - back to default
    private void clearFilter() {
        // Reset the filter title
        allRestaurantsText.setText("All Category");
        sortedByText.setText("sorted by category");

        // Update RecyclerView adapters with the full list
        updateAllAdapters(allFoodList);
    }






    }

