package sg.edu.np.mad.mad_p01_team4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class mapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "mapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ProgressBar progressBar;

    // UI Elements
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawer;
    private NavigationView navigationView;
    private TextView distanceTextView;
    private Spinner modeSpinner;

    // Map and Location Variables
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private final LatLng[] cafeLocations = new LatLng[]{
            new LatLng(1.3366473055287562, 103.77138756904179), // Café 1
            new LatLng(1.337653013114712, 103.78039747684988), // Café 2
            new LatLng(37.42039711108658, -122.0861325112942)  // Café 3
    };

    // Fragment map for navigation drawer
    private Map<Integer, Class<? extends Fragment>> fragmentMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);
        progressBar = view.findViewById(R.id.progressBar);

        // Set up the toolbar and navigation drawer
        drawerLayout = view.findViewById(R.id.drawer_layout);
        buttonDrawer = view.findViewById(R.id.buttonDrawerToggle);
        navigationView = view.findViewById(R.id.navigationView);

        buttonDrawer.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        fragmentMap = new HashMap<>();
        initializeFragmentMap();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            Log.d(TAG, "Navigation item clicked: " + itemId);
            displaySelectedFragment(itemId);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        distanceTextView = view.findViewById(R.id.distance_text);

        // Initialize the spinner
        modeSpinner = view.findViewById(R.id.mode_spinner);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mMap != null) {
                    // Recalculate the route when the mode of transportation changes
                    checkLocationPermission();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Apply window insets for proper layout adjustments
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Create location request
        locationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Create location callback to handle location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateLocationUI(location);
                    }
                }
            }
        };

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        // Check for location permissions and request if not granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Enable location on the map
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateLocationUI(Location location) {
        showProgressBar();
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

        distanceTextView.setText("Distances to cafes:\n");

        // Calculate distances to cafes and find the closest one
        float[] results = new float[1];
        LatLng closestCafe = null;
        float closestDistance = Float.MAX_VALUE;

        for (LatLng cafeLocation : cafeLocations) {
            Location.distanceBetween(userLocation.latitude, userLocation.longitude, cafeLocation.latitude, cafeLocation.longitude, results);
            float distance = results[0] / 1000; // Convert to kilometers
            if (distance < closestDistance) {
                closestDistance = distance;
                closestCafe = cafeLocation;
            }
        }

        if (closestCafe != null) {
            mMap.clear(); // Clear existing markers
            mMap.addMarker(new MarkerOptions().position(closestCafe).title("Closest Cafe: " + closestDistance + " km"));
            distanceTextView.append(String.format("Closest Cafe: %.2f km\n", closestDistance));

            // Fetch and display route
            fetchRoute(userLocation, closestCafe);
        } else {
            hideProgressBar();
        }
    }

    private void fetchRoute(LatLng origin, LatLng destination) {
        String apiKey = getString(R.string.google_maps_key); // Retrieve the Google Maps API key from strings.xml
        showProgressBar(); // Display progress bar

        // Get the selected mode of transportation
        String mode = getTransportMode();

        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude + "," + origin.longitude + "&destination=" + destination.latitude + "," + destination.longitude + "&mode=" + mode + "&key=" + apiKey;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                JSONArray routes = response.getJSONArray("routes");
                if (routes.length() > 0) {
                    hideProgressBar();
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String points = overviewPolyline.getString("points");
                    List<LatLng> decodedPath = PolyUtil.decode(points);

                    mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.BLUE).width(10));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error parsing route", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            error.printStackTrace();
            hideProgressBar();
            Toast.makeText(getContext(), "Error fetching route", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonObjectRequest);
    }

    private String getTransportMode() {
        String mode = modeSpinner.getSelectedItem().toString().toLowerCase();
        switch (mode) {
            case "driving":
            case "walking":
            case "bicycling":
            case "transit":
                return mode;
            default:
                return "driving";
        }
    }

    private void initializeFragmentMap() {
        fragmentMap.put(R.id.navMenu, productFragment.class);
        fragmentMap.put(R.id.navCart, cartFragment.class);
        fragmentMap.put(R.id.navAccount, profileFragment.class);
        fragmentMap.put(R.id.navMap, mapFragment.class);
        fragmentMap.put(R.id.navPoints, pointsFragment.class);
        fragmentMap.put(R.id.navFavourite, FavoritesFragment.class);
        fragmentMap.put(R.id.navOngoingOrders, ongoingFragment.class);
        fragmentMap.put(R.id.navHistory, orderhistoryFragment.class);
        // Add more mappings as needed
    }

    private void displaySelectedFragment(int itemId) {
        Class<? extends Fragment> fragmentClass = fragmentMap.get(itemId);
        if (fragmentClass != null) {
            try {
                Fragment selectedFragment = fragmentClass.getDeclaredConstructor().newInstance();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null)
                        .commit();
                Log.d(TAG, "Fragment transaction committed for: " + fragmentClass.getSimpleName());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                e.printStackTrace();
                Log.e(TAG, "Error instantiating fragment: " + e.getMessage());
            } catch (java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }
        } else {
            Log.e(TAG, "Unknown navigation item selected: " + itemId);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                // Permission denied
                Log.e(TAG, "Location permission denied.");
            }
        }
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
