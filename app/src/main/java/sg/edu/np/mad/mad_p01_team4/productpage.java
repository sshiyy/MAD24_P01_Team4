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
        pizza_list.add(new Food("Margherita Pizza", 10.00, R.drawable.marghertiapizza));
        pizza_list.add(new Food("Pepperoni Pizza", 12.00, R.drawable.pepperonipizza));
        pizza_list.add(new Food("Cheese Pizza", 11.00, R.drawable.cheesepizza));
        pizza_list.add(new Food("Mushroom Pizza", 11.00, R.drawable.mushroompizza));

        // for appetizer
        ArrayList<Food> appetizer_list = new ArrayList<>();
        appetizer_list.add(new Food("Mushroom Soup", 9.00, R.drawable.mushroomsoupgarbread));
        appetizer_list.add(new Food("Cauliflower Soup", 7.00, R.drawable.cheesycauliflowersoup));
        appetizer_list.add(new Food("Clam Chowder", 8.00, R.drawable.clamchowder));
        appetizer_list.add(new Food("Avocado Toast", 7.00, R.drawable.avocadotoast));
        appetizer_list.add(new Food("Broccoli Toast", 6.00, R.drawable.broccoligarlictoastwithhoney));
        appetizer_list.add(new Food("Poached Salmon", 15.00, R.drawable.poachedsalmon));
        appetizer_list.add(new Food("Grilled Fish", 14.00, R.drawable.grilledfish));
        appetizer_list.add(new Food("Smoked Salmon Rosti", 12.00, R.drawable.smokesalmonrosti));
        appetizer_list.add(new Food("Salad", 6.00, R.drawable.salad));

        // for side dish
        ArrayList<Food> sidedish_list = new ArrayList<>();

        // for dessert
        ArrayList<Food> dessert_list = new ArrayList<>();

        // for beverage
        ArrayList<Food> beverage_list = new ArrayList<>();


        // recyclerview for mains
        FoodAdapter foodAdapter = new FoodAdapter(food_list, this);
        RecyclerView recyclerView = findViewById(R.id.productpagerv);
        GridLayoutManager gridlayoutman = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridlayoutman);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(foodAdapter);

        // recyclerview for pizza
        FoodAdapter pizzaAdapter = new FoodAdapter(pizza_list, this);
        RecyclerView pizzaRecyclerView = findViewById(R.id.pizzapagerv);
        GridLayoutManager pizzaLayoutManager = new GridLayoutManager(this, 2);
        pizzaRecyclerView.setLayoutManager(pizzaLayoutManager);
        pizzaRecyclerView.setItemAnimator(new DefaultItemAnimator());
        pizzaRecyclerView.setAdapter(pizzaAdapter);

        // recyclerview for appetizer
        FoodAdapter appetizerAdapter = new FoodAdapter(appetizer_list, this);
        RecyclerView appetizerRecyclerView = findViewById(R.id.appetizerpagerv);
        GridLayoutManager appetizerLayoutManager = new GridLayoutManager(this, 2);
        appetizerRecyclerView.setLayoutManager(appetizerLayoutManager);
        appetizerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        appetizerRecyclerView.setAdapter(appetizerAdapter);

    }
}