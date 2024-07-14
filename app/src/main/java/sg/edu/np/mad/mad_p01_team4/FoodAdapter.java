package sg.edu.np.mad.mad_p01_team4;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private ArrayList<Food> foodList;
    private ArrayList<Food> filteredFoodList;
    private Context context;

    public FoodAdapter(ArrayList<Food> foodList, Context context) {
        this.foodList = foodList;
        this.filteredFoodList = new ArrayList<>(foodList);
        this.context = context;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for each item in the recyclerview
        View view = LayoutInflater.from(context).inflate(R.layout.custom_productlist, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        // Get the current food item from the filtered list
        Food food = filteredFoodList.get(position);
        // Set the name and price of the food item
        holder.tvName.setText(food.getName());
        int price = (int) food.getPrice();
        holder.tvPrice.setText("$" + price);

        // Load image using Glide
        Glide.with(context)
                .load(food.getImg()) // Assuming img is a URL or path to the image
                .into(holder.ivImage);

        // Click listener on image to show the detailed view of the food
        holder.ivImage.setOnClickListener(v -> showFoodDetailDialog(food));

        // Check if the food item is already in favorites
        checkIfFavorite(food, holder.favBtn);

        holder.favBtn.setOnClickListener(v -> {
            toggleFavorite(food, holder.favBtn);
        });
    }

    @Override
    public int getItemCount() {
        return filteredFoodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        public ImageButton favBtn;
        private TextView tvName, tvPrice;
        private ImageView ivImage;

        // Viewholder to hold the views for each food
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivImage = itemView.findViewById(R.id.ivImage);
            favBtn = itemView.findViewById(R.id.favBtn);
        }
    }

    // Method to show a dialog with detailed information about the food item
    private void showFoodDetailDialog(Food food) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Redirect to login/signup activity
            Intent intent = new Intent(context, loginPage.class);
            context.startActivity(intent);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_food_detail, null);

        ImageView ivFoodImage = dialogView.findViewById(R.id.foodImage);
        TextView tvFoodDescription = dialogView.findViewById(R.id.descriptionTxt);
        LinearLayout modificationsLayout = dialogView.findViewById(R.id.modificationsLayout);
        EditText specialRequestInput = dialogView.findViewById(R.id.specialRequestInput);

        // Load image using Glide
        Glide.with(context)
                .load(food.getImg()) // Assuming img is a URL or path to the image
                .into(ivFoodImage);

        // set food description
        tvFoodDescription.setText(food.getDescription());

        // add checkboxes for modifications
        List<Map<String, Object>> modifications = food.getModifications();
        List<CheckBox> checkBoxes = new ArrayList<>();
        if (modifications != null) {
            for (Map<String, Object> modification : modifications) {
                String name = (String) modification.keySet().toArray()[0];
                Boolean value = (Boolean) modification.values().toArray()[0];

                CheckBox checkBox = new CheckBox(context);
                checkBox.setText(name);
                checkBox.setChecked(value);
                checkBoxes.add(checkBox);
                modificationsLayout.addView(checkBox);
            }
        }

        // create and show alertdialog
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();
        alertDialog.show();

        // Add to cart button
        Button addToCartButton = dialogView.findViewById(R.id.addToCartButton);
        addToCartButton.setOnClickListener(v -> {
            String userId = currentUser.getUid();
            String foodName = food.getName();
            int price = food.getPrice();
            List<Map<String, Object>> selectedModifications = new ArrayList<>();
            for (int i = 0; i < modificationsLayout.getChildCount(); i++) {
                View child = modificationsLayout.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) child;
                    selectedModifications.add(Collections.singletonMap(checkBox.getText().toString(), checkBox.isChecked()));
                }
            }
            String specialRequest = specialRequestInput.getText().toString();

            Order order = new Order(userId, foodName, price, selectedModifications, specialRequest);
            addOrderToFirebase(order);

            alertDialog.dismiss(); // Close the dialog
        });

        // cross button to close the dialog
        ImageButton crossbutton = dialogView.findViewById(R.id.dialogcross);
        crossbutton.setOnClickListener(v -> {
            alertDialog.dismiss(); // close the dialog
        });
    }

    private void addOrderToFirebase(Order order) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the image URL from Food_Items collection
        db.collection("Food_Items")
                .whereEqualTo("name", order.getFoodName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String imgUrl = document.getString("img");
                        order.setImg(imgUrl); // Set the image URL in the order

                        // Add the order to the currently_ordering collection
                        db.collection("currently_ordering")
                                .add(order)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(context, "Order added to cart successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to add order to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to fetch food image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to update the data list of the adapter with filtered items
    public void updateList(ArrayList<Food> newList) {
        filteredFoodList.clear();
        filteredFoodList.addAll(newList);
        notifyDataSetChanged();
    }

    // Method to update the filtered list based on search query
    public void filter(String query) {
        filteredFoodList.clear();
        if (query.isEmpty()) {
            filteredFoodList.addAll(foodList); // Show all items if query is empty
        } else {
            query = query.toLowerCase().trim();
            for (Food food : foodList) {
                if (food.getName().toLowerCase().contains(query)) {
                    filteredFoodList.add(food);
                }
            }
        }
        notifyDataSetChanged(); // Notify adapter of data change
    }

    private void checkIfFavorite(Food food, ImageButton favBtn) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("favorites")
                    .whereEqualTo("userId", currentUser.getUid())
                    .whereEqualTo("foodName", food.getName())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            favBtn.setImageResource(R.drawable.redfavbtn);
                        } else {
                            favBtn.setImageResource(R.drawable.favbtn);
                        }
                    });
        }
    }

    private void toggleFavorite(Food food, ImageButton favBtn) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(context, loginPage.class);
            context.startActivity(intent);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("favorites")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("foodName", food.getName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                        favBtn.setImageResource(R.drawable.favbtn);
                    } else {
                        Map<String, Object> favorite = new HashMap<>();
                        favorite.put("userId", currentUser.getUid());
                        favorite.put("foodName", food.getName());
                        favorite.put("price", food.getPrice());
                        favorite.put("img", food.getImg());
                        favorite.put("description", food.getDescription());

                        db.collection("favorites")
                                .add(favorite)
                                .addOnSuccessListener(documentReference -> {
                                    favBtn.setImageResource(R.drawable.redfavbtn);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to add to favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
