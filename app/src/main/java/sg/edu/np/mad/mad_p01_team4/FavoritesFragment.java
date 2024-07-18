package sg.edu.np.mad.mad_p01_team4;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView favoritesRecyclerView;
    private TextView emptyFavoritesMessage;
    private FoodAdapter foodAdapter;
    private List<Food> favoriteItems;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_favorites, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView);
        emptyFavoritesMessage = view.findViewById(R.id.emptyFavoritesMessage);
        ImageView backButton = view.findViewById(R.id.crossicon);

        // Set up RecyclerView
        favoriteItems = new ArrayList<>();
        foodAdapter = new FoodAdapter(new ArrayList<>(favoriteItems), getContext()); // Use FoodAdapter
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoritesRecyclerView.setAdapter(foodAdapter);

        // Load favorite items
        loadFavoriteItems();

        // Set up back button
        backButton.setOnClickListener(v -> getActivity().onBackPressed());

        return view;
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
