package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.regex.Pattern;

public class Register_Page extends AppCompatActivity {

    // Declare EditText and Button variables
    EditText regusername, regname, regpassword, regemail;
    Button registerbtn;

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
        regusername = findViewById(R.id.regusername);
        regname = findViewById(R.id.regname);
        regpassword = findViewById(R.id.regpassword);
        regemail = findViewById(R.id.regemail);
        registerbtn = findViewById(R.id.btn_register);

        // Initialize Firebase Firestore instance
        db = FirebaseFirestore.getInstance();

        // Set OnClickListener for the register button
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text entered in EditTexts
                String regUsernameText = regusername.getText().toString().trim();
                String regnameText = regname.getText().toString().trim();
                String regPasswordText = regpassword.getText().toString().trim();
                String regEmailText = regemail.getText().toString().trim();

                // Validate the input fields
                if (regUsernameText.isEmpty() || regnameText.isEmpty() ||
                        regPasswordText.isEmpty() || regEmailText.isEmpty()) {
                    Toast.makeText(Register_Page.this, "Incomplete Form", Toast.LENGTH_SHORT).show();
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
                                String regUsernameText = regusername.getText().toString().trim();
                                String regnameText = regname.getText().toString().trim();
                                String regPasswordText = regpassword.getText().toString().trim();
                                String regEmailText = regemail.getText().toString().trim();

                                // Call function to register the user
                                registerUser(regUsernameText, regnameText, regPasswordText, regEmailText);
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
    private void registerUser(String username, String name, String password, String email) {
        // Create a new user with email and password using Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Create a HashMap to store user data
                                HashMap<Object, Object> userMap = new HashMap<>();
                                userMap.put("username", username);
                                userMap.put("name", name);
                                userMap.put("email", email);
                                userMap.put("points", 0);  // Store points as an integer

                                // Store data in Firebase Firestore using the user's UID as the document ID
                                db.collection("Accounts").document(user.getUid()).set(userMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Register-Page", "User registration successful!");
                                                Intent intent = new Intent(Register_Page.this, Login_Page.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Register-Page", "Error registering user:", e);
                                                Toast.makeText(Register_Page.this, "Registration failed! Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            // If registration fails, display a message to the user
                            Log.w("Register-Page", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Register_Page.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
