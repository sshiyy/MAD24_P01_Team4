package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private Button deleteAccountBtn;
    private Button logoutBtn;
    private FirebaseAuth.AuthStateListener emailVerificationListener;
    private String currentUsername;
    private String currentName;
    private String currentEmail;

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
        ImageButton starbtn = findViewById(R.id.point);
        ImageButton cartbtn = findViewById(R.id.cart_button);
        ImageButton profilebtn = findViewById(R.id.profile_button);
        usernameTitle = findViewById(R.id.titleUsername);
        nameTitle = findViewById(R.id.titleName);
        pointsDisplay = findViewById(R.id.pointsNumber);
        emailDisplay = findViewById(R.id.profileEmail);
        nameDisplay = findViewById(R.id.profileName);
        usernameDisplay = findViewById(R.id.profileUsername);
        editProfileBtn = findViewById(R.id.btn_editProfile);
        deleteAccountBtn = findViewById(R.id.button2);
        logoutBtn = findViewById(R.id.btn_logout);


        // Navbar
        homebtn.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, productpage.class)));
        cartbtn.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, cartpage.class)));
        profilebtn.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, ProfilePage.class)));
        starbtn.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, Points_Page.class)));

        // Handle logout
        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(ProfilePage.this, Login_Page.class));
            finish();
        });

        // Fetch and display user's details
        fetchUserDetails(currentUser);

        // Handle profile edit
        editProfileBtn.setOnClickListener(v -> {
            if (areDetailsChanged()) {
                updateProfile(currentUser);
            } else {
                Toast.makeText(ProfilePage.this, "No details changed", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle account deletion
        deleteAccountBtn.setOnClickListener(v -> deleteAccount(currentUser));
    }


    private void fetchUserDetails(FirebaseUser currentUser) {
        String userEmail = currentUser.getEmail();
        db.collection("Accounts")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            currentUsername = document.getString("username");
                            currentName = document.getString("name");
                            currentEmail = document.getString("email");
                            Long points = document.getLong("points");

                            usernameTitle.setText(currentUsername); // Set the username to TextView
                            usernameDisplay.setText(currentUsername); // Set the username to EditText
                            nameTitle.setText(currentName); // Set the name to TextView
                            nameDisplay.setText(currentName); // Set the name to EditText
                            emailDisplay.setText(currentEmail); // Set the email to EditText

                            if (points != null) {
                                pointsDisplay.setText(String.valueOf(points)); // Set the points to TextView
                            }

                            Log.d("ProfilePage", "User details fetched");
                            break; // Break after fetching the first matching document
                        }
                    } else {
                        Log.d("ProfilePage", "Failed to fetch user details", task.getException());
                    }
                });
    }

    private boolean areDetailsChanged() {
        String newUsername = usernameDisplay.getText().toString().trim();
        String newName = nameDisplay.getText().toString().trim();
        String newEmail = emailDisplay.getText().toString().trim();

        return !newUsername.equals(currentUsername) || !newName.equals(currentName) || !newEmail.equals(currentEmail);
    }

    private void updateProfile(FirebaseUser currentUser) {
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

        if (!newEmail.isEmpty() && !newEmail.equals(currentUser.getEmail())) {
            // Show password input dialog for email change
            showPasswordDialog(currentUser, newEmail, updatedFields);
        } else {
            updateFirestore(currentUser.getEmail(), updatedFields);
        }
    }

    private void showPasswordDialog(FirebaseUser currentUser, String newEmail, Map<String, Object> updatedFields) {
        // Inflate the custom layout for password input
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.activity_dialog_password_input, null);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Re-authenticate")
                .setMessage("Please enter your current password to update your email. A verification email will be sent to you!")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = passwordInput.getText().toString().trim();
                    if (!password.isEmpty()) {
                        reauthenticateAndChangeEmail(currentUser, newEmail, password, updatedFields);
                    } else {
                        Toast.makeText(ProfilePage.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void reauthenticateAndChangeEmail(FirebaseUser currentUser, String newEmail, String password, Map<String, Object> updatedFields) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Use verifyBeforeUpdateEmail to send a verification email to the new email address
                currentUser.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(ProfilePage.this, "Verification email sent to " + newEmail, Toast.LENGTH_LONG).show();

                        // Define the email verification listener
                        emailVerificationListener = auth -> {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                updatedFields.put("email", newEmail);
                                updateFirestore(user.getEmail(), updatedFields); // Update Firestore with the new email
                                // Remove the listener
                                mAuth.removeAuthStateListener(emailVerificationListener);
                            }
                        };

                        // Add the listener to FirebaseAuth
                        mAuth.addAuthStateListener(emailVerificationListener);

                        // Log out the current user and redirect to the login page
                        mAuth.signOut();
                        Toast.makeText(ProfilePage.this, "Verify your email before logging in!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProfilePage.this, Login_Page.class));
                        finish();
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

    private void updateFirestore(String oldEmail, Map<String, Object> updatedFields) {
        db.collection("Accounts")
                .whereEqualTo("email", oldEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            db.collection("Accounts").document(document.getId())
                                    .update(updatedFields)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ProfilePage.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                        fetchUserDetails(mAuth.getCurrentUser()); // Refresh user details after update
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ProfilePage.this, "Profile update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                            break; // Break after updating the first matching document
                        }
                    } else {
                        Toast.makeText(ProfilePage.this, "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAccount(FirebaseUser currentUser) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.activity_dialog_password_input, null);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Delete Account")
                .setMessage("Please enter your current password to delete your account.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = passwordInput.getText().toString().trim();
                    if (!password.isEmpty()) {
                        reauthenticateAndDeleteAccount(currentUser, password);
                    } else {
                        Toast.makeText(ProfilePage.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void reauthenticateAndDeleteAccount(FirebaseUser currentUser, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Delete user from Firestore
                db.collection("Accounts")
                        .whereEqualTo("email", currentUser.getEmail())
                        .get()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                                for (DocumentSnapshot document : task1.getResult()) {
                                    db.collection("Accounts").document(document.getId())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Delete user from Firebase Authentication
                                                currentUser.delete().addOnCompleteListener(task2 -> {
                                                    if (task2.isSuccessful()) {
                                                        Toast.makeText(ProfilePage.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(ProfilePage.this, Login_Page.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(ProfilePage.this, "Account deletion failed: " + task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(ProfilePage.this, "Failed to delete user from Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                    break; // Break after deleting the first matching document
                                }
                            } else {
                                Toast.makeText(ProfilePage.this, "Failed to delete user from Firestore: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
}
