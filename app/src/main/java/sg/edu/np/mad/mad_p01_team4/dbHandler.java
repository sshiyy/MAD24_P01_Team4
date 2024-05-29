// File: DBHandler.java
package sg.edu.np.mad.mad_p01_team4;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class dbHandler extends Application {

    private static final String TAG = "DBHandler";

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }

    public void getFoodItems(firestorecallback<Food> firestoreCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference foodItemsCollection = db.collection("Food_Items");

        foodItemsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<Food> foodList = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Food food = document.toObject(Food.class);
                        foodList.add(food);
                    }
                    firestoreCallback.onCallback(foodList);
                    Log.d(TAG, "getFoodItems: Callback invoked with foodList size: " + foodList.size());
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }
}
