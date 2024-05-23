package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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


        ImageButton filterbutton = findViewById(R.id.filterbutton);
        filterbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(productpage.this, FliteringPage.class);
                startActivity(intent);
            }
        });

        FloatingActionButton cartbutton = findViewById(R.id.cart_button);

        cartbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(productpage.this, cartpage.class);
                startActivity(intent);
            }
        });


        // for mains
        ArrayList<Food> food_list = new ArrayList<>();
        food_list.add(new Food("Carbonara", 13, R.drawable.pastacarbonara, "Creamy carbonara made with our homemade sauce"));
        food_list.add(new Food("Bolognese", 11, R.drawable.pastabolognese, "Classic spaghetti bolognese with rich meat sauce"));
        food_list.add(new Food("Aglio Olio", 10, R.drawable.aglioolio, "Simple and flavorful garlic and olive oil pasta"));
        food_list.add(new Food("Baked Rice", 12, R.drawable.baakedrice, "Oven-baked rice with a crispy cheese topping"));
        food_list.add(new Food("Chicken Chop", 13, R.drawable.chickenchop, "Juicy grilled chicken chop with sides"));
        food_list.add(new Food("Fish & Chips", 14, R.drawable.fishnchips, "Crispy fried fish served with golden fries"));
        food_list.add(new Food("Lasagna", 12, R.drawable.lasagna, "Layered pasta with rich meat sauce and cheese"));
        food_list.add(new Food("Mac & Cheese", 10, R.drawable.macncheese, "Creamy and cheesy macaroni pasta"));
        food_list.add(new Food("Risotto", 10, R.drawable.risotto, "Creamy Italian rice dish with vegetables"));
        food_list.add(new Food("Steak with Rice", 16, R.drawable.steakwithegg, "Grilled steak served with seasoned rice"));
        food_list.add(new Food("Steak with Potato", 16, R.drawable.steakwithpotato, "Grilled steak served with baked potato"));

// for pizza
        ArrayList<Food> pizza_list = new ArrayList<>();
        pizza_list.add(new Food("Margherita Pizza", 10, R.drawable.marghertiapizza, "Classic pizza with fresh tomatoes and mozzarella"));
        pizza_list.add(new Food("Pepperoni Pizza", 12, R.drawable.pepperonipizza, "Spicy pepperoni and melted cheese on a crispy crust"));
        pizza_list.add(new Food("Cheese Pizza", 11, R.drawable.cheesepizza, "Loaded with a blend of gooey cheeses"));
        pizza_list.add(new Food("Mushroom Pizza", 11, R.drawable.mushroompizza, "Savory mushrooms and herbs on a cheesy base"));

// for appetizer
        ArrayList<Food> appetizer_list = new ArrayList<>();
        appetizer_list.add(new Food("Mushroom Soup", 9, R.drawable.mushroomsoupgarbread, "Creamy mushroom soup with garlic bread"));
        appetizer_list.add(new Food("Cauliflower Soup", 7, R.drawable.cheesycauliflowersoup, "Smooth and creamy cauliflower soup"));
        appetizer_list.add(new Food("Clam Chowder", 8, R.drawable.clamchowder, "Rich and hearty clam chowder"));
        appetizer_list.add(new Food("Avocado Toast", 7, R.drawable.avocadotoast, "Toasted bread topped with fresh avocado"));
        appetizer_list.add(new Food("Broccoli Toast", 6, R.drawable.broccoligarlictoastwithhoney, "Crispy toast with roasted broccoli and honey"));
        appetizer_list.add(new Food("Cheese Bread Sticks", 7, R.drawable.cheesybreadsticks, "Cheesy and garlic-flavored bread sticks"));
        appetizer_list.add(new Food("Poached Salmon", 15, R.drawable.poachedsalmon, "Delicate poached salmon with herbs"));
        appetizer_list.add(new Food("Grilled Fish", 14, R.drawable.grilledfish, "Perfectly grilled fish with lemon butter"));
        appetizer_list.add(new Food("Spicy Shrimp", 14, R.drawable.spicycaribbeanshrimp, "Spicy Caribbean-style shrimp"));
        appetizer_list.add(new Food("Smoked Salmon Rosti", 12, R.drawable.smokesalmonrosti, "Crispy potato rosti topped with smoked salmon"));
        appetizer_list.add(new Food("Salad", 6, R.drawable.salad, "Fresh garden salad with a variety of vegetables"));

// for side dish
        ArrayList<Food> sidedish_list = new ArrayList<>();
        sidedish_list.add(new Food("BBQ Sausage", 8, R.drawable.bbqsausage, "Grilled BBQ sausages with a smoky flavor"));
        sidedish_list.add(new Food("Buffalo Wings", 9, R.drawable.buffalowings, "Spicy and tangy buffalo chicken wings"));
        sidedish_list.add(new Food("Calamari", 8, R.drawable.calamari, "Crispy fried calamari rings with dipping sauce"));
        sidedish_list.add(new Food("Curly Fries", 7, R.drawable.curlyfries, "Seasoned and crispy curly fries"));
        sidedish_list.add(new Food("Fries", 6, R.drawable.fries, "Classic golden fries, perfect for snacking"));
        sidedish_list.add(new Food("Honey Chicken Wings", 9, R.drawable.honeychickenwings, "Sweet and sticky honey-glazed wings"));
        sidedish_list.add(new Food("Meatball & Cheese", 8, R.drawable.meatballandmozzarella, "Juicy meatballs topped with melted cheese"));
        sidedish_list.add(new Food("Onion Ring", 5, R.drawable.onionrings, "Crispy and golden onion rings"));
        sidedish_list.add(new Food("Popcorn Chicken", 7, R.drawable.popcornchick, "Bite-sized crispy popcorn chicken"));

// for dessert
        ArrayList<Food> dessert_list = new ArrayList<>();
        dessert_list.add(new Food("Tiramisu Crepe Cake", 6, R.drawable.tiramisucrepecake, "Layered crepe cake with tiramisu flavors"));
        dessert_list.add(new Food("Tiramisu", 7, R.drawable.tiramisu, "Classic Italian tiramisu with coffee and mascarpone"));
        dessert_list.add(new Food("Strawberry Shortcake", 6, R.drawable.strawberryshortcake, "Light and fluffy cake with strawberries and cream"));
        dessert_list.add(new Food("Rainbow Crepe Cake", 6, R.drawable.rainbowcrepecake, "Colorful layered crepe cake with a sweet filling"));
        dessert_list.add(new Food("Ice Cream Waffle", 7, R.drawable.icecreamwaffle, "Warm waffle topped with ice cream"));
        dessert_list.add(new Food("Ice Cream Croissant", 8, R.drawable.icecreamcroissant, "Buttery croissant filled with ice cream"));
        dessert_list.add(new Food("Chocolate Cake", 6, R.drawable.chococake, "Rich and moist chocolate cake"));
        dessert_list.add(new Food("Banana Split", 7, R.drawable.bananasplit, "Classic banana split with ice cream and toppings"));

// for beverage
        ArrayList<Food> beverage_list = new ArrayList<>();
        beverage_list.add(new Food("Apple Juice", 4, R.drawable.applejuice, "Fresh and crisp apple juice"));
        beverage_list.add(new Food("Avocado Milkshake", 5, R.drawable.avocadomilkshake, "Creamy avocado milkshake"));
        beverage_list.add(new Food("Coffee", 4, R.drawable.coffee, "Freshly brewed coffee"));
        beverage_list.add(new Food("Hot Chocolate", 4, R.drawable.hotchoco, "Rich and creamy hot chocolate"));
        beverage_list.add(new Food("Lemonade", 3, R.drawable.lemonade, "Refreshing homemade lemonade"));
        beverage_list.add(new Food("Mocha", 4, R.drawable.mocha, "Chocolate-flavored coffee"));
        beverage_list.add(new Food("Orange Juice", 4, R.drawable.orangejuice, "Freshly squeezed orange juice"));
        beverage_list.add(new Food("Root Beer", 5, R.drawable.rootbeer, "Classic root beer soda"));
        beverage_list.add(new Food("Strawberry Smoothie", 5, R.drawable.strawberrysmoothie, "Sweet and creamy strawberry smoothie"));


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


        //intent for Filter
        Button filterButton = findViewById(R.id.filterbutton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(productpage.this, FliteringPage.class));

            }
        });

        // Set click listeners for other ImageButtons (assuming you have them in your layout)
        ImageView homeButton = findViewById(R.id.home);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to HomeActivity
                startActivity(new Intent(productpage.this, productpage.class));
            }
        });

        ImageView orderButton = findViewById(R.id.order);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to OrderActivity
                startActivity(new Intent(productpage.this, Checkout.class));
            }
        });

        // Set click listeners for favouritesButton and accountButton if you uncomment them

        //        ImageView favouritesButton = findViewById(R.id.favourites);
//        favouritesButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Navigate to FavouritesActivity
//                startActivity(new Intent(productpage.this, FavouritesActivity.class));
//            }
//        });

//        ImageView accountButton = findViewById(R.id.account);
//        accountButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Navigate to AccountActivity
//                startActivity(new Intent(productpage.this, AccountActivity.class));
//            }
//        });

    }
}