package sg.edu.np.mad.mad_p01_team4;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class profileFragment extends Fragment {

    private static final String TAG = "AccountFragment";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private TextView nameTitle;
    private TextView pointsDisplay;
    private EditText emailDisplay;
    private EditText nameDisplay;
    private ImageView profilePic;

    private FirebaseUser currentUser;
    private String currentName;
    private String currentEmail;
    private String profilePicUrl;

    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawer;
    private NavigationView navigationView;

    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;
    private FirebaseAuth.AuthStateListener emailVerificationListener;

    private Map<Integer, Class<? extends Fragment>> fragmentMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_page, container, false);

        drawerLayout = view.findViewById(R.id.drawer_layout);
        buttonDrawer = view.findViewById(R.id.buttonDrawerToggle);
        navigationView = view.findViewById(R.id.navigationView);

        buttonDrawer.setOnClickListener(v -> drawerLayout.open());

        fragmentMap = new HashMap<>();
        initializeFragmentMap();

        // Set up the navigation drawer
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            Log.d(TAG, "Navigation item clicked: " + itemId);
            displaySelectedFragment(itemId);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        initFirebase();
        initUI(view);

        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return view;
        }

        fetchUserDetails();

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            Glide.with(this).load(selectedImageUri).apply(RequestOptions.circleCropTransform()).into(profilePic);
                            uploadProfileImage(); // Upload the image and update Firestore
                        }
                    }
                });

        profilePic.setOnClickListener(v -> ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).createIntent(new Function1<Intent, Unit>() {
            @Override
            public Unit invoke(Intent intent) {
                imagePickLauncher.launch(intent);
                return null;
            }
        }));

        // Initialize emailVerificationListener
        emailVerificationListener = auth -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null && user.isEmailVerified()) {
                if (currentEmail != null) {
                    Map<String, Object> updatedFields = new HashMap<>();
                    updatedFields.put("email", currentEmail);
                    updateFirestore(updatedFields);
                    mAuth.removeAuthStateListener(emailVerificationListener);
                }
            }
        };

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (emailVerificationListener != null) {
            mAuth.addAuthStateListener(emailVerificationListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (emailVerificationListener != null) {
            mAuth.removeAuthStateListener(emailVerificationListener);
        }
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    private void initUI(View view) {
        nameTitle = view.findViewById(R.id.titleName);
        pointsDisplay = view.findViewById(R.id.pointsNumber);
        emailDisplay = view.findViewById(R.id.profileEmail);
        nameDisplay = view.findViewById(R.id.profileName);
        profilePic = view.findViewById(R.id.profileImg);

        Button editProfileBtn = view.findViewById(R.id.btn_editProfile);
        Button deleteAccountBtn = view.findViewById(R.id.button2);
        Button logoutBtn = view.findViewById(R.id.btn_logout);

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    selectedImageUri = data.getData();
                    Glide.with(this).load(selectedImageUri).apply(RequestOptions.circleCropTransform()).into(profilePic);
                }
            }
        });

        editProfileBtn.setOnClickListener(v -> updateProfileIfNeeded());
        deleteAccountBtn.setOnClickListener(v -> showPasswordDialog(this::deleteAccount));
        logoutBtn.setOnClickListener(v -> logout());
    }

    private void fetchUserDetails() {
        String userEmail = currentUser.getEmail();
        if (userEmail != null) {
            db.collection("Accounts")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                currentName = document.getString("name");
                                currentEmail = document.getString("email");
                                profilePicUrl = document.getString("profilePicUrl");
                                Long points = document.getLong("points");

                                nameTitle.setText(currentName);
                                nameDisplay.setText(currentName);
                                emailDisplay.setText(currentEmail);

                                if (points != null) {
                                    pointsDisplay.setText(String.valueOf(points));
                                }

                                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                    Glide.with(this).load(profilePicUrl).apply(RequestOptions.circleCropTransform()).into(profilePic);
                                }

                                Log.d(TAG, "User details fetched");
                                break;
                            }
                        } else {
                            Log.d(TAG, "Failed to fetch user details", task.getException());
                        }
                    });
        } else {
            Log.d(TAG, "User email is null");
        }
    }

    private void updateProfileIfNeeded() {
        if (areDetailsChanged()) {
            updateProfile();
        } else {
            Toast.makeText(getActivity(), "No details changed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean areDetailsChanged() {
        String newName = nameDisplay.getText().toString().trim();
        String newEmail = emailDisplay.getText().toString().trim();
        return !newName.equals(currentName) || !newEmail.equals(currentEmail);
    }

    private void updateProfile() {
        String newName = nameDisplay.getText().toString().trim();
        String newEmail = emailDisplay.getText().toString().trim();

        Map<String, Object> updatedFields = new HashMap<>();
        if (!newName.isEmpty() && !newName.equals(currentName)) {
            updatedFields.put("name", newName);
        }

        if (!newEmail.isEmpty() && !newEmail.equals(currentEmail)) {
            showPasswordDialog(password -> reauthenticateAndChangeEmail(newEmail, password, updatedFields));
        } else {
            updateFirestore(updatedFields);
        }
    }

    private void showPasswordDialog(PasswordCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.activity_dialog_password_input, null);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle("Re-authenticate")
                .setMessage("Please enter your current password to update your profile.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = passwordInput.getText().toString();
                    callback.onPasswordEntered(password);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void reauthenticateAndChangeEmail(String newEmail, String password, Map<String, Object> updatedFields) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser.updateEmail(newEmail)
                                .addOnCompleteListener(emailUpdateTask -> {
                                    if (emailUpdateTask.isSuccessful()) {
                                        currentUser.sendEmailVerification()
                                                .addOnCompleteListener(verificationTask -> {
                                                    if (verificationTask.isSuccessful()) {
                                                        currentEmail = newEmail;
                                                        mAuth.addAuthStateListener(emailVerificationListener);
                                                        Toast.makeText(getActivity(), "Verification email sent. Please verify to complete the update.", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Log.d(TAG, "Failed to send verification email", verificationTask.getException());
                                                    }
                                                });
                                    } else {
                                        handleEmailUpdateError(emailUpdateTask.getException());
                                    }
                                });
                    } else {
                        handleReauthenticationError(task.getException());
                    }
                });
    }

    private void handleEmailUpdateError(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(getActivity(), "Invalid user.", Toast.LENGTH_SHORT).show();
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(getActivity(), "Invalid credentials.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Email update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleReauthenticationError(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(getActivity(), "Invalid user.", Toast.LENGTH_SHORT).show();
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(getActivity(), "Invalid credentials.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Reauthentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFirestore(Map<String, Object> updatedFields) {
        if (updatedFields.isEmpty()) {
            Toast.makeText(getActivity(), "No details to update", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();
        if (userEmail != null) {
            db.collection("Accounts")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                db.collection("Accounts").document(document.getId())
                                        .update(updatedFields)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                fetchUserDetails();
                                            } else {
                                                Toast.makeText(getActivity(), "Failed to update profile: " + updateTask.getException(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                break;
                            }
                        } else {
                            Log.d(TAG, "Failed to find user document", task.getException());
                        }
                    });
        }
    }

    private void uploadProfileImage() {
        if (selectedImageUri != null) {
            StorageReference profileImageRef = storageRef.child("profile_images/" + currentUser.getUid() + ".jpg");

            profileImageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                profilePicUrl = uri.toString();
                                Map<String, Object> updatedFields = new HashMap<>();
                                updatedFields.put("profilePicUrl", profilePicUrl);
                                updateFirestore(updatedFields);
                                Toast.makeText(getActivity(), "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to upload profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteAccount(String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        db.collection("Accounts").document(currentUser.getUid())
                                .delete()
                                .addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        currentUser.delete()
                                                .addOnCompleteListener(deleteAuthTask -> {
                                                    if (deleteAuthTask.isSuccessful()) {
                                                        Toast.makeText(getActivity(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                        redirectToLogin();
                                                    } else {
                                                        Toast.makeText(getActivity(), "Failed to delete account: " + deleteAuthTask.getException(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getActivity(), "Failed to delete Firestore document: " + deleteTask.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        handleReauthenticationError(task.getException());
                    }
                });
    }

    private void logout() {
        mAuth.signOut();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(getActivity(), loginPage.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        getActivity().finish();
    }

    private interface PasswordCallback {
        void onPasswordEntered(String password);
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
