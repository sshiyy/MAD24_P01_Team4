package sg.edu.np.mad.mad_p01_team4;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProductPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodAdapter foodAdapter;
    private List<food_item> foodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        foodList = new ArrayList<>();
        foodList.add(new food_item("Carbonara", 12.50, R.drawable.pastacarbonara));
        foodList.add(new food_item("Bolognese", 11.00, R.drawable.pastabolognese));


        foodAdapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(foodAdapter);
    }
}