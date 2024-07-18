package sg.edu.np.mad.mad_p01_team4;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class favorites extends AppCompatActivity {

    private RecyclerView favoritesRecyclerView;
    private TextView emptyFavoritesMessage;
    private FoodAdapter foodAdapter;
    private List<Food> favoriteItems;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyFavoritesMessage = findViewById(R.id.emptyFavoritesMessage);
        ImageView backButton = findViewById(R.id.crossicon);

        // Set up RecyclerView
        favoriteItems = new ArrayList<>();
        foodAdapter = new FoodAdapter(new ArrayList<>(favoriteItems), this); // Use FoodAdapter
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setAdapter(foodAdapter);

        // Load favorite items
        loadFavoriteItems();

        // Set up back button
        backButton.setOnClickListener(v -> finish());
    }

    private void loadFavoriteItems() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Handle user not logged in
            return;
        }

        db.collection("favorites")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteItems.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Food food = document.toObject(Food.class);
                        favoriteItems.add(food);
                    }
                    foodAdapter.updateList(new ArrayList<>(favoriteItems)); // Update FoodAdapter
                    toggleEmptyMessage();
                })
                .addOnFailureListener(e -> {
                    // Handle failure to load favorites
                });
    }

    private void toggleEmptyMessage() {
        if (favoriteItems.isEmpty()) {
            emptyFavoritesMessage.setVisibility(View.VISIBLE);
            favoritesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyFavoritesMessage.setVisibility(View.GONE);
            favoritesRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
