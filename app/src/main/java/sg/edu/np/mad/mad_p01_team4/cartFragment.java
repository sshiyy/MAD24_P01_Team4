package sg.edu.np.mad.mad_p01_team4;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Collections;
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
                    .get(Source.CACHE) // First try to get data from the cache
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // If cache data is available, proceed with it
                            processPricing(task.getResult());
                        }

                        // Now, force fetch data from the server to get the latest updates
                        db.collection("currently_ordering")
                                .whereEqualTo("userId", currentUser.getUid())
                                .get(Source.SERVER)
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    processPricing(queryDocumentSnapshots);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Failed to load pricing details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
        }
    }

    private void processPricing(QuerySnapshot queryDocumentSnapshots) {
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
                showEditDialog(order, position);
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
            int iconLeft;
            int iconRight;

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

            background.draw(c);

            if (icon != null) {
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int iconHeight = itemHeight / 3; // Make the icon one-third of the item's height
                int iconWidth = icon.getIntrinsicWidth() * iconHeight / icon.getIntrinsicHeight();

                iconMargin = (itemHeight - iconHeight) / 2;
                iconTop = itemView.getTop() + iconMargin;
                iconBottom = iconTop + iconHeight;

                if (dX > 0) { // Swiping to the right
                    iconLeft = itemView.getLeft() + iconMargin;
                    iconRight = iconLeft + iconWidth;
                } else { // Swiping to the left
                    iconRight = itemView.getRight() - iconMargin;
                    iconLeft = iconRight - iconWidth;
                }

                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                icon.draw(c);
            }

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

                // Trigger the notification
                triggerCheckoutNotification();

                // Navigate to the points fragment to show updated points
                Fragment pointsFragment = new pointsFragment();
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, pointsFragment); // Replace R.id.fragment_container with the ID of your container layout
                transaction.addToBackStack(null);
                transaction.commit();
                dialog.dismiss();

                // Show AlertDialog to prompt user to add widget
                showAddWidgetDialog();
            });
            dialog.show();
        }

        ImageView cross = dialog.findViewById(R.id.cross);
        cross.setOnClickListener(v -> dialog.dismiss());
    }

    private void triggerCheckoutNotification() {
        Context context = getActivity();

        if (context != null) {
            // Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Checkout Channel";
                String description = "Channel for Checkout Notifications";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("checkout_channel_id", name, importance);
                channel.setDescription(description);

                // Register the channel with the system; you can't change the importance or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "checkout_channel_id")
                    .setSmallIcon(R.drawable.mainlogo) // Replace with your app's icon
                    .setContentTitle("Checkout Successful")
                    .setContentText("Thank you for your purchase!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            // Notification ID is a unique int for each notification that you must define
            int notificationId = 2;
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(notificationId, builder.build());
        }
    }
    // Method to show AlertDialog
    private void showAddWidgetDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Widget")
                .setMessage("Do you want to add the order widget to your home screen to track your order?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Start TimerService to update the widget
                    requireContext().startService(new Intent(requireContext(), TimerService.class));

                    // Provide instructions to the user
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Instructions")
                            .setMessage("To add the widget to your home screen:\n\n" +
                                    "1. Long press on an empty area on your home screen.\n" +
                                    "2. Select 'Widgets'.\n" +
                                    "3. Find and drag the 'Order Widget' to your home screen.")
                            .setPositiveButton("OK", null)
                            .show();
                })
                .setNegativeButton("No", null)
                .show();
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
                .get(Source.CACHE) // First try to get data from the cache
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // If cache data is available, proceed with it
                        processCurrentOrders(task.getResult());
                    }

                    // Now, force fetch data from the server to get the latest updates
                    db.collection("currently_ordering")
                            .whereEqualTo("userId", currentUser.getUid())
                            .get(Source.SERVER)
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                processCurrentOrders(queryDocumentSnapshots);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Failed to load current orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void processCurrentOrders(QuerySnapshot queryDocumentSnapshots) {
        currentOrders.clear();
        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
            Order order = document.toObject(Order.class);
            order.setDocumentId(document.getId()); // Set the document ID
            currentOrders.add(order);
        }
        cartAdapter.notifyDataSetChanged();
        updateConfirmButtonState();
        updatePricing();
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

    private void showEditDialog(Order order, int position) {
        // Inflate the dialog view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_food_detail, null);

        ImageView ivFoodImage = dialogView.findViewById(R.id.foodImage);
        TextView tvFoodDescription = dialogView.findViewById(R.id.descriptionTxt);
        LinearLayout modificationsLayout = dialogView.findViewById(R.id.modificationsLayout);
        EditText specialRequestInput = dialogView.findViewById(R.id.specialRequestInput);
        Button updateButton = dialogView.findViewById(R.id.addToCartButton); // Change to update button
        ImageButton closeButton = dialogView.findViewById(R.id.dialogcross);

        // Change the text of the button to "Update"
        updateButton.setText("Update");

        // Load image using Glide
        Glide.with(getContext())
                .load(order.getImg()) // Assuming img is a URL or path to the image
                .into(ivFoodImage);

        // Set food description (assuming description is the food name in this context)
        tvFoodDescription.setText(order.getFoodName());

        // Add checkboxes for modifications and tick the ones already made
        List<Map<String, Object>> modifications = order.getModifications();
        List<CheckBox> checkBoxes = new ArrayList<>();
        if (modifications != null) {
            for (Map<String, Object> modification : modifications) {
                String name = modification.keySet().iterator().next();
                Boolean value = (Boolean) modification.values().iterator().next();

                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setText(name);
                checkBox.setChecked(value);
                checkBoxes.add(checkBox);
                modificationsLayout.addView(checkBox);
            }
        }

        // Set the special request input text
        specialRequestInput.setText(order.getSpecialRequest());

        // Create and show alert dialog
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();
        alertDialog.show();

        // Set the click listener for the update button
        updateButton.setOnClickListener(v -> {
            List<Map<String, Object>> newModifications = new ArrayList<>();
            for (CheckBox checkBox : checkBoxes) {
                newModifications.add(Collections.singletonMap(checkBox.getText().toString(), checkBox.isChecked()));
            }
            String specialRequest = specialRequestInput.getText().toString();

            // Update the order
            order.setModifications(newModifications);
            order.setSpecialRequest(specialRequest);
            updateOrderInDatabase(order);
            currentOrders.set(position, order);
            cartAdapter.notifyItemChanged(position);
            alertDialog.dismiss(); // Close the dialog
        });

        // Set the click listener for the close button
        closeButton.setOnClickListener(v -> {
            cartAdapter.notifyItemChanged(position); // Reset the swipe state
            alertDialog.dismiss();
        });
    }

    private void updateOrderInDatabase(Order order) {
        // Update the order in the database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("currently_ordering").document(order.getDocumentId())
                .set(order)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Order successfully updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating order", e));
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
            List<Order> ordersToMove = new ArrayList<>(currentOrders); // Copy current orders to a new list

            for (Order order : ordersToMove) {
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
            updatePricing();
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
