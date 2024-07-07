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

        // Retrieve the discount amount from SharedPreferences
        discountAmount = sharedPreferences.getInt("discount", 0);

        // Initializing textView objects
        itemsTotalamt = findViewById(R.id.itemsTotalamt);
        GSTamt = findViewById(R.id.GSTamt);
        totalamt = findViewById(R.id.totalamt);
        discountAmt = findViewById(R.id.discountAmt); // Discount amount TextView

        // cross button in cart page
        ImageView cartcrossbtn = findViewById(R.id.crossicon);
        cartcrossbtn.setOnClickListener(v -> finish());

        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> showPayment());

        recyclerView = findViewById(R.id.cartrv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // gets the list of cart items from cart instance
        List<Food> cartitems = cart.getInstance().getCartitems();
        // creates an adapter
        cartAdapter cartAdapter = new cartAdapter(cartitems, this);
        // notify adapter of data changes
        recyclerView.setAdapter(cartAdapter);

        cartAdapter.notifyDataSetChanged();

        if (cart.getInstance().isCartempty()) {
            showAlertDialog();
        }
        updateCartSummary();
    }

    // method to calculate and update the cart summary
    public void updateCartSummary() {
        // get total price and GST from cart instance
        double total = cart.getInstance().getItemsTotal(); // total price of all items in cart
        double gst = cart.getInstance().getGST(); // GST amt

        // apply discount if any
        double totalAmount = total + gst - discountAmount;

        // display total price and GST in TextViews & format as currency values
        itemsTotalamt.setText(String.format("$%.2f", total));
        GSTamt.setText(String.format("$%.2f", gst));
        discountAmt.setText(String.format("-$%.2f", discountAmount));
        totalamt.setText(String.format("$%.2f", totalAmount));

        // Reset discount amount in SharedPreferences if totalAmount is calculated
        if (discountAmount > 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("discount", 0);
            editor.apply();
        }
    }

    // method to show payments options
    private void showPayment() {
        View view = getLayoutInflater().inflate(R.layout.activity_payment_method, null);

        // finds 'RadioGroup' in the inflated view & stores as paymentGroup
        RadioGroup paymentGroup = view.findViewById(R.id.payment_method_group);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view); // sets content view of dialog to the inflated view
        Button payButton = view.findViewById(R.id.payButton); // finds the payButton

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)); // sets peak height of the bottom sheet behavior
        // makes the bottom sheet non-hideable -> appear to user for payment selection
        bottomSheetBehavior.setHideable(false);

        // check if a radio button is selected
        paymentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                payButton.setEnabled(true);
            } else {
                payButton.setEnabled(false);
            }
        });

        if (payButton != null) {
            payButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // once payButton is clicked, shows toast message
                    Toast.makeText(v.getContext(), "Payment Successful!", Toast.LENGTH_SHORT).show();

                    // Calculate total amount after discount
                    double totalAmount = cart.getInstance().getItemsTotal() + cart.getInstance().getGST() - discountAmount;

                    // Convert total amount to points
                    convertToPoints(totalAmount);

                    // Clear the cart
                    cart.getInstance().clearCart();

                    // Navigate to the product page
                    Intent intent = new Intent(v.getContext(), productpage.class);
                    startActivity(intent);
                    finish();
                }
            });

            dialog.show();
        }

        // cancel button
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        // cross
        ImageView cross = dialog.findViewById(R.id.cross);
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    // to convert the amount of money spent to points
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

    // To show an alert if cart is empty
    private void showAlertDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cart is Empty")
                .setMessage("Cart is empty. Please add items before proceeding.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show(); // show alert dialog
    }

    // to redeem points if there is
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
                                                discountAmount = discount; // Update discount amount only if redemption is successful
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
