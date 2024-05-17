package sg.edu.np.mad.mad_p01_team4;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class FliteringPage extends AppCompatActivity {

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

        SeekBar priceSeekBar = findViewById(R.id.priceSeekBar);
        TextView minPriceTextView = findViewById(R.id.minPrice);
        TextView maxPriceTextView = findViewById(R.id.maxPrice);


        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the TextViews with the selected price range
                int minPrice = 5 + progress; // Adjust this according to your logic
                int maxPrice = 100 - progress; // Adjust this according to your logic
                minPriceTextView.setText("$" + minPrice);
                maxPriceTextView.setText("$" + maxPrice);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed here
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed here
            }
        });
    }


}