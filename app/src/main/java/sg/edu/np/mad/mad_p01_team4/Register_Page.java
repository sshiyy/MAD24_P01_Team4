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

public class Register_Page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Getting References
        EditText regusername = findViewById(R.id.et_regusername);
        EditText regpassword = findViewById(R.id.et_regpassword);
        EditText regemail = findViewById(R.id.et_regemail);
        Button registerbtn = findViewById(R.id.btn_register);
        //

        //Register Button Functions
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the text entered in EditTexts
                String regUsernameText = regusername.getText().toString().trim();
                String regPasswordText = regpassword.getText().toString().trim();
                String regEmailText = regemail.getText().toString().trim();

                // Transfer to Log In Page
                Intent intent = new Intent(v.getContext(), ProductPage.class);
                v.getContext().startActivity(intent);
            }
        });


    }

}