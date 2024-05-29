package sg.edu.np.mad.mad_p01_team4;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class cartpage extends AppCompatActivity {

    private TextView tvitemstotalprice;

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

        ImageView cartcrossbtn = findViewById(R.id.crossicon);
        cartcrossbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent crossintent = new Intent(cartpage.this, productpage.class);
                startActivity(crossintent);
            }
        });

        Button cfmbtn = findViewById(R.id.btnConfirm);
        cfmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cartpage.this, Checkout.class);
                startActivity(intent);
            }
        });



        RecyclerView recyclerview = findViewById(R.id.cartrv);
        tvitemstotalprice = findViewById(R.id.tvitemstotalprice);

        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        List<Food> cartitems = cart.getInstance().getCartitems();
        cartAdapter cartAdapter = new cartAdapter(cartitems, this);
        recyclerview.setAdapter(cartAdapter);

        cartAdapter.notifyDataSetChanged();

        if(cart.getInstance().isCartempty()){
            showAlertDialog();
        } else {
            updateItemstotalPrice();
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

    private void updateItemstotalPrice() {
        double totalprice = calculateTotalPrice();
        tvitemstotalprice.setText(String.format("$%.0f", totalprice));
    }

    private double calculateTotalPrice() {
        List<Food> cartitems = cart.getInstance().getCartitems();
        double totalprice = 0;
        for (Food food : cartitems) {
            totalprice += food.getPrice() * food.getQuantity();
        }
        return totalprice;
    }


}