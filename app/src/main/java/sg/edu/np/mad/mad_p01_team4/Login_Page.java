package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.regex.Pattern;

import javax.xml.validation.Validator;

public class Login_Page extends AppCompatActivity {

    // Declare FirebaseAuth instance
    private FirebaseAuth mAuth;
    private EditText email, password;
    private Button loginBtn;
    private TextView signupRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Get references to the UI elements
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginBtn = findViewById(R.id.btn_login);
        signupRedirectText = findViewById(R.id.tv_signup);

        // Set OnClickListener for the login button
        loginBtn.setOnClickListener(v -> {
            if (validateInput()) {
                String userEmail = email.getText().toString().trim();
                String userPassword = password.getText().toString().trim();
                loginUser(userEmail, userPassword);
            }
        });

        // Set OnClickListener for the signup redirect text
        signupRedirectText.setOnClickListener(v -> startActivity(new Intent(this, Register_Page.class)));
    }

    // Function to validate user input
    private boolean validateInput() {
        boolean isValid = true;

        if (isValidEmail(email.getText().toString().trim())) {
            email.setError("Email is invalid");
            isValid = false;
        } else {
            email.setError(null);
        }

        if (password.getText().toString().trim().isEmpty()) {
            password.setError("Password cannot be empty");
            isValid = false;
        } else {
            password.setError(null);
        }

        return isValid;
    }

    // Function to log in the user using Firebase Authentication
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d("Login_Page", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Proceed to the next activity
                                Intent intent = new Intent(Login_Page.this, ProfilePage.class); // Replace Home_Page.class with your target activity
                                startActivity(intent);
                            }
                        } else {
                            // If sign in fails, display a message to the user
                            Log.d("Login_Page", "signInWithEmail:failure", task.getException());
                            Toast.makeText(Login_Page.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[\\w!#$%&'*+/=?^`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}
