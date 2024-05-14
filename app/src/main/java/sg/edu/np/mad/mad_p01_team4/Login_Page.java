package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login_Page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button login_btn = findViewById(R.id.btn_login);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get references to EditTexts
                EditText username = findViewById(R.id.et_username);
                EditText password = findViewById(R.id.et_password);

                // Get the text entered in EditTexts
                String usernameText = username.getText().toString().trim();
                String passwordText = password.getText().toString().trim();

                // Check if username or password is empty
                if (usernameText.isEmpty() || passwordText.isEmpty()) {
                    // Show a Toast message (or any other popup)
                    Toast.makeText(v.getContext(), "Username or Password cannot be empty!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Username and password are not empty, proceed with login logic (optional)
                    // ... (e.g., validate credentials with server)

                    // Start Product Page
                    Intent intent = new Intent(v.getContext(), ProductPage.class);
                    v.getContext().startActivity(intent);
                }
            }
        });


    }
}