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
        appetizer_list.add(new Food("Cheese bread sticks",6.80, R.drawable.cheesybreadsticks));
        appetizer_list.add(new Food("Poached Salmon", 15.00, R.drawable.poachedsalmon));
        appetizer_list.add(new Food("Grilled Fish", 14.00, R.drawable.grilledfish));
        appetizer_list.add(new Food("Spicy Shrimp", 14.00, R.drawable.spicycaribbeanshrimp));
        appetizer_list.add(new Food("Smoked Salmon Rosti", 12.00, R.drawable.smokesalmonrosti));
        appetizer_list.add(new Food("Salad", 6.00, R.drawable.salad));

        // for side dish
        ArrayList<Food> sidedish_list = new ArrayList<>();
        sidedish_list.add(new Food("BBQ Sausage", 8.00, R.drawable.bbqsausage));
        sidedish_list.add(new Food("Buffalo Wings", 9.00, R.drawable.buffalowings));
        sidedish_list.add(new Food("Calamari", 8.50, R.drawable.calamari));
        sidedish_list.add(new Food("Curly Fries", 7.00, R.drawable.curlyfries));
        sidedish_list.add(new Food("Fries", 6.00, R.drawable.fries));
        sidedish_list.add(new Food("Honey Chicken Wings", 9.00, R.drawable.honeychickenwings));
        sidedish_list.add(new Food("Meatball & Cheese", 8.00, R.drawable.meatballandmozzarella));
        sidedish_list.add(new Food("Onion Ring", 5.00, R.drawable.onionrings));
        sidedish_list.add(new Food("Popcorn Chicken", 7.00, R.drawable.popcornchick));

        // for dessert
        ArrayList<Food> dessert_list = new ArrayList<>();
        dessert_list.add(new Food("Tiramisu Crepe Cake", 6.00, R.drawable.tiramisucrepecake));
        dessert_list.add(new Food("Tiramisu", 7.00, R.drawable.tiramisu));
        dessert_list.add(new Food("Strawberry Shortcake", 6.00, R.drawable.strawberryshortcake));
        dessert_list.add(new Food("Rainbow Crepe Cake", 6.00, R.drawable.rainbowcrepecake));
        dessert_list.add(new Food("Ice Cream Waffle", 7.00, R.drawable.icecreamwaffle));
        dessert_list.add(new Food("Ice Cream Croissant", 8.00, R.drawable.icecreamcroissant));
        dessert_list.add(new Food("Chocolate Cake", 6.00, R.drawable.chococake));
        dessert_list.add(new Food("Banana Split", 7.00, R.drawable.bananasplit));

        // for beverage
        ArrayList<Food> beverage_list = new ArrayList<>();
        beverage_list.add(new Food("Apple Juice", 3.50, R.drawable.applejuice));
        beverage_list.add(new Food("Avocado Milkshake", 5.00, R.drawable.avocadomilkshake));
        beverage_list.add(new Food("Coffee", 3.50, R.drawable.coffee));
        beverage_list.add(new Food("Hot Chocolate", 3.50, R.drawable.hotchoco));
        beverage_list.add(new Food("Lemonade", 2.50, R.drawable.lemonade));
        beverage_list.add(new Food("Mocha", 3.50, R.drawable.mocha));
        beverage_list.add(new Food("Orange Juice", 3.50, R.drawable.orangejuice));
        beverage_list.add(new Food("Root Beer", 4.50, R.drawable.rootbeer));
        beverage_list.add(new Food("Strawberry Smoothie", 5.00, R.drawable.strawberrysmoothie));


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

        // recyclerview for side dish
        FoodAdapter sidedishAdapter = new FoodAdapter(sidedish_list, this);
        RecyclerView sidedishRecyclerView = findViewById(R.id.sidedishpagerv);
        GridLayoutManager sidedishLayoutManager = new GridLayoutManager(this, 2);
        sidedishRecyclerView.setLayoutManager(sidedishLayoutManager);
        sidedishRecyclerView.setItemAnimator(new DefaultItemAnimator());
        sidedishRecyclerView.setAdapter(sidedishAdapter);

        // recyclerview for dessert
        FoodAdapter dessertAdapter = new FoodAdapter(dessert_list, this);
        RecyclerView dessertRecyclerView = findViewById(R.id.dessertpagerv);
        GridLayoutManager dessertLayoutManager = new GridLayoutManager(this, 2);
        dessertRecyclerView.setLayoutManager(dessertLayoutManager);
        dessertRecyclerView.setItemAnimator(new DefaultItemAnimator());
        dessertRecyclerView.setAdapter(dessertAdapter);

        // recyclerview for beverage
        FoodAdapter beverageAdapter = new FoodAdapter(beverage_list, this);
        RecyclerView beverageRecyclerView = findViewById(R.id.beveragepagerv);
        GridLayoutManager beverageLayoutManager = new GridLayoutManager(this, 2);
        beverageRecyclerView.setLayoutManager(beverageLayoutManager);
        beverageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        beverageRecyclerView.setAdapter(beverageAdapter);

    }
}