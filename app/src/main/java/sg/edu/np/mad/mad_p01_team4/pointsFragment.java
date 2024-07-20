package sg.edu.np.mad.mad_p01_team4;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class pointsFragment extends Fragment {

    private TextView currentTier;
    private TextView pointsCount;
    private TextView goalPointsText;
    private ProgressBar progressBar;
    private Button redeemButton;
    private Button redeemButton2;
    private Button redeemButton3;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private long userPoints;
    private SharedPreferences sharedPreferences;

    private RecyclerView voucherRecyclerView;
    private VoucherAdapter voucherAdapter;
    private List<Voucher> voucherList;

    private ImageButton buttonDrawer;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private Map<Integer, Class<? extends Fragment>> fragmentMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_points_page, container, false);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get References
        currentTier = view.findViewById(R.id.levelLabel);
        pointsCount = view.findViewById(R.id.pointsValue);
        goalPointsText = view.findViewById(R.id.pointsToNextLevel);
        progressBar = view.findViewById(R.id.levelProgressBar);
        redeemButton = view.findViewById(R.id.button_100_points);
        redeemButton2 = view.findViewById(R.id.button_200_points);
        redeemButton3 = view.findViewById(R.id.button_350_points);

        // Initialize RecyclerView and Adapter
        voucherRecyclerView = view.findViewById(R.id.voucherRecyclerView);
        voucherRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        voucherList = new ArrayList<>();
        voucherAdapter = new VoucherAdapter(voucherList, getContext(), this::navigateToCartWithVoucher);
        voucherRecyclerView.setAdapter(voucherAdapter);

        drawerLayout = view.findViewById(R.id.drawer_layout);
        buttonDrawer = view.findViewById(R.id.buttonDrawerToggle);
        navigationView = view.findViewById(R.id.navigationView);

        buttonDrawer.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        initializeFragmentMap();

        // Initialize NavigationView
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            displaySelectedFragment(itemId);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Fetch user details to update points and tier
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserDetails(currentUser.getEmail());
        } else {
            Log.e(TAG, "User is not authenticated.");
        }

        // Fetch and display vouchers
        fetchVouchers();

        // Handle redeem button clicks
        redeemButton.setOnClickListener(v -> redeemPoints(100, "$5 off your next purchase", 5));
        redeemButton2.setOnClickListener(v -> redeemPoints(200, "$10 off your next purchase", 10));
        redeemButton3.setOnClickListener(v -> redeemPoints(350, "$20 off your next purchase", 20));

        return view;
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
        // Add more mappings as needed
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

    private void fetchVouchers() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("Accounts")
                    .whereEqualTo("email", currentUser.getEmail())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List<Map<String, Object>> vouchers = (List<Map<String, Object>>) document.get("vouchers");
                                voucherList.clear();
                                if (vouchers != null) {
                                    for (Map<String, Object> voucherData : vouchers) {
                                        String title = (String) voucherData.get("title");
                                        String description = (String) voucherData.get("description");
                                        Long discountAmt = (Long) voucherData.get("discountAmt");
                                        Voucher voucher = new Voucher(title, description, discountAmt != null ? discountAmt.intValue() : 0);
                                        voucherList.add(voucher);
                                    }
                                }
                                voucherAdapter.notifyDataSetChanged();
                                break;
                            }
                        } else {
                            Log.d("pointsFragment", "Error getting documents: ", task.getException());
                        }
                    });
        }
    }

    private void fetchUserDetails(@NonNull String userEmail) {
        db.collection("Accounts")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Long points = document.getLong("points");
                            userPoints = points != null ? points : 0;
                            updateTier(userPoints);
                            updateProgressBar(userPoints);

                            if (pointsCount != null && goalPointsText != null) {
                                pointsCount.setText(userPoints + "");
                                goalPointsText.setText(calculateGoalPointsText(userPoints));

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
            userPoints -= pointsRequired;
            updatePointsInFirestore(userPoints, discountDescription, discountAmt);
        } else {
            Log.d("PointsFragment", "Not enough points to redeem this voucher");
            Toast.makeText(getActivity(), "Not enough points to redeem this voucher.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePointsInFirestore(long newPoints, String discountDescription, int discountAmt) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("Accounts")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String tier = calculateTier(newPoints);
                                db.collection("Accounts").document(document.getId())
                                        .update("points", newPoints, "tier", tier)
                                        .addOnSuccessListener(aVoid -> {
                                            pointsCount.setText(newPoints + " pts");
                                            goalPointsText.setText(calculateGoalPointsText(newPoints));
                                            currentTier.setText(tier);
                                            updateProgressBar(newPoints);
                                            Log.d("Points_Page", "Points and tier updated successfully in Firestore");
                                            addVoucher(currentUser.getUid(), discountDescription, discountAmt);
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("Accounts")
                    .whereEqualTo("email", userEmail)
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
                                        .update("vouchers", FieldValue.arrayUnion(voucher))
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("pointsFragment", "Voucher added successfully");
                                            Voucher newVoucher = new Voucher(discountDescription, discountDescription, discountAmt);
                                            voucherList.add(newVoucher);
                                            voucherAdapter.notifyDataSetChanged();
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
        int progress = (int) points;
        progressBar.setProgress(progress);

        if (points >= 450) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.platinium)));
        } else if (points >= 300) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold)));
        } else if (points >= 100) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.silver)));
        } else {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.bronze)));
        }
    }

    private String calculateTier(long points) {
        if (points >= 450) {
            return "Platinum";
        } else if (points >= 300) {
            return "Gold";
        } else if (points >= 100) {
            return "Silver";
        } else {
            return "Bronze";
        }
    }

    private void updateTier(long points) {
        String tier = calculateTier(points);
        currentTier.setText(tier);
        goalPointsText.setText(calculateGoalPointsText(points));
    }

    private String calculateGoalPointsText(long points) {
        if (points < 100) {
            return "Points needed for Silver: " + (100 - points);
        } else if (points < 300) {
            return "Points needed for Gold: " + (300 - points);
        } else if (points < 450) {
            return "Points needed for Platinum: " + (450 - points);
        } else {
            return "You have reached the highest tier!";
        }
    }

    private void navigateToCartWithVoucher(String voucherTitle, int voucherDiscount) {
        cartFragment cartFragment = new cartFragment();

        Bundle args = new Bundle();
        args.putString("voucherTitle", voucherTitle);
        args.putInt("voucherDiscount", voucherDiscount);
        cartFragment.setArguments(args);

        // Navigate to the cartFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, cartFragment) // Replace with the ID of your container layout
                .addToBackStack(null)
                .commit();
    }
}
