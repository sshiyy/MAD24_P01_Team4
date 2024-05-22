package sg.edu.np.mad.mad_p01_team4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;


public class Register_Page extends AppCompatActivity {

    EditText regusername, regpassword, regemail;
    Button registerbtn;

    FirebaseDatabase database;
    DatabaseReference reference;

    // Function to check for valid email format
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[\\w!#$%&'*+/=?^`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);


        // Getting References
        regusername = findViewById(R.id.et_regusername);
        regpassword = findViewById(R.id.et_regpassword);
        regemail = findViewById(R.id.et_regemail);
        registerbtn = findViewById(R.id.btn_register);
        //

        //Register Button Functions
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                // Get the text entered in EditTexts
                String regUsernameText = regusername.getText().toString().trim();
                String regPasswordText = regpassword.getText().toString().trim();
                String regEmailText = regemail.getText().toString().trim();

                // Email Validation with Regular Expression
                if (!isValidEmail(regEmailText)) {
                    regemail.setError("Invalid Email format!"); // Set error on EditText
                    return; // Exit the function if email is invalid
                }

                // Create a HashMap to store user data
                HashMap<String, String> userMap = new HashMap<>();
                userMap.put("username", regUsernameText);
                userMap.put("password", regPasswordText);
                userMap.put("email", regEmailText);

                // Store data in Firebase using username as the key
                reference.child(regUsernameText).setValue(userMap);

                Toast.makeText(Register_Page.this, "Register Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register_Page.this, Login_Page.class);
                startActivity(intent);

            }
        });


    }

}