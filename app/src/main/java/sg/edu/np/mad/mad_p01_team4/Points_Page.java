package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Points_Page extends AppCompatActivity {

    private TextView currentTier;
    private TextView pointsCount;
    private TextView currentPointsText;
    private TextView goalPointsText;
    private Button redeemButton;
    private Button redeemButton2;
    private Button redeemButton3;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private long userPoints;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_page);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get References
        currentTier = findViewById(R.id.currentTier);
        pointsCount = findViewById(R.id.pointsCount);
        currentPointsText = findViewById(R.id.currentPointsText);
        goalPointsText = findViewById(R.id.goalPointsText);
        redeemButton = findViewById(R.id.redeemButton);
        redeemButton2 = findViewById(R.id.redeemButton2);
        redeemButton3 = findViewById(R.id.redeemButton3);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Fetch user details and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserDetails(currentUser.getEmail());
        }

        // Handle redeem button clicks
        redeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redeemPoints(100, 5); // 100 points for $5 off
            }
        });

        redeemButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redeemPoints(200, 10); // 200 points for $10 off
            }
        });

        redeemButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redeemPoints(350, 20); // 350 points for $20 off
            }
        });

        ImageButton homeBtn = findViewById(R.id.home);
        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Points_Page.this, productpage.class);
            startActivity(intent);
        });


        ImageButton cartbutton = findViewById(R.id.cart_button);
        cartbutton.setOnClickListener(v -> {
            Intent intent = new Intent(Points_Page.this, cartpage.class);
            startActivity(intent);
        });

        ImageButton starbutton = findViewById(R.id.points);
        starbutton.setOnClickListener(v -> {
            Intent intent = new Intent(Points_Page.this, Points_Page.class);
            startActivity(intent);
        });


        // Setup profile button
        ImageButton profilebtn = findViewById(R.id.account);
        profilebtn.setOnClickListener(v -> {
            Intent intent = new Intent(Points_Page.this, ProfilePage.class);
            startActivity(intent);
        });
    }

    private void fetchUserDetails(String userEmail) {
        db.collection("Accounts")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Long points = document.getLong("points");
                            userPoints = points != null ? points : 0;
                            updateTier(userPoints);

                            pointsCount.setText(userPoints + " pts");
                            currentPointsText.setText("Current Points: " + userPoints);
                            goalPointsText.setText(calculateGoalPointsText(userPoints)); // Calculate points needed for the next tier

                            break;
                        }
                    } else {
                        Log.d("Points_Page", "Failed to fetch user details", task.getException());
                    }
                });
    }

    private void redeemPoints(int pointsRequired, int discountAmount) {
        if (userPoints >= pointsRequired) {
            userPoints -= pointsRequired;
            updatePointsInFirestore(userPoints);

            // Save the discount amount to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("discount", discountAmount);
            editor.apply();

            Toast.makeText(this, "Redeemed $" + discountAmount + " off!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Not enough points to redeem this voucher.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePointsInFirestore(long newPoints) {
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
                                            currentPointsText.setText("Current Points: " + newPoints);
                                            goalPointsText.setText(calculateGoalPointsText(newPoints)); // Calculate points needed for the next tier
                                            currentTier.setText(tier);
                                            Log.d("Points_Page", "Points and tier updated successfully in Firestore");
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


    private String calculateTier(long points) {
        if (points >= 300) {
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
        } else {
            return "You have reached the highest tier!";
        }
    }
}
