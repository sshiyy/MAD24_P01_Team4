package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.util.Random;

public class cartFragment extends Fragment {

    private static final String TAG = "CartFragment";

    private RecyclerView recyclerView;
    private RelativeLayout btnConfirm;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private List<Order> currentOrders;
    private cartAdapter cartAdapter;
    private TextView noGSTprice;
    private TextView GST;
    private TextView totalpricing;
    private TextView totalprice;
    private TextView discountPrice;

    private ImageButton crossBtn;
    private TextView emptyCartMessage;

    private Map<Integer, Class<? extends Fragment>> fragmentMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_cartpage, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize TextViews
        noGSTprice = view.findViewById(R.id.noGSTprice);
        GST = view.findViewById(R.id.GST);
        totalpricing = view.findViewById(R.id.totalpricing);
        totalprice = view.findViewById(R.id.totalprice);
        discountPrice = view.findViewById(R.id.discountPrice);
        emptyCartMessage = view.findViewById(R.id.emptyCartMessage);

        btnConfirm = view.findViewById(R.id.cfmbtn);
        btnConfirm.setOnClickListener(v -> showPayment());

        recyclerView = view.findViewById(R.id.cartrv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        crossBtn = view.findViewById(R.id.crossBtn);
        crossBtn.setOnClickListener(v -> getActivity().onBackPressed());

        // Initialize currentOrders list and cartAdapter
        currentOrders = new ArrayList<>();
        cartAdapter = new cartAdapter(currentOrders, getActivity());
        recyclerView.setAdapter(cartAdapter);

        // Attach the ItemTouchHelper to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Load current orders
        loadCurrentOrders();
        updatePricing();

        // Check for voucher discount in the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            int voucherDiscount = arguments.getInt("voucherDiscount", 0); // Default to 0 if not found
            Log.d(TAG, "Voucher discount: " + voucherDiscount);
            if (voucherDiscount != 0) {
                applyVoucherDiscount(voucherDiscount);
            }
        }

