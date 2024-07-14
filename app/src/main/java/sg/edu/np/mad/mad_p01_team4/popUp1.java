package sg.edu.np.mad.mad_p01_team4;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class popUp1 {

    public static void showPopup(Context context, String message) {
        // Create the dialog
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup1);
        dialog.setCancelable(true); // Make it dismissible on outside touch

        // Get references to the UI elements
        TextView messageText = dialog.findViewById(R.id.tv_message);
        Button okButton = dialog.findViewById(R.id.btn_ok);

        // Set the message
        messageText.setText(message);

        // Set OnClickListener for the OK button
        okButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }
}
