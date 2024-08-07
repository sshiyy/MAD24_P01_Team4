package sg.edu.np.mad.mad_p01_team4;

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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ongoingFragment extends Fragment {

    private static final String TAG = "OngoingFragment";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private ImageButton buttonDrawer;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Map<Integer, Class<? extends Fragment>> fragmentMap;
    private TextView noordermsg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_ongoingorders, container, false);

        // initialize firebase and firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // initialise recyclerview and set layout
        recyclerView = view.findViewById(R.id.ongoingrv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderAdapter = new OrderAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(orderAdapter);

        // initialize
        drawerLayout = view.findViewById(R.id.drawer_layout);
        buttonDrawer = view.findViewById(R.id.buttonDrawerToggle);
        navigationView = view.findViewById(R.id.navigationView);
        noordermsg = view.findViewById(R.id.noongoingMessage);

        buttonDrawer.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        initializeFragmentMap();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            displaySelectedFragment(itemId);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        loadOrders();

        return view;
    }

    // method to load order from firebase
    public void loadOrders() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        db.collection("ongoing_orders")
                .whereEqualTo("userId", currentUser.getUid())
                .get(Source.CACHE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        processOrders(task.getResult());
                    }

                    // fetch data from the server to get the latest updates
                    db.collection("ongoing_orders")
                            .whereEqualTo("userId", currentUser.getUid())
                            .get(Source.SERVER)
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                processOrders(queryDocumentSnapshots);
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to load orders from server", e));
                });
    }

    // method to process the orders retrieved from firestore
    private void processOrders(QuerySnapshot queryDocumentSnapshots) {
        Map<String, List<Order>> ordersMap = new HashMap<>();
        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
            Order order = document.toObject(Order.class);
            order.setDocumentId(document.getId());
            String orderId = order.getOrderId();

            if (!ordersMap.containsKey(orderId)) {
                ordersMap.put(orderId, new ArrayList<>());
            }
            ordersMap.get(orderId).add(order);
        }

        List<OrderGroup> orderGroups = new ArrayList<>();
        for (Map.Entry<String, List<Order>> entry : ordersMap.entrySet()) {
            orderGroups.add(new OrderGroup(entry.getKey(), entry.getValue()));
        }

        // Sort the orderGroups by the timestamp of the first order in each group in descending order
        Collections.sort(orderGroups, new Comparator<OrderGroup>() {
            @Override
            public int compare(OrderGroup o1, OrderGroup o2) {
                if (!o1.getOrders().isEmpty() && !o2.getOrders().isEmpty()) {
                    return Long.compare(o2.getOrders().get(0).getTimestamp(), o1.getOrders().get(0).getTimestamp());
                }
                return 0;
            }
        });

        if (orderGroups.isEmpty()) {
            noordermsg.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noordermsg.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            orderAdapter.updateOrderGroups(orderGroups);
        }
    }

    // method to initialize the fragment map for navigation
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

    // method to display the selected fragment based on the item id
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
