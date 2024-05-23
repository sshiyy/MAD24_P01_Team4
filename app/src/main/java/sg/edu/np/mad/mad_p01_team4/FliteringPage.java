package sg.edu.np.mad.mad_p01_team4;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FliteringPage extends AppCompatActivity {

    private SeekBar priceSeekBar;
    private TextView minPrice;
    private TextView maxPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flitering_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });



        // Find the ImageView by its ID
        ImageView crossImage = findViewById(R.id.crossImage);

        // Set an OnClickListener on it
        crossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use an Intent to navigate to the target activity (ProductActivity)
                Intent intent = new Intent(FliteringPage.this, productpage.class);
                startActivity(intent);
            }
        });

//        // Set click listeners for other ImageButtons (assuming you have them in your layout)
//        ImageView homeButton = findViewById(R.id.home);
//        homeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Navigate to HomeActivity
//                startActivity(new Intent(FliteringPage.this, productpage.class));
//            }
//        });
//
//        ImageView orderButton = findViewById(R.id.order);
//        orderButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Navigate to OrderActivity
//                startActivity(new Intent(FliteringPage.this, Checkout.class));
//            }
//        });

        // Set click listeners for favouritesButton and accountButton if you uncomment them

        //        ImageView favouritesButton = findViewById(R.id.favourites);
//        favouritesButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Navigate to FavouritesActivity
//                startActivity(new Intent(FliteringPage.this, FavouritesActivity.class));
//            }
//        });

//        ImageView accountButton = findViewById(R.id.account);
//        accountButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Navigate to AccountActivity
//                startActivity(new Intent(FliteringPage.this, AccountActivity.class));
//            }
//        });

    }


}
