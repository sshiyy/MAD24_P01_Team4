package sg.edu.np.mad.mad_p01_team4;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritesFragment extends Fragment {

    private RecyclerView favoritesRecyclerView;
    private FoodAdapter foodAdapter;
    private List<Food> favoriteItems;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawer;
    private NavigationView navigationView;
    private TextView emptyFavoritesMessage;
    private Map<Integer, Class<? extends Fragment>> fragmentMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_favorites, container, false);

        drawerLayout = view.findViewById(R.id.drawer_layout);
        buttonDrawer = view.findViewById(R.id.buttonDrawerToggle);
        navigationView = view.findViewById(R.id.navigationView);
        favoritesRecyclerView = view.findViewById(R.id.favoriterv);
        emptyFavoritesMessage = view.findViewById(R.id.emptyFavoritesMessage);
        buttonDrawer.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        initializeFragmentMap();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            Log.d(TAG, "Navigation item clicked: " + itemId);
            displaySelectedFragment(itemId);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        RelativeLayout cartbutton = view.findViewById(R.id.cart_button);
        cartbutton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new cartFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        favoriteItems = new ArrayList<>();
        foodAdapter = new FoodAdapter(new ArrayList<>(favoriteItems), getContext(), R.layout.custom_itemlist_small, this); // Pass FavoritesFragment instance
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        favoritesRecyclerView.setLayoutManager(gridLayoutManager);
        favoritesRecyclerView.setAdapter(foodAdapter);

        // Load favorite items
        loadFavoriteItems();

        return view;
    }


    private void loadFavoriteItems() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Handle user not logged in
            Log.e(TAG, "User not logged in");
            return;
        }

        // First fetch the list of favorites for the current user
        db.collection("favorites")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> favoriteFoodNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            favoriteFoodNames.add(document.getString("foodName"));
                        }

                        // Now fetch the food items from the Food_Items collection
                        fetchFoodItems(favoriteFoodNames);
                    } else {
                        Log.w(TAG, "Error getting favorites.", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load favorites", e);
                });
    }

    private void fetchFoodItems(List<String> favoriteFoodNames) {
        db.collection("Food_Items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        favoriteItems.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Food food = document.toObject(Food.class);
                            food.setModifications((List<Map<String, Object>>) document.get("modifications"));

                            // Check if this food item is in the list of favorite food names
                            if (favoriteFoodNames.contains(food.getName())) {
                                favoriteItems.add(food);
                            }
                        }
                        foodAdapter.updateList(new ArrayList<>(favoriteItems)); // Update FoodAdapter
                        toggleEmptyMessage();
                    } else {
                        Log.w(TAG, "Error getting food items.", task.getException());
                    }
                });
    }


    public void toggleEmptyMessage() {
        if (favoriteItems.isEmpty()) {
            emptyFavoritesMessage.setVisibility(View.VISIBLE);
            favoritesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyFavoritesMessage.setVisibility(View.GONE);
            favoritesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void updateFavoriteItems(List<Food> newList) {
        favoriteItems.clear();
        favoriteItems.addAll(newList);
        toggleEmptyMessage();
    }

    private void initializeFragmentMap() {
        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.navMenu, productFragment.class);
        fragmentMap.put(R.id.navCart, cartFragment.class);
        fragmentMap.put(R.id.navAccount, profileFragment.class);
        fragmentMap.put(R.id.navMap, mapFragment.class);
        fragmentMap.put(R.id.navPoints, pointsFragment.class);
        fragmentMap.put(R.id.navFavourite, FavoritesFragment.class);
        fragmentMap.put(R.id.navOngoingOrders, ongoingFragment.class);
        fragmentMap.put(R.id.navHistory, orderhistoryFragment.class);
    }

    private void displaySelectedFragment(int itemId) {
        Class<? extends Fragment> fragmentClass = fragmentMap.get(itemId);
        if (fragmentClass != null) {
            try {
                Fragment selectedFragment = fragmentClass.newInstance();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null)
                        .commit();
                Log.d(TAG, "Fragment transaction committed for: " + fragmentClass.getSimpleName());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                Log.e(TAG, "Error instantiating fragment: " + e.getMessage());
            } catch (java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }
        } else {
            Log.e(TAG, "Unknown navigation item selected: " + itemId);
        }
    }
}
