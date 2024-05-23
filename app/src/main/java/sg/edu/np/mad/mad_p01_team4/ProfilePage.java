package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilePage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView usernameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if the user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Redirect to Login Page
            startActivity(new Intent(ProfilePage.this, Login_Page.class));
            finish();
            Log.d("ProfilePage", "Not Logged In");
            return;
        }

        // Set window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get References
        ImageButton homebtn = findViewById(R.id.home);
        ImageButton orderbtn = findViewById(R.id.order);
        ImageButton favouritebtn = findViewById(R.id.favourites);
        ImageButton profilebtn = findViewById(R.id.account);
        ImageView logoutbtn = findViewById(R.id.iv_logout); // Add a logout button in your layout
        usernameTextView  = findViewById(R.id.tv_Name); // Add this TextView in your layout

        // Navbar
        homebtn.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, productpage.class)));
        orderbtn.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, Checkout.class)));

        // Handle logout
        logoutbtn.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(ProfilePage.this, Login_Page.class));
            finish();
        });

        // Fetch and display user's name
        fetchUserName(currentUser.getUid());
    }

    private void fetchUserName(String userId) {
        db.collection("Accounts").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String username = document.getString("username");
                            usernameTextView.setText(username); // Set the username to TextView
                            Log.d("ProfilePage", "Logged In");
                        } else {
                            Log.d("ProfilePage", "No such document");
                        }
                    } else {
                        Log.d("ProfilePage", "get failed with ", task.getException());
                    }
                });
    }
}
