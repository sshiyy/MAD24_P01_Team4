package sg.edu.np.mad.mad_p01_team4;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences; // Import for SharedPreferences
import android.content.res.ColorStateList; // Import for ColorStateList
import android.os.Bundle; // Import for handling Activity lifecycle events
import android.util.Log; // Import for logging
import android.view.LayoutInflater; // Import for layout inflater
import android.view.View; // Import for handling view interactions
import android.view.ViewGroup; // Import for view groups
import android.widget.Button; // Import for Button widget
import android.widget.ImageButton; // Import for ImageButton widget
import android.widget.ProgressBar; // Import for ProgressBar widget
import android.widget.TextView; // Import for TextView widget
import android.widget.Toast; // Import for Toast messages

import androidx.annotation.NonNull; // Import for non-null annotation
import androidx.annotation.Nullable; // Import for nullable annotation
import androidx.core.view.GravityCompat; // Import for handling drawer gravity
import androidx.drawerlayout.widget.DrawerLayout; // Import for DrawerLayout
import androidx.fragment.app.Fragment; // Import for fragment handling
import androidx.fragment.app.FragmentManager; // Import for fragment manager
import androidx.fragment.app.FragmentTransaction; // Import for fragment transaction
import androidx.recyclerview.widget.LinearLayoutManager; // Import for linear layout manager in RecyclerView
import androidx.recyclerview.widget.RecyclerView; // Import for RecyclerView

import com.google.android.material.navigation.NavigationView; // Import for navigation view
import com.google.firebase.auth.FirebaseAuth; // Import for Firebase authentication
import com.google.firebase.auth.FirebaseUser; // Import for Firebase user
import com.google.firebase.firestore.FieldValue; // Import for Firestore field value
import com.google.firebase.firestore.FirebaseFirestore; // Import for Firestore database
import com.google.firebase.firestore.QueryDocumentSnapshot; // Import for Firestore query document snapshot

import java.util.ArrayList; // Import for ArrayList
import java.util.HashMap; // Import for HashMap
import java.util.List; // Import for List interface
import java.util.Map; // Import for Map interface

public class pointsFragment extends Fragment { // Main fragment class extending Fragment

    private TextView currentTier; // TextView for displaying current tier
    private TextView pointsCount; // TextView for displaying points count
    private TextView goalPointsText; // TextView for displaying goal points text
    private ProgressBar progressBar; // ProgressBar for displaying points progress
    private Button redeemButton; // Button for redeeming points
    private Button redeemButton2; // Button for redeeming points
    private Button redeemButton3; // Button for redeeming points
    private FirebaseAuth mAuth; // Firebase authentication instance
    private FirebaseFirestore db; // Firestore database instance
    private long userPoints; // Variable for storing user points
    private SharedPreferences sharedPreferences; // SharedPreferences instance

    private RecyclerView voucherRecyclerView; // RecyclerView for displaying vouchers
    private VoucherAdapter voucherAdapter; // Adapter for the RecyclerView
    private List<Voucher> voucherList; // List to store vouchers

    private ImageButton buttonDrawer; // ImageButton for opening the drawer
    private DrawerLayout drawerLayout; // DrawerLayout for navigation drawer
    private NavigationView navigationView; // NavigationView for navigation items

