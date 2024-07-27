package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class registerPage extends AppCompatActivity {

    // Declare EditText and Button variables
    EditText regname, regpassword, regemail;
    Button registerbtn;

    private TextView logInRedirect;

    // Declare Firebase Firestore and FirebaseAuth instances
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    // Function to check for valid email format
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[\\w!#$%&'*+/=?^`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^`{|}~-]+)" +
                "*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Get references to the EditText and Button elements
        regname = findViewById(R.id.regname);
        regpassword = findViewById(R.id.regpassword);
        regemail = findViewById(R.id.regemail);
        registerbtn = findViewById(R.id.btn_register);
        logInRedirect = findViewById(R.id.tv_LogInHereText);

        // Initialize Firebase Firestore instance
        db = FirebaseFirestore.getInstance();

        // Set OnClickListener for the signup redirect text
        logInRedirect.setOnClickListener(v -> startActivity(new Intent(this, loginPage.class)));

        // Set OnClickListener for the register button
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text entered in EditTexts
                String regnameText = regname.getText().toString().trim();
                String regPasswordText = regpassword.getText().toString().trim();
                String regEmailText = regemail.getText().toString().trim();

                // Validate the input fields
                if (regnameText.isEmpty() || regPasswordText.isEmpty() || regEmailText.isEmpty()) {
                    popUp1.showPopup(registerPage.this, "Incomplete Form");
                    Log.d("Register-Page", "Incomplete Form");
                    return;
                }

                // Validate the email format
                if (!isValidEmail(regEmailText)) {
                    popUp1.showPopup(registerPage.this, "Invalid Email format!");
                    Log.d("Register-Page", "Invalid Email");
                    return;
                }

                // Validate if password length >= 6
                if (regPasswordText.length() < 6) {
                    popUp1.showPopup(registerPage.this, "Minimum 6 Characters for password!");
                    Log.d("Register-Page", "Password Length");
                    return;
                }

                // Check email existence before adding user
                ValidateEmailInFirestore(regEmailText);
            }
        });
    }

    // Function to validate if the email already exists in Firestore
    private void ValidateEmailInFirestore(String email) {
        db.collection("Accounts")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean emailAvailable = task.getResult().isEmpty();
                            if (emailAvailable) {
                                // Check email existence in Firebase Auth
                                ValidateEmailInFirebaseAuth(email);
                            } else {
                                popUp1.showPopup(registerPage.this, "Email Already Exists!");
                                Log.d("Register-Page", "Existing Email in Firestore");
                            }
                        } else {
                            Log.w("Register_Page", "Error checking email existence in Firestore", task.getException());
                            // Handle potential errors during email existence check in Firestore
                        }
                    }
                });
    }

    // Function to validate if the email already exists in Firebase Auth
    private void ValidateEmailInFirebaseAuth(String email) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    boolean emailExists = !task.getResult().getSignInMethods().isEmpty();
                    if (emailExists) {
                        popUp1.showPopup(registerPage.this, "Email Already Used!");
                        Log.d("Register-Page", "Existing Email in Firebase Auth");
                    } else {
                        // Email is available, proceed with registration logic
                        String regnameText = regname.getText().toString().trim();
                        String regPasswordText = regpassword.getText().toString().trim();
                        String regEmailText = regemail.getText().toString().trim();

                        // Call function to register the user
                        registerUser(regnameText, regPasswordText, regEmailText);
                    }
                } else {
                    Log.w("Register_Page", "Error checking email existence in Firebase Auth", task.getException());
                    // Handle potential errors during email existence check in Firebase Auth
                }
            }
        });
    }

    // Function to register the user with Firebase Authentication and store additional user data in FireStore
    private void registerUser(String name, String password, String email) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Send verification email
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Create a HashMap to store user data
                                                    HashMap<Object, Object> userMap = new HashMap<>();
                                                    userMap.put("name", name);
                                                    userMap.put("email", email); // Ensure email is correctly assigned
                                                    userMap.put("points", 0);
                                                    userMap.put("vouchers", new ArrayList<Map<String, Object>>() {{
                                                        add(new HashMap<String, Object>() {{
                                                            put("title", "$5 off your next purchase");
                                                            put("description", "Welcome Voucher");
                                                            put("discountAmt", 5);
                                                        }});
                                                    }}); // Initialize vouchers list

                                                    db.collection("Accounts").document(user.getUid()).set(userMap)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("Register-Page", "User registration successful!");
                                                                    popUp1.showPopup(registerPage.this, "Account created successfully! Please check your email for verification.");
                                                                    Intent intent = new Intent(registerPage.this, loginPage.class);
                                                                    startActivity(intent);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w("Register-Page", "Error registering user:", e);
                                                                    popUp1.showPopup(registerPage.this, "Registration failed! Please try again.");
                                                                }
                                                            });
                                                } else {
                                                    Log.e("Register-Page", "sendEmailVerification", task.getException());
                                                    popUp1.showPopup(registerPage.this, "Failed to send verification email.");
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.w("Register-Page", "createUserWithEmail:failure", task.getException());
                            popUp1.showPopup(registerPage.this, "Authentication failed.");
                        }
                    }
                });
    }
}
