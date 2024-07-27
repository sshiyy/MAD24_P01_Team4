package sg.edu.np.mad.mad_p01_team4;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class orderhistoryFragment extends Fragment {

    private static final String TAG = "OrderHistoryFragment";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private OrderHistoryAdapter orderHistoryAdapter;
    private ImageButton buttonDrawer;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Map<Integer, Class<? extends Fragment>> fragmentMap;
    private TextView nohistoryMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_orderhistory, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.historyrv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderHistoryAdapter = new OrderHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(orderHistoryAdapter);

        drawerLayout = view.findViewById(R.id.drawer_layout);
        buttonDrawer = view.findViewById(R.id.buttonDrawerToggle);
        navigationView = view.findViewById(R.id.navigationView);
        nohistoryMessage = view.findViewById(R.id.nohistoryMessage);

        // Set the drawer toggle button listener
        buttonDrawer.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        initializeFragmentMap();

        // Initialize NavigationView
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            displaySelectedFragment(itemId);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        loadOrderHistory();

        return view;
    }

    private void loadOrderHistory() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        db.collection("order_history")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, List<Order>> ordersMap = new HashMap<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setDocumentId(document.getId()); // Set the document ID for potential future use
                        String orderId = order.getOrderId(); // Use orderId for grouping

                        if (!ordersMap.containsKey(orderId)) {
                            ordersMap.put(orderId, new ArrayList<>());
                        }
                        ordersMap.get(orderId).add(order);
                    }

                    List<OrderGroup> orderGroups = new ArrayList<>();
                    for (Map.Entry<String, List<Order>> entry : ordersMap.entrySet()) {
                        orderGroups.add(new OrderGroup(entry.getKey(), entry.getValue()));
                    }

                    if (orderGroups.isEmpty()) {
                        nohistoryMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        nohistoryMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        orderHistoryAdapter.updateOrderGroups(orderGroups);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load order history", e));
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
        fragmentMap.put(R.id.navFavourite, FavoritesFragment.class);
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