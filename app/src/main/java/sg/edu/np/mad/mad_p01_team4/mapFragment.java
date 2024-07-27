package sg.edu.np.mad.mad_p01_team4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class mapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView distanceTextView;

    private final LatLng[] cafeLocations = {
            new LatLng(1.3366473055287562, 103.77138756904179), // Café 1
            new LatLng(1.337653013114712, 103.78039747684988), // Café 2
            new LatLng(37.42039711108658, -122.0861325112942)  // Café 3
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);

        distanceTextView = view.findViewById(R.id.distance_text);

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

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for (LatLng cafeLocation : cafeLocations) {
            mMap.addMarker(new MarkerOptions().position(cafeLocation).title("Cafe Location"));
        }

        // Get the user's location
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                            for (LatLng cafeLocation : cafeLocations) {
                                float[] results = new float[1];
                                Location.distanceBetween(userLocation.latitude, userLocation.longitude, cafeLocation.latitude, cafeLocation.longitude, results);
                                float distanceInMeters = results[0];

                                // Show distance to the closest cafe
                                distanceTextView.append(String.format("Distance to cafe: %.2f meters\n", distanceInMeters));

                                // Fetch and show the route
                                fetchRoute(userLocation, cafeLocation);
                            }
                        }
                    }
                });
    }

    private void fetchRoute(LatLng origin, LatLng destination) {
        new Thread(() -> {
            try {
                String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                        origin.latitude + "," + origin.longitude + "&destination=" +
                        destination.latitude + "," + destination.longitude + "&key=AIzaSyDGJRH2YB6yHLy9FVCdhA0MV2H6xnauKB0";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                parseRoute(response.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void parseRoute(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPoints = overviewPolyline.getString("points");
                List<LatLng> points = decodePolyline(encodedPoints);
                requireActivity().runOnUiThread(() -> {
                    mMap.addPolyline(new PolylineOptions()
                            .addAll(points)
                            .width(10)
                            .color(ContextCompat.getColor(requireContext(), R.color.green)));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                                        for (LatLng cafeLocation : cafeLocations) {
                                            float[] results = new float[1];
                                            Location.distanceBetween(userLocation.latitude, userLocation.longitude, cafeLocation.latitude, cafeLocation.longitude, results);
                                            float distanceInMeters = results[0];

                                            // Show distance to the closest cafe
                                            distanceTextView.append(String.format("Distance to cafe: %.2f meters\n", distanceInMeters));

                                            // Fetch and show the route
                                            fetchRoute(userLocation, cafeLocation);
                                        }
                                    }
                                }
                            });
                }
            }
        }
    }
}
