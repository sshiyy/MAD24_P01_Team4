package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login_Page extends AppCompatActivity {

    EditText username, password;
    Button login_btn;

    TextView signupRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);


        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);
        login_btn = findViewById(R.id.btn_login);
        signupRedirectText = findViewById(R.id.tv_signup);


        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validatePassword() | !validateUsername() ){

                }else {
                    checkUser();
                }
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login_Page.this, Register_Page.class);
                startActivity(intent);
            }
        });

    }

    public boolean validateUsername(){
        String val = username.getText().toString();
        if (val.isEmpty()){
            username.setError("Username cannot be empty");
            return false;
        }else {
            username.setError(null);
            return true;
        }
    }

    public boolean validatePassword(){
        String valpass = password.getText().toString();
        if (valpass.isEmpty()){
            password.setError("Password cannot be empty");
            return false;
        }else {
            password.setError(null);
            return true;
        }
    }

    public void checkUser(){
        String userUsername = username.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    username.setError(null);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);

                    if (!Objects.equals(passwordFromDB, password)) {
                        username.setError(null);
                        Intent intent = new Intent(Login_Page.this, productpage.class);
                        startActivity(intent);
                    } else {
                        password.setError("Invalid");
                        password.requestFocus();
                    }
                }else {
                    username.setError("User does not exist");
                    username.requestFocus();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

    }
}