package sg.edu.np.mad.mad_p01_team4;

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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE);

        // Fetch user details and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserDetails(currentUser.getEmail());
        }

        // Get References
        voucherRecyclerView = view.findViewById(R.id.voucherRecyclerView);
        voucherRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list and adapter
        voucherList = new ArrayList<>();
        voucherAdapter = new VoucherAdapter(voucherList, getContext(), this::navigateToCartWithVoucher);
        voucherRecyclerView.setAdapter(voucherAdapter);

        // Fetch and display vouchers
        fetchVouchers();

        setupNavBar(view);

        // Handle redeem button clicks
        redeemButton.setOnClickListener(v -> redeemPoints(100, "$5 off your next purchase"));
        redeemButton2.setOnClickListener(v -> redeemPoints(200, "$10 off your next purchase"));
        redeemButton3.setOnClickListener(v -> redeemPoints(350, "$20 off your next purchase"));

        return view;
    }

    private void fetchVouchers() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("vouchers")
                    .whereEqualTo("userId", currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            voucherList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Voucher voucher = document.toObject(Voucher.class);
                                voucherList.add(voucher);
                            }
                            voucherAdapter.notifyDataSetChanged();
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

    private void redeemPoints(int pointsRequired, String discountDescription) {
        if (userPoints >= pointsRequired) {
            userPoints -= pointsRequired;
            updatePointsInFirestore(userPoints, discountDescription);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("discount", pointsRequired / 20);
            editor.apply();

            Toast.makeText(getActivity(), "Redeemed " + discountDescription, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Not enough points to redeem this voucher.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePointsInFirestore(long newPoints, String discountDescription) {
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
                                            addVoucher(currentUser.getUid(), discountDescription);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Points_Page", "Failed to update points and tier in Firestore", e);
                                        });
                                break;
                            }
                        } else {
                            Log.e("Points_Page", "Failed to find document with email: " + userEmail, task.getException());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Points_Page", "Failed to fetch document with email: " + userEmail, e);
                    });
        } else {
            Log.e("Points_Page", "No authenticated user found");
        }
    }

    private void addVoucher(String userId, String discountDescription) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> voucher = new HashMap<>();
        voucher.put("title", discountDescription);
        voucher.put("description", "Redeemed Voucher");
        voucher.put("userId", userId);
        voucher.put("type", "redeemed");

        db.collection("vouchers")
                .add(voucher)
                .addOnSuccessListener(documentReference -> {
                    Log.d("pointsFragment", "Voucher added successfully");
                    fetchVouchers();
                })
                .addOnFailureListener(e -> Log.w("pointsFragment", "Error adding voucher", e));
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

    private void setupNavBar(View view) {
        ImageButton homebtn = view.findViewById(R.id.home);
        ImageButton starbtn = view.findViewById(R.id.points);
        ImageButton cartbtn = view.findViewById(R.id.cart);
        ImageButton profilebtn = view.findViewById(R.id.account);

        homebtn.setOnClickListener(v -> replaceFragment(new productFragment()));
        cartbtn.setOnClickListener(v -> replaceFragment(new cartFragment()));
        profilebtn.setOnClickListener(v -> replaceFragment(new profileFragment()));
        starbtn.setOnClickListener(v -> replaceFragment(new pointsFragment()));
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void navigateToCartWithVoucher(String voucherTitle, String voucherDiscount) {
        cartFragment cartFragment = new cartFragment();

        Bundle args = new Bundle();
        args.putString("voucherTitle", voucherTitle);
        args.putString("voucherDiscount", voucherDiscount);
        cartFragment.setArguments(args);

        replaceFragment(cartFragment);
    }
}
