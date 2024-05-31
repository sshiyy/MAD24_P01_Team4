package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class Checkout extends AppCompatActivity {


    private RadioButton Card, DBS, PayNow, paypal;
    private Button payButton;
    private BottomSheetDialog dialog;
    private RadioGroup paymentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        paymentGroup = findViewById(R.id.payment_method_group);
        Card = findViewById(R.id.Card);
        DBS = findViewById(R.id.DBS);
        PayNow = findViewById(R.id.PayNow);
        paypal = findViewById(R.id.paypal);
        payButton = findViewById(R.id.payButton);

        paymentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) { // check if a button is selected
                    payButton.setEnabled(true); // make this clickable for user
                    payButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            Toast.makeText(v.getContext(), "Payment Successful!", Toast.LENGTH_SHORT).show();
                            cart.getInstance().clearCart();
                            Intent intent = new Intent(v.getContext(), productpage.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    payButton.setEnabled(false);
                }
            }
        });

        // Initialize the BottomSheetDialog
        dialog = new BottomSheetDialog(this);

    }
    public void dismissBottomSheet(View view) {
        dialog.dismiss();
    }
}
