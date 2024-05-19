package sg.edu.np.mad.mad_p01_team4;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class Checkout extends AppCompatActivity {


    private Button checkoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_page);

        Button checkoutButton = findViewById(R.id.checkoutButton);
        checkoutButton.setBackgroundColor(getResources().getColor(R.color.orange));


        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPayment();
            }
        });

    }

    private void showPayment() {
        View view = getLayoutInflater().inflate(R.layout.activity_payment_method,null);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);

        // Set peek height and behavior for the dialog
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_peek_height));
        bottomSheetBehavior.setHideable(false);

        CheckBox creditCardCheckBox = dialog.findViewById(R.id.Card);
        CheckBox paypalCheckBox = dialog.findViewById(R.id.paypal);
        Button payButton = dialog.findViewById(R.id.payButton);

        View.OnClickListener checkBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payButton.setEnabled(creditCardCheckBox.isChecked() || paypalCheckBox.isChecked());
            }
        };

        creditCardCheckBox.setOnClickListener(checkBoxListener);
        paypalCheckBox.setOnClickListener(checkBoxListener);

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(Checkout.this, "Payment Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Checkout.this, Login_Page.class);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }
}
