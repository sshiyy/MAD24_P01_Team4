package sg.edu.np.mad.mad_p01_team4;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class cartpage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView itemsTotalamt, GSTamt, totalamt, discountAmt;
    private Button btnConfirm;

    private int points = 0; // Points start at 0 for each session
    private double totalPrice = 0.0; // Total price of the cart items
    private double discountAmount = 0.0; // Discount amount to be applied
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cartpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        discountAmount = sharedPreferences.getInt("discount", 0);

        // Initializing textView objects
        itemsTotalamt = findViewById(R.id.itemsTotalamt);
        GSTamt = findViewById(R.id.GSTamt);
        totalamt = findViewById(R.id.totalamt);
        discountAmt = findViewById(R.id.discountAmt); // Discount amount TextView

        ImageView cartcrossbtn = findViewById(R.id.crossicon);
        cartcrossbtn.setOnClickListener(v -> finish());

        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> showPayment());

        recyclerView = findViewById(R.id.cartrv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Food> cartitems = cart.getInstance().getCartitems();
        cartAdapter cartAdapter = new cartAdapter(cartitems, this);
        recyclerView.setAdapter(cartAdapter);

        cartAdapter.notifyDataSetChanged();

        if (cart.getInstance().isCartempty()) {
            showAlertDialog();
        }
        updateCartSummary();
    }

    // Method to calculate and update the cart summary
    public void updateCartSummary() {
        // Get total price and GST from cart instance
        double total = cart.getInstance().getItemsTotal();
        double gst = cart.getInstance().getGST();

        // Display total price and GST in TextViews
        itemsTotalamt.setText(String.format("$%.2f", total));
        GSTamt.setText(String.format("$%.2f", gst));

        // Calculate total amount
        double totalAmount = total + gst;
        if (discountAmount > 0) {
            findViewById(R.id.discount).setVisibility(View.VISIBLE);
            discountAmt.setText(String.format("-$%.2f", discountAmount));
        } else {
            findViewById(R.id.discount).setVisibility(View.VISIBLE);
        }
        totalamt.setText(String.format("$%.2f", totalAmount - discountAmount)); // Apply discount
        totalPrice = totalAmount - discountAmount; // Update the totalPrice field with discount applied
    }

    // Method to show payment options
    private void showPayment() {
        View view = getLayoutInflater().inflate(R.layout.activity_payment_method, null);

        RadioGroup paymentGroup = view.findViewById(R.id.payment_method_group);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        Button payButton = view.findViewById(R.id.payButton);

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_peek_height));
        bottomSheetBehavior.setHideable(false);

        paymentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                payButton.setEnabled(true);
            } else {
                payButton.setEnabled(false);
            }
        });


        if (payButton != null) {
            payButton.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Payment Successful! You saved $" + discountAmount, Toast.LENGTH_SHORT).show();
                cart.getInstance().clearCart();
                convertToPoints(totalPrice); // Convert total amount to points
                // Clear the discount after payment
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("discount");
                editor.apply();
                discountAmount = 0; // Reset the discount amount

                Intent intent = new Intent(v.getContext(), productpage.class);
                startActivity(intent);
                finish();
            });

            dialog.show();
        }

        // Cancel button
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        // Cross icon to close the dialog
        ImageView cross = dialog.findViewById(R.id.cross);
        cross.setOnClickListener(v -> dialog.dismiss());
    }

    private void convertToPoints(double totalAmount) {
        int earnedPoints = (int) totalAmount; // $1 = 1 point

        // Retrieve current points from Firestore
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("Accounts")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Long currentPoints = document.getLong("points");
                                currentPoints = currentPoints != null ? currentPoints : 0;
                                long newPoints = currentPoints + earnedPoints;
                                String tier = calculateTier(newPoints);

                                db.collection("Accounts").document(document.getId())
                                        .update("points", newPoints, "tier", tier)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("UpdatePoints", "Points and tier updated successfully in Firestore");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("UpdatePoints", "Failed to update points and tier in Firestore", e);
                                        });

                                // Notify the user of the successful order confirmation and points earned
                                Toast.makeText(cartpage.this, "Order confirmed! You earned " + earnedPoints + " points. Total points: " + newPoints, Toast.LENGTH_LONG).show();
                                break;
                            }
                        } else {
                            Log.e("UpdatePoints", "Failed to find document with email: " + userEmail);
                        }
                    });
        } else {
            Log.e("UpdatePoints", "No authenticated user found");
        }
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cart is Empty")
                .setMessage("Cart is empty. Please add items before proceeding.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                .show();
    }

    private void redeemPoints(int pointsRequired, double discount) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("Accounts")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Long currentPoints = document.getLong("points");
                                if (currentPoints != null && currentPoints >= pointsRequired) {
                                    long newPoints = currentPoints - pointsRequired;
                                    db.collection("Accounts").document(document.getId())
                                            .update("points", newPoints)
                                            .addOnSuccessListener(aVoid -> {
                                                discountAmount += discount;
                                                // Save the discount amount to SharedPreferences
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putInt("discount", (int) discountAmount);
                                                editor.apply();
                                                updateCartSummary(); // Recalculate the cart summary with the discount
                                                Toast.makeText(cartpage.this, "Redeemed $" + discount + " off!", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("RedeemPoints", "Failed to update points in Firebase", e);
                                            });
                                } else {
                                    Toast.makeText(cartpage.this, "Not enough points to redeem this voucher.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Log.e("RedeemPoints", "Failed to find document with email: " + userEmail);
                        }
                    });
        } else {
            Log.e("RedeemPoints", "No authenticated user found");
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
}
