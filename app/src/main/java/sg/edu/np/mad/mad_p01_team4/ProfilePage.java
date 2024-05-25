package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfilePage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView usernameTitle;
    private TextView nameTitle;
    private TextView pointsDisplay;
    private EditText emailDisplay;
    private EditText nameDisplay;
    private EditText usernameDisplay;
    private Button editProfileBtn;

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

        // Get References
        ImageButton homebtn = findViewById(R.id.home);
        ImageButton orderbtn = findViewById(R.id.order);
        ImageButton favouritebtn = findViewById(R.id.favourites);
        ImageButton profilebtn = findViewById(R.id.account);
        ImageView logoutbtn = findViewById(R.id.iv_logout);
        usernameTitle = findViewById(R.id.titleUsername);
        nameTitle = findViewById(R.id.titleName);
        pointsDisplay = findViewById(R.id.pointsNumber);
        emailDisplay = findViewById(R.id.profileEmail);
        nameDisplay = findViewById(R.id.profileName);
        usernameDisplay = findViewById(R.id.profileUsername);
        editProfileBtn = findViewById(R.id.btn_editProfile);

        // Navbar
        homebtn.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, productpage.class)));
        orderbtn.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, Checkout.class)));

        // Handle logout
        logoutbtn.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(ProfilePage.this, Login_Page.class));
            finish();
        });

        // Fetch and display user's details
        fetchUserDetails(currentUser.getUid());

        // Handle profile edit
        editProfileBtn.setOnClickListener(v -> updateProfile(currentUser.getUid()));
    }

    private void fetchUserDetails(String userId) {
        db.collection("Accounts").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String username = document.getString("username");
                            String name = document.getString("name");
                            String email = document.getString("email");
                            Long points = document.getLong("points");

                            usernameTitle.setText(username); // Set the username to TextView
                            usernameDisplay.setHint(username); // Set the username to EditText
                            nameTitle.setText(name); // Set the name to TextView
                            nameDisplay.setHint(name); // Set the name to EditText
                            emailDisplay.setHint(email); // Set the email to EditText

                            if (points != null) {
                                pointsDisplay.setText(String.valueOf(points)); // Set the points to TextView
                            }

                            Log.d("ProfilePage", "User details fetched");
                        } else {
                            Log.d("ProfilePage", "No such document");
                        }
                    } else {
                        Log.d("ProfilePage", "get failed with ", task.getException());
                    }
                });
    }

    private void updateProfile(String userId) {
        String newUsername = usernameDisplay.getText().toString().trim();
        String newName = nameDisplay.getText().toString().trim();
        String newEmail = emailDisplay.getText().toString().trim();

        Map<String, Object> updatedFields = new HashMap<>();

        if (!newUsername.isEmpty()) {
            updatedFields.put("username", newUsername);
        }
        if (!newName.isEmpty()) {
            updatedFields.put("name", newName);
        }
        if (!newEmail.isEmpty()) {
            updatedFields.put("email", newEmail);
        }

        if (!updatedFields.isEmpty()) {
            db.collection("Accounts").document(userId).update(updatedFields)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfilePage.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        fetchUserDetails(userId); // Refresh user details after update
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfilePage.this, "Profile update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show();
        }
    }
}
