package sg.edu.np.mad.mad_p01_team4;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
                Intent crossintent = new Intent(cartpage.this, productpage.class);
                startActivity(crossintent);
            }
        });

        updateCartSummary();

        Button cfmbtn = findViewById(R.id.btnConfirm);
        cfmbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPayment();
            }
        });

        RecyclerView recyclerview = findViewById(R.id.cartrv);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        List<Food> cartitems = cart.getInstance().getCartitems();
        cartAdapter cartAdapter = new cartAdapter(cartitems, this);
        recyclerview.setAdapter(cartAdapter);

        cartAdapter.notifyDataSetChanged();

        if (cart.getInstance().isCartempty()) {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cart is Empty")
                .setMessage("Cart is empty. Please add items before proceeding.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    // method to calculate and update the cart summary
    private void updateCartSummary() {
        // get total price and GST from cart instance
        double total = cart.getInstance().getItemsTotal();
        double gst = cart.getInstance().getGST();

        // display total price and GST in TextViews
        itemsTotalamt.setText(String.format("$%.2f", total));
        GSTamt.setText(String.format("$%.2f", gst));

        // Calculate total amount
        double totalAmount = total + gst;
        totalamt.setText(String.format("$%.2f", totalAmount));
    }

    // method to show payments options
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
            payButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Payment Successful!", Toast.LENGTH_SHORT).show();
                    cart.getInstance().clearCart();
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

    public void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}

