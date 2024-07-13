package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class cartpage extends AppCompatActivity {

    private static final String TAG = "CartPage";

    private RecyclerView recyclerView;
    private Button btnConfirm;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private List<Order> currentOrders;
    private cartAdapter cartAdapter;

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

        // Cross button in cart page
        ImageView cartcrossbtn = findViewById(R.id.crossicon);
        cartcrossbtn.setOnClickListener(v -> finish());

        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> showPayment());

        recyclerView = findViewById(R.id.cartrv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize currentOrders list and cartAdapter
        currentOrders = new ArrayList<>();
        cartAdapter = new cartAdapter(currentOrders, this);
        recyclerView.setAdapter(cartAdapter);

        // Load current orders
        loadCurrentOrders();

        // Attach the ItemTouchHelper to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    // Define the callback for the ItemTouchHelper
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false; // We are not implementing onMove in this example
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
                icon = ContextCompat.getDrawable(cartpage.this, R.drawable.editicon);
                background.setColor(Color.BLUE);
                background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX), itemView.getBottom());
            } else if (dX < 0) { // Swiping to the left
                icon = ContextCompat.getDrawable(cartpage.this, R.drawable.ic_delete);
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

        // Finds 'RadioGroup' in the inflated view & stores as paymentGroup
        RadioGroup paymentGroup = view.findViewById(R.id.payment_method_group);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view); // Sets content view of dialog to the inflated view
        Button payButton = view.findViewById(R.id.payButton); // Finds the payButton

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)); // Sets peak height of the bottom sheet behavior
        // Makes the bottom sheet non-hideable -> appear to user for payment selection
        bottomSheetBehavior.setHideable(false);

        // Check if a radio button is selected
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
                    // Once payButton is clicked, shows toast message
                    Toast.makeText(v.getContext(), "Payment Successful!", Toast.LENGTH_SHORT).show();

                    // Navigate to the product page
                    Intent intent = new Intent(v.getContext(), productpage.class);
                    startActivity(intent);
                    finish();
                }
            });

            dialog.show();
        }

        // Cancel button
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        // Cross button
        ImageView cross = dialog.findViewById(R.id.cross);
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void loadCurrentOrders() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, Login_Page.class);
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
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load current orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to remove order from database
    private void removeOrderFromDatabase(Order order) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("currently_ordering")
                .document(order.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(cartpage.this, "Order deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(cartpage.this, "Failed to delete order", Toast.LENGTH_SHORT).show());
    }

    // Method to edit order
    private void editOrder(Order order) {
        // Implement edit order logic here
        Toast.makeText(cartpage.this, "Edit order: " + order.getFoodName(), Toast.LENGTH_SHORT).show();
    }
}
