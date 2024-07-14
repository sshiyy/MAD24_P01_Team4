package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
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
                if (regnameText.isEmpty() ||
                        regPasswordText.isEmpty() || regEmailText.isEmpty()) {
                    Toast.makeText(registerPage.this, "Incomplete Form", Toast.LENGTH_SHORT).show();
                    Log.d("Register-Page", "Incomplete Form");
                    return;
                }

                // Validate the email format
                if (!isValidEmail(regEmailText)) {
                    regemail.setError("Invalid Email format!"); // Set error on EditText
                    Log.d("Register-Page", "Invalid Email");
                    return; // Exit the function if email is invalid
                }

                // Validate if password length >= 6
                if(regPasswordText.length() < 6){
                    regpassword.setError("Minimum 6 Characters for password!"); // Set error on EditText
                    Log.d("Register-Page", "Password Length");
                    return; // Exit the function if email is invalid
                }

                // Check email existence before adding user
                ValidateEmail(regEmailText);
            }
        });
    }

    // Function to validate if the email already exists in Firestore
    private void ValidateEmail(String email) {
        db.collection("Accounts")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean emailAvailable = task.getResult().isEmpty();
                            if (emailAvailable) {
                                // Email is available, proceed with registration logic
                                String regnameText = regname.getText().toString().trim();
                                String regPasswordText = regpassword.getText().toString().trim();
                                String regEmailText = regemail.getText().toString().trim();

                                // Call function to register the user
                                registerUser(regnameText, regPasswordText, regEmailText);
                            } else {
                                regemail.setError("Email Already Exists!"); // Set error on EditText
                                Log.d("Register-Page", "Existing Email");
                            }
                        } else {
                            Log.w("Register_Page", "Error checking email existence", task.getException());
                            // Handle potential errors during email existence check
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
                                                    userMap.put("email", email);
                                                    userMap.put("points", 0);

                                                    db.collection("Accounts").document(user.getUid()).set(userMap)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("Register-Page", "User registration successful!");
                                                                    Toast.makeText(registerPage.this, "Account created successfully! Please check your email for verification.", Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(registerPage.this, loginPage.class);
                                                                    startActivity(intent);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w("Register-Page", "Error registering user:", e);
                                                                    Toast.makeText(registerPage.this, "Registration failed! Please try again.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else {
                                                    Log.e("Register-Page", "sendEmailVerification", task.getException());
                                                    Toast.makeText(registerPage.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.w("Register-Page", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(registerPage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