        return view;
    }

    private void applyVoucherDiscount(int voucherDiscount) {
        String totalPriceText = totalprice.getText().toString().replace("$", "");
        Log.d(TAG, "Total price text before applying discount: " + totalPriceText);

        try {
            double totalPrice = Double.parseDouble(totalPriceText);
            double newTotalPrice = totalPrice - voucherDiscount;
            totalprice.setText(String.format("$%.2f", newTotalPrice));

            // Set the discount price and make it visible
            discountPrice.setText(String.format("-$%.2f", (double) voucherDiscount));
            discountPrice.setVisibility(View.VISIBLE);

            Log.d(TAG, "Discount applied: " + voucherDiscount);
            Log.d(TAG, "New total price: " + newTotalPrice);

            // Remove the voucher from the database and update UI in pointsFragment
            removeVoucherFromDatabase();
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid total price format", e);
            Toast.makeText(getActivity(), "Invalid total price format", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeVoucherFromDatabase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("Accounts")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List<Map<String, Object>> vouchers = (List<Map<String, Object>>) document.get("vouchers");
                                if (vouchers != null) {
                                    for (Map<String, Object> voucherData : vouchers) {
                                        String title = (String) voucherData.get("title");
                                        if (title.equals(getArguments().getString("voucherTitle"))) {
                                            db.collection("Accounts").document(document.getId())
                                                    .update("vouchers", FieldValue.arrayRemove(voucherData))
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(getActivity(), "Voucher used and removed successfully!", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Failed to remove voucher", e);
                                                        Toast.makeText(getActivity(), "Failed to remove voucher: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "Failed to find document with email: " + userEmail, task.getException());
                            Toast.makeText(getActivity(), "Failed to find user account", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch document with email: " + userEmail, e);
                        Toast.makeText(getActivity(), "Failed to fetch user account", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "No authenticated user found");
            Toast.makeText(getActivity(), "No authenticated user found", Toast.LENGTH_SHORT).show();
        }
    }


    private void updatePricing() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("currently_ordering")
                    .whereEqualTo("userId", currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        double totalPrice = 0;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Order order = document.toObject(Order.class);
                            totalPrice += order.getPrice();
                        }
                        double gst = totalPrice * 0.09;
                        double priceWithoutGST = totalPrice - gst;

                        noGSTprice.setText(String.format("$%.2f", priceWithoutGST));
                        GST.setText(String.format("$%.2f", gst));
                        totalpricing.setText(String.format("$%.2f", totalPrice));
                        totalprice.setText(String.format("$%.2f", totalPrice)); // Ensure the dollar sign is included

                        Log.d(TAG, "Total price text after update: " + totalprice.getText().toString());

                        // Apply the voucher discount if any
                        Bundle arguments = getArguments();
                        if (arguments != null) {
                            int voucherDiscount = arguments.getInt("voucherDiscount", 0); // Default to 0 if not found
                            if (voucherDiscount != 0) {
                                applyVoucherDiscount(voucherDiscount);
                            }
                        }

                        if (totalPrice == 0) {
                            emptyCartMessage.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            btnConfirm.setEnabled(false);
                        } else {
                            emptyCartMessage.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            btnConfirm.setEnabled(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to load pricing details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Define the callback for the ItemTouchHelper
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Order order = currentOrders.get(position);
            if (direction == ItemTouchHelper.RIGHT) {
                // Handle edit order
                editOrder(order);
                cartAdapter.notifyItemChanged(position);
            } else if (direction == ItemTouchHelper.LEFT) {
                // Handle remove order
                removeOrderFromDatabase(order);
                currentOrders.remove(position); // Remove the item from the list
                cartAdapter.notifyItemRemoved(position); // Notify the adapter about the removal
                updateConfirmButtonState();
                updatePricing();
            }
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            View itemView = viewHolder.itemView;
            ColorDrawable background = new ColorDrawable();
            Drawable icon = null;
            int iconMargin;
            int iconTop;
            int iconBottom;

            if (dX > 0) { // Swiping to the right
                icon = ContextCompat.getDrawable(getActivity(), R.drawable.editicon);
                background.setColor(Color.BLUE);
                background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX), itemView.getBottom());
            } else if (dX < 0) { // Swiping to the left
                icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_delete);
                background.setColor(Color.RED);
                background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else { // View is unswiped
                background.setBounds(0, 0, 0, 0);
            }

            if (icon != null) {
                iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                iconTop = itemView.getTop() + iconMargin;
                iconBottom = iconTop + icon.getIntrinsicHeight();

                if (dX > 0) { // Swiping to the right
                    icon.setBounds(itemView.getLeft() + iconMargin, iconTop, itemView.getLeft() + iconMargin + icon.getIntrinsicWidth(), iconBottom);
                } else if (dX < 0) { // Swiping to the left
                    icon.setBounds(itemView.getRight() - iconMargin - icon.getIntrinsicWidth(), iconTop, itemView.getRight() - iconMargin, iconBottom);
                }
                icon.draw(c);
            }

            background.draw(c);
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    // Method to show payment options
    private void showPayment() {
        View view = getLayoutInflater().inflate(R.layout.activity_payment_method, null);
        RadioGroup paymentGroup = view.findViewById(R.id.payment_method_group);
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
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
                Toast.makeText(v.getContext(), "Payment Successful!", Toast.LENGTH_SHORT).show();
                moveOrdersToOngoing();
                double totalPrice = Double.parseDouble(totalpricing.getText().toString().substring(1));
                updatePointsAfterCheckout(totalPrice);

                // Navigate to the points fragment to show updated points
                Fragment pointsFragment = new pointsFragment();
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, pointsFragment); // Replace R.id.fragment_container with the ID of your container layout
                transaction.addToBackStack(null);
                transaction.commit();
                dialog.dismiss();
            });
            dialog.show();
        }

        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        }

        ImageView cross = dialog.findViewById(R.id.cross);
        cross.setOnClickListener(v -> dialog.dismiss());
    }


    private void loadCurrentOrders() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(getActivity(), loginPage.class);
            startActivity(intent);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("currently_ordering")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    currentOrders.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setDocumentId(document.getId()); // Set the document ID
                        currentOrders.add(order);
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateConfirmButtonState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to load current orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to remove order from database
    private void removeOrderFromDatabase(Order order) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("currently_ordering")
                .document(order.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    String message = "Order " + order.getFoodName() + " removed";
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    String message = "Failed to delete order " + order.getFoodName();
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                });
    }

    // Method to edit order
    private void editOrder(Order order) {
        // Implement edit order logic here
        Toast.makeText(getActivity(), "Edit order: " + order.getFoodName(), Toast.LENGTH_SHORT).show();
    }

    private void updateConfirmButtonState() {
        if (currentOrders.isEmpty()) {
            btnConfirm.setEnabled(false);
        } else {
            btnConfirm.setEnabled(true);
        }
    }

    private void moveOrdersToOngoing() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String orderId = generateRandomOrderId();
            for (Order order : currentOrders) {
                order.setOrderId(orderId); // Set the orderId field
                db.collection("ongoing_orders")
                        .add(order)
                        .addOnSuccessListener(documentReference -> {
                            db.collection("currently_ordering")
                                    .document(order.getDocumentId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Order moved to ongoing_orders and deleted from currently_ordering"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete order from currently_ordering", e));
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to add order to ongoing_orders", e));
            }
            // Clear the current orders list and notify the adapter
            currentOrders.clear();
            cartAdapter.notifyDataSetChanged();
            updateConfirmButtonState();
        }
    }

    private String generateRandomOrderId() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000)); // Generates a random 4-digit number and formats it as String
    }

    private void updatePointsAfterCheckout(double totalPrice) {
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
                                long newPoints = (currentPoints != null ? currentPoints : 0) + (long) totalPrice; // 1 dollar equals 1 point

                                String newTier = calculateTier(newPoints);
                                db.collection("Accounts").document(document.getId())
                                        .update("points", newPoints, "tier", newTier)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Points and tier updated successfully in Firestore");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to update points and tier in Firestore", e);
                                        });
                                break;
                            }
                        } else {
                            Log.d(TAG, "Failed to fetch user details", task.getException());
                        }
                    });
        } else {
            Log.e(TAG, "No authenticated user found");
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
}
