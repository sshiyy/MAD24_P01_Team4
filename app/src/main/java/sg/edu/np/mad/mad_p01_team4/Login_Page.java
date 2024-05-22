package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login_Page extends AppCompatActivity {

    private EditText username, password;
    private Button loginBtn;
    private TextView signupRedirectText;

    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);
        loginBtn = findViewById(R.id.btn_login);
        signupRedirectText = findViewById(R.id.tv_signup);

        reference = FirebaseDatabase.getInstance().getReference("users");

        // Combine login button click and validation
        loginBtn.setOnClickListener(v -> {
            if (validateInput()) {
                checkUser();
            }
        });

        signupRedirectText.setOnClickListener(v -> startActivity(new Intent(this, Register_Page.class)));
    }

    private boolean validateInput() {
        boolean isValid = true;

        if (username.getText().toString().trim().isEmpty()) {
            username.setError("Username cannot be empty");
            isValid = false;
        } else {
            username.setError(null);
        }

        if (password.getText().toString().trim().isEmpty()) {
            password.setError("Password cannot be empty");
            isValid = false;
        } else {
            password.setError(null);
        }

        return isValid;
    }

    private void checkUser() {
        String userUsername = username.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);

                    if (Objects.equals(passwordFromDB, userPassword)) {
                        // Login successful
                        Toast.makeText(Login_Page.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login_Page.this, productpage.class);
                        startActivity(intent);
                    } else {
                        password.setError("Invalid password");
                        password.requestFocus();
                    }
                } else {
                    username.setError("User does not exist");
                    username.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login_Page.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
