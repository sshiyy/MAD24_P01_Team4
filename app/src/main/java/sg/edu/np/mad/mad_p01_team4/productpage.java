package sg.edu.np.mad.mad_p01_team4;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class productpage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.productpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // for mains
        ArrayList<Food> food_list = new ArrayList<>();
        food_list.add(new Food("Carbonara", 12.50, R.drawable.pastacarbonara));
        food_list.add(new Food("Bolognese", 11.00, R.drawable.pastabolognese));
        food_list.add(new Food("Aglio Olio", 10.00, R.drawable.aglioolio));
        food_list.add(new Food("Baked Rice", 11.50, R.drawable.baakedrice));
        food_list.add(new Food("Chicken Chop", 12.80, R.drawable.chickenchop));
        food_list.add(new Food("Fish & Chips", 13.50, R.drawable.fishnchips));
        food_list.add(new Food("Lasagna", 12.00,R.drawable.lasagna));
        food_list.add(new Food("Mac & Cheese", 10.00, R.drawable.macncheese));
        food_list.add(new Food("Risotto", 10.00, R.drawable.risotto));
        food_list.add(new Food("Steak with Rice", 15.80, R.drawable.steakwithegg));
        food_list.add(new Food("Steak with Potato", 15.80, R.drawable.steakwithpotato));


        // for pizza
        ArrayList<Food> pizza_list = new ArrayList<>();

        // for appetizer
        ArrayList<Food> appetizer_list = new ArrayList<>();

        // for side dish
        ArrayList<Food> sidedish_list = new ArrayList<>();

        // for dessert
        ArrayList<Food> dessert_list = new ArrayList<>();

        // for beverage
        ArrayList<Food> beverage_list = new ArrayList<>();


        // recyclerview
        FoodAdapter foodAdapter = new FoodAdapter(food_list, this);
        RecyclerView recyclerView = findViewById(R.id.productpagerv);
        GridLayoutManager gridlayoutman = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridlayoutman);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(foodAdapter);

    }
}