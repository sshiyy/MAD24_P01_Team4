package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class loginPage extends AppCompatActivity {

    // Declare FireBase & Buttons
    private FirebaseAuth mAuth;
    private EditText email, password;
    private Button loginBtn;
    private TextView signupRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Start FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Get References
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginBtn = findViewById(R.id.btn_Login);
        signupRedirectText = findViewById(R.id.tv_signup);

        // Set OnClickListener for Login Button
        loginBtn.setOnClickListener(v -> {
            if (validateInput()) {
                String userEmail = email.getText().toString().trim();
                String userPassword = password.getText().toString().trim();
                loginUser(userEmail, userPassword);
            }
        });

        // Set OnClickListener to redirect Register Page
        signupRedirectText.setOnClickListener(v -> startActivity(new Intent(this, registerPage.class)));

        TextView forgotPasswordText = findViewById(R.id.tv_forgot_password);
        forgotPasswordText.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            if (userEmail.isEmpty()) {
                popUp1.showPopup(loginPage.this, "Enter your email to reset your password");
            } else {
                mAuth.sendPasswordResetEmail(userEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                popUp1.showPopup(loginPage.this, "Email has been sent");
                            } else {
                                popUp1.showPopup(loginPage.this, "This email isn't connected to any accounts");
                            }
                        });
            }
        });
    }

    // Function to validate user input
    private boolean validateInput() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        if (emailText.isEmpty()) {
            Log.d("Login_Page", "Email is empty");
            errorMessage.append("Email cannot be empty\n");
            isValid = false;
        } else if (!isValidEmail(emailText)) {
            Log.d("Login_Page", "Email is invalid");
            errorMessage.append("Email is invalid\n");
            isValid = false;
        }

        if (passwordText.isEmpty()) {
            errorMessage.append("Password cannot be empty\n");
            isValid = false;
        }

        if (!isValid) {
            popUp1.showPopup(this, errorMessage.toString().trim());
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
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    Log.d("Login_Page", "signInWithEmail:success");
                                    Intent intent = new Intent(loginPage.this, productpage.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    mAuth.signOut();  // Sign out the user
                                    popUp1.showPopup(loginPage.this, "Please verify your email before logging in.");
                                }
                            }
                        } else {
                            Log.d("Login_Page", "signInWithEmail:failure", task.getException());
                            popUp1.showPopup(loginPage.this, "Authentication failed.");
                        }
                    }
                });
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[\\w!#$%&'*+/=?^`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?(\\.[a-zA-Z]{2,})?$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}
