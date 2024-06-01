package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
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
        orderbtn.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, cartpage.class)));

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
                            usernameDisplay.setText(username); // Set the username to EditText
                            nameTitle.setText(name); // Set the name to TextView
                            nameDisplay.setText(name); // Set the name to EditText
                            emailDisplay.setText(email); // Set the email to EditText

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

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (!newEmail.isEmpty() && currentUser != null) {
            // Show password input dialog for email change
            showPasswordDialog(currentUser, newEmail, updatedFields, userId);
        } else {
            updateFirestore(userId, updatedFields);
        }
    }

    private void showPasswordDialog(FirebaseUser currentUser, String newEmail, Map<String, Object> updatedFields, String userId) {
        // Inflate the custom layout for password input
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.activity_dialog_password_input, null);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Re-authenticate")
                .setMessage("Please enter your current password to update your email.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = passwordInput.getText().toString().trim();
                    if (!password.isEmpty()) {
                        reauthenticateAndChangeEmail(currentUser, newEmail, password, updatedFields, userId);
                    } else {
                        Toast.makeText(ProfilePage.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void reauthenticateAndChangeEmail(FirebaseUser currentUser, String newEmail, String password, Map<String, Object> updatedFields, String userId) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
        currentUser.reauthenticateAndRetrieveData(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update email in Firebase Authentication
                currentUser.updateEmail(newEmail).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        // Update email in Firestore
                        updatedFields.put("email", newEmail);
                        updateFirestore(userId, updatedFields);
                    } else {
                        if (task1.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(ProfilePage.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfilePage.this, "Email update failed: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                    Toast.makeText(ProfilePage.this, "User re-authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfilePage.this, "Re-authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateFirestore(String userId, Map<String, Object> updatedFields) {
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