    private Map<Integer, Class<? extends Fragment>> fragmentMap; // Map to store fragment classes

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_points_page, container, false); // Inflate the layout for this fragment

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get References
        currentTier = view.findViewById(R.id.levelLabel); // Find TextView by ID
        pointsCount = view.findViewById(R.id.pointsValue); // Find TextView by ID
        goalPointsText = view.findViewById(R.id.pointsToNextLevel); // Find TextView by ID
        progressBar = view.findViewById(R.id.levelProgressBar); // Find ProgressBar by ID
        redeemButton = view.findViewById(R.id.button_100_points); // Find Button by ID
        redeemButton2 = view.findViewById(R.id.button_200_points); // Find Button by ID
        redeemButton3 = view.findViewById(R.id.button_350_points); // Find Button by ID

        // Initialize RecyclerView and Adapter
        voucherList = new ArrayList<>(); // Initialize voucher list
        voucherAdapter = new VoucherAdapter(voucherList, getContext(), this::navigateToCartWithVoucher); // Initialize the adapter with the voucher list
        RecyclerView voucherRecyclerView = view.findViewById(R.id.voucherRecyclerView); // Ensure this ID matches your layout
        voucherRecyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Set layout manager to RecyclerView
        voucherRecyclerView.setAdapter(voucherAdapter); // Set adapter to RecyclerView

        drawerLayout = view.findViewById(R.id.drawer_layout); // Find DrawerLayout by ID
        buttonDrawer = view.findViewById(R.id.buttonDrawerToggle); // Find ImageButton by ID
        navigationView = view.findViewById(R.id.navigationView); // Find NavigationView by ID

        buttonDrawer.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START)); // Set click listener for drawer button

        initializeFragmentMap(); // Initialize fragment map

        // Initialize NavigationView
        navigationView.setNavigationItemSelectedListener(menuItem -> { // Set navigation item selected listener
            int itemId = menuItem.getItemId(); // Get selected item ID
            displaySelectedFragment(itemId); // Display selected fragment
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer
            return true;
        });

        // Fetch user details to update points and tier
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get current authenticated user
        if (currentUser != null) {
            fetchUserDetails(currentUser.getEmail()); // Fetch user details if user is authenticated
        } else {
            Log.e(TAG, "User is not authenticated.");
        }

        // Fetch and display vouchers
        fetchVouchers();

        // Handle redeem button clicks
        redeemButton.setOnClickListener(v -> redeemPoints(100, "$2 off your next purchase", 2)); // Set click listener for redeem button
        redeemButton2.setOnClickListener(v -> redeemPoints(200, "$10 off your next purchase", 10)); // Set click listener for redeem button
        redeemButton3.setOnClickListener(v -> redeemPoints(350, "$20 off your next purchase", 20)); // Set click listener for redeem button

        return view; // Return the inflated view
    }

    private void initializeFragmentMap() {
        fragmentMap = new HashMap<>(); // Initialize fragment map
        fragmentMap.put(R.id.navMenu, productFragment.class); // Map navigation item to fragment
        fragmentMap.put(R.id.navCart, cartFragment.class); // Map navigation item to fragment
        fragmentMap.put(R.id.navAccount, profileFragment.class); // Map navigation item to fragment
        fragmentMap.put(R.id.navMap, mapFragment.class); // Map navigation item to fragment
        fragmentMap.put(R.id.navPoints, pointsFragment.class); // Map navigation item to fragment
        fragmentMap.put(R.id.navFavourite, FavoritesFragment.class); // Map navigation item to fragment
        fragmentMap.put(R.id.navOngoingOrders, ongoingFragment.class); // Map navigation item to fragment
        fragmentMap.put(R.id.navHistory, orderhistoryFragment.class); // Map navigation item to fragment
        // Add more mappings as needed
    }

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

    private void fetchVouchers() {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get current authenticated user
        if (currentUser != null) {
            db.collection("Accounts") // Reference to Accounts collection in Firestore
                    .whereEqualTo("email", currentUser.getEmail()) // Query documents where email matches
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List<Map<String, Object>> vouchers = (List<Map<String, Object>>) document.get("vouchers"); // Get vouchers from document
                                voucherList.clear(); // Clear current voucher list
                                if (vouchers != null) {
                                    for (Map<String, Object> voucherData : vouchers) {
                                        String title = (String) voucherData.get("title"); // Get voucher title
                                        String description = (String) voucherData.get("description"); // Get voucher description
                                        Long discountAmt = (Long) voucherData.get("discountAmt"); // Get voucher discount amount
                                        Voucher voucher = new Voucher(title, description, discountAmt != null ? discountAmt.intValue() : 0); // Create new voucher object
                                        voucherList.add(voucher); // Add voucher to list
                                    }
                                }
                                voucherAdapter.notifyDataSetChanged(); // Notify adapter of data change
                                break;
                            }
                        } else {
                            Log.d("pointsFragment", "Error getting documents: ", task.getException());
                        }
                    });
        }
    }

    private void fetchUserDetails(@NonNull String userEmail) {
        db.collection("Accounts") // Reference to Accounts collection in Firestore
                .whereEqualTo("email", userEmail) // Query documents where email matches
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Long points = document.getLong("points"); // Get points from document
                            userPoints = points != null ? points : 0; // Set user points
                            updateTier(userPoints); // Update user tier
                            updateProgressBar(userPoints); // Update progress bar

                            if (pointsCount != null && goalPointsText != null) {
                                pointsCount.setText(userPoints + ""); // Update points count
                                goalPointsText.setText(calculateGoalPointsText(userPoints)); // Update goal points text

                                // Show success message
                                Toast.makeText(getContext(), "Points fetched successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Show error message if any TextView is null
                                Toast.makeText(getContext(), "Failed to update points. TextView not found.", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    } else {
                        Log.d("pointsFragment", "Failed to fetch user details", task.getException());
                        // Show error message if task is not successful
                        Toast.makeText(getContext(), "Failed to fetch user details. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redeemPoints(int pointsRequired, String discountDescription, int discountAmt) {
        if (userPoints >= pointsRequired) {
            userPoints -= pointsRequired; // Deduct points from user points
            updatePointsInFirestore(userPoints, discountDescription, discountAmt); // Update points in Firestore
        } else {
            Log.d("PointsFragment", "Not enough points to redeem this voucher");
            Toast.makeText(getActivity(), "Not enough points to redeem this voucher.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePointsInFirestore(long newPoints, String discountDescription, int discountAmt) {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get current authenticated user
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("Accounts") // Reference to Accounts collection in Firestore
                    .whereEqualTo("email", userEmail) // Query documents where email matches
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String tier = calculateTier(newPoints); // Calculate new tier
                                db.collection("Accounts").document(document.getId())
                                        .update("points", newPoints, "tier", tier) // Update points and tier in document
                                        .addOnSuccessListener(aVoid -> {
                                            pointsCount.setText(newPoints + " pts"); // Update points count
                                            goalPointsText.setText(calculateGoalPointsText(newPoints)); // Update goal points text
                                            currentTier.setText(tier); // Update current tier
                                            updateProgressBar(newPoints); // Update progress bar
                                            Log.d("Points_Page", "Points and tier updated successfully in Firestore");
                                            addVoucher(currentUser.getUid(), discountDescription, discountAmt); // Add voucher to user account
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Points_Page", "Failed to update points and tier in Firestore", e);
                                            Toast.makeText(getActivity(), "Failed to update points and tier in Firestore", Toast.LENGTH_SHORT).show();
                                        });
                                break;
                            }
                        } else {
                            Log.e("Points_Page", "Failed to find document with email: " + userEmail, task.getException());
                            Toast.makeText(getActivity(), "Failed to find user account", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Points_Page", "Failed to fetch document with email: " + userEmail, e);
                        Toast.makeText(getActivity(), "Failed to fetch user account", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Points_Page", "No authenticated user found");
            Toast.makeText(getActivity(), "No authenticated user found", Toast.LENGTH_SHORT).show();
        }
    }

    private void addVoucher(String userId, String discountDescription, int discountAmt) {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get current authenticated user
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("Accounts") // Reference to Accounts collection in Firestore
                    .whereEqualTo("email", userEmail) // Query documents where email matches
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Create a voucher map with title, description, and discount amount
                                Map<String, Object> voucher = new HashMap<>();
                                voucher.put("title", discountDescription);
                                voucher.put("description", discountDescription);
                                voucher.put("discountAmt", discountAmt);

                                // Add voucher to the user's account
                                db.collection("Accounts").document(document.getId())
                                        .update("vouchers", FieldValue.arrayUnion(voucher)) // Add voucher to array
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("pointsFragment", "Voucher added successfully");
                                            Voucher newVoucher = new Voucher(discountDescription, discountDescription, discountAmt); // Create new voucher object
                                            voucherList.add(newVoucher); // Add voucher to list
                                            voucherAdapter.notifyDataSetChanged(); // Notify adapter of data change
                                            Toast.makeText(getActivity(), "Redeemed " + discountDescription + " successfully!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("pointsFragment", "Error adding voucher", e);
                                            Toast.makeText(getActivity(), "Error adding voucher: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                break;
                            }
                        } else {
                            Log.e("Points_Page", "Failed to find document with email: " + userEmail, task.getException());
                            Toast.makeText(getActivity(), "Failed to find user account", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Points_Page", "Failed to fetch document with email: " + userEmail, e);
                        Toast.makeText(getActivity(), "Failed to fetch user account", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Points_Page", "No authenticated user found");
            Toast.makeText(getActivity(), "No authenticated user found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgressBar(long points) {
        int progress = (int) points; // Convert points to int for progress bar
        progressBar.setProgress(progress); // Set progress bar progress

        if (points >= 450) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.platinium))); // Set progress bar color to platinum
        } else if (points >= 300) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold))); // Set progress bar color to gold
        } else if (points >= 100) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.silver))); // Set progress bar color to silver
        } else {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.bronze))); // Set progress bar color to bronze
        }
    }

    private String calculateTier(long points) {
        if (points >= 450) {
            return "Platinum"; // Return platinum tier
        } else if (points >= 300) {
            return "Gold"; // Return gold tier
        } else if (points >= 100) {
            return "Silver"; // Return silver tier
        } else {
            return "Bronze"; // Return bronze tier
        }
    }

    private void updateTier(long points) {
        String tier = calculateTier(points); // Calculate tier based on points
        currentTier.setText(tier); // Update current tier
        goalPointsText.setText(calculateGoalPointsText(points)); // Update goal points text
    }

    private String calculateGoalPointsText(long points) {
        if (points < 100) {
            return "Points needed for Silver: " + (100 - points); // Return points needed for silver
        } else if (points < 300) {
            return "Points needed for Gold: " + (300 - points); // Return points needed for gold
        } else if (points < 450) {
            return "Points needed for Platinum: " + (450 - points); // Return points needed for platinum
        } else {
            return "You have reached the highest tier!"; // Return highest tier message
        }
    }

    private void navigateToCartWithVoucher(Voucher voucher) {
        cartFragment cartFragment = new cartFragment(); // Create new cart fragment

        Bundle args = new Bundle();
        args.putString("voucherTitle", voucher.getTitle()); // Put voucher title in bundle
        args.putInt("voucherDiscount", voucher.getDiscountAmt()); // Put voucher discount in bundle
        cartFragment.setArguments(args); // Set arguments to cart fragment

        // Navigate to the cartFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, cartFragment) // Replace with the ID of your container layout
                .addToBackStack(null)
                .commit();

        // Remove the voucher from the database and update the UI
        removeVoucherFromDatabase(voucher);
    }

    private void removeVoucherFromDatabase(Voucher voucher) {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get current authenticated user
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("Accounts") // Reference to Accounts collection in Firestore
                    .whereEqualTo("email", userEmail) // Query documents where email matches
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("Accounts").document(document.getId())
                                        .update("vouchers", FieldValue.arrayRemove(voucher.toMap())) // Remove voucher from array
                                        .addOnSuccessListener(aVoid -> {
                                            voucherList.remove(voucher); // Remove voucher from list
                                            voucherAdapter.notifyDataSetChanged(); // Notify adapter of data change
                                            Toast.makeText(getActivity(), "Voucher removed successfully!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("pointsFragment", "Failed to remove voucher", e);
                                            Toast.makeText(getActivity(), "Failed to remove voucher: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                break;
                            }
                        } else {
                            Log.e("pointsFragment", "Failed to find document with email: " + userEmail, task.getException());
                            Toast.makeText(getActivity(), "Failed to find user account", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("pointsFragment", "Failed to fetch document with email: " + userEmail, e);
                        Toast.makeText(getActivity(), "Failed to fetch user account", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("pointsFragment", "No authenticated user found");
            Toast.makeText(getActivity(), "No authenticated user found", Toast.LENGTH_SHORT).show();
        }
    }
}
