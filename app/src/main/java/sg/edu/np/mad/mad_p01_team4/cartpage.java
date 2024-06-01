package sg.edu.np.mad.mad_p01_team4;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;

public class cartpage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView itemsTotalamt, GSTamt, totalamt;
    private Button btnConfirm;

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


        // initializing textView objects
        itemsTotalamt = findViewById(R.id.itemsTotalamt);
        GSTamt = findViewById(R.id.GSTamt);
        totalamt = findViewById(R.id.totalamt);


        ImageView cartcrossbtn = findViewById(R.id.crossicon);
        cartcrossbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button cfmbtn = findViewById(R.id.btnConfirm);
        cfmbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPayment();
            }
        });

        RecyclerView recyclerview = findViewById(R.id.cartrv);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        // gets the list of cart items from cart instance
        List<Food> cartitems = cart.getInstance().getCartitems();
        // creates an adapter
        cartAdapter cartAdapter = new cartAdapter(cartitems, this);
        // notify adapter of data changes
        recyclerview.setAdapter(cartAdapter);

        cartAdapter.notifyDataSetChanged();

        // check if cart is empty
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

        // display total price and GST in TextViews & format as currency values
        itemsTotalamt.setText(String.format("$%.2f", total));
        GSTamt.setText(String.format("$%.2f", gst));

        // calculate total amount
        double totalAmount = total + gst;
        totalamt.setText(String.format("$%.2f", totalAmount));
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cart is Empty")
                .setMessage("Cart is empty. Please add items before proceeding.")

                // adds a positive button (ok) to dialog
                // when button is clicked, it calls the 'finish' method to close the activity
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show(); // show alert dialog
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
        // // makes the bottom sheet non-hideable -> appear to user for payment selection
        bottomSheetBehavior.setHideable(false);

        // check if a radio button is selected
        // yes (checkedId != 1) -> pay button is enable, user can click on it
        // no (checkedId == -1) -> pay button remains disabled, user cannot click on it
        paymentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                payButton.setEnabled(true);
            } else {
                payButton.setEnabled(false);
            }
        });

        // check if pay button is not null -> it is being enabled (user have selected a payment method)
        if (payButton != null) {
            payButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // once payButton is click, shows toast message
                    Toast.makeText(v.getContext(), "Payment Successful!", Toast.LENGTH_SHORT).show();
                    cart.getInstance().clearCart(); // clears cart
                    Intent intent = new Intent(v.getContext(), productpage.class);
                    // starts productpage activity -> direct user back to product page
                    // and finish current event
                    startActivity(intent);
                    finish();
                }
            });

            dialog.show();
        }

        // cancel button
        // finds cancel button in the dialog
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                // when clicked on 'cancel' it dismiss the dialog
                // dialog will be hidden from user
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        // cross
        // finds cross button in the dialog
        ImageView cross = dialog.findViewById(R.id.cross);
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            // when clicked on 'x' it dismiss the dialog
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}

