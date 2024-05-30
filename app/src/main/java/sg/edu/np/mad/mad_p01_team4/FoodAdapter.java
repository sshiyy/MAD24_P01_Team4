package sg.edu.np.mad.mad_p01_team4;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

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
        View view = LayoutInflater.from(context).inflate(R.layout.custom_productlist, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = filteredFoodList.get(position);
        holder.tvName.setText(food.getName());
        int price = (int) food.getPrice();
        holder.tvPrice.setText("$" + price);

        // Load image using Glide
        Glide.with(context)
                .load(food.getImg()) // Assuming img is a URL or path to the image
                .into(holder.ivImage);

        holder.ivImage.setOnClickListener(v -> showFoodDetailDialog(food));

        holder.btnIncrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(holder.tvQuantity.getText().toString());
            quantity++;
            holder.tvQuantity.setText(String.valueOf(quantity));
            cart.getInstance().additems(food);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(holder.tvQuantity.getText().toString());
            if (quantity > 0) {
                quantity--;
                holder.tvQuantity.setText(String.valueOf(quantity));
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredFoodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvPrice, tvQuantity;
        private ImageView ivImage;
        private ImageButton btnIncrease, btnDecrease;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }

    private void showFoodDetailDialog(Food food) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_food_detail, null);

        ImageView ivFoodImage = dialogView.findViewById(R.id.ivFoodImage);
        TextView tvFoodDescription = dialogView.findViewById(R.id.tvFoodDescription);
        ImageButton btnFavorite = dialogView.findViewById(R.id.toggleFavorite);

        // Load image using Glide
        Glide.with(context)
                .load(food.getImg()) // Assuming img is a URL or path to the image
                .into(ivFoodImage);

        tvFoodDescription.setText(food.getDescription());

        // Use an array to encapsulate the isFavorite variable
        final boolean[] isFavorite = {FavoritesManager.isFavorite(food)};
        btnFavorite.setImageResource(isFavorite[0] ? R.drawable.redhearticon : R.drawable.hearticon);

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavorite[0]) {
                    FavoritesManager.removeFromFavorites(food);
                    btnFavorite.setImageResource(R.drawable.hearticon);
                } else {
                    FavoritesManager.addToFavorites(food);
                    btnFavorite.setImageResource(R.drawable.redhearticon);
                }
                isFavorite[0] = !isFavorite[0];  // Toggle favorite status
            }
        });
        new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    // Method to update the data list of the adapter with filtered items
    public void updateList(ArrayList<Food> newList) {
        filteredFoodList.clear();
        filteredFoodList.addAll(newList);
        notifyDataSetChanged();
    }
}
