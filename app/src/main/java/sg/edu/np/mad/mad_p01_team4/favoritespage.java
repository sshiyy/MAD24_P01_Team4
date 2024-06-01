package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class favoritespage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favoritespage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        List<Food> favoriteitems = FavoritesManager.getFavoriteItems();
        FoodAdapter foodadapter = new FoodAdapter((ArrayList<Food>) favoriteitems, this);
        recyclerView.setAdapter(foodadapter);


        ImageView cartcrossbtn = findViewById(R.id.crossicon);
        cartcrossbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent crossintent = new Intent(favoritespage.this, productpage.class);
                startActivity(crossintent);
            }
        });

        ImageView homeButton = findViewById(R.id.home);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to HomeActivity
                startActivity(new Intent(favoritespage.this, productpage.class));
            }
        });

        ImageView cartbutton = findViewById(R.id.cart_button);
        cartbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(favoritespage.this, cartpage.class));
            }
        });

        ImageView orderButton = findViewById(R.id.order);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to OrderActivity
                startActivity(new Intent(favoritespage.this, cartpage.class));
            }
        });

        ImageView favouritesButton = findViewById(R.id.favourites);
        favouritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to FavouritesActivity
                startActivity(new Intent(favoritespage.this, favoritespage.class));
            }
        });
    }
}