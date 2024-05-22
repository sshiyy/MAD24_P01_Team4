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


public class Register_Page extends AppCompatActivity {

    EditText regusername, regpassword, regemail;
    Button registerbtn;

    FirebaseDatabase database;
    DatabaseReference reference;

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

                Firebase firebase = new Firebase(regEmailText, regUsernameText, regPasswordText);


                reference.child(regUsernameText).setValue(firebase);

                Toast.makeText(Register_Page.this, "Register Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register_Page.this, Login_Page.class);
                startActivity(intent);



            }
        });

// For redirecting to login page after registering
//        registerbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Register_Page.this, Login_Page.class);
//                startActivity(intent);
//            }
//        });


    }

}