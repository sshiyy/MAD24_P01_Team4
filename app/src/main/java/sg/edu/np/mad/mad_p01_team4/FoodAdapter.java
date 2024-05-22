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
import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private ArrayList<Food> foodList;
    private Context context;

    public FoodAdapter(ArrayList<Food> foodList, Context context) {
        this.foodList = foodList;
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
        Food food = foodList.get(position);
        holder.tvName.setText(food.getName());
        int price = (int) food.getPrice();
        holder.tvPrice.setText("$" + price);
        holder.ivImage.setImageResource(food.getImageResourceId());

        holder.ivImage.setOnClickListener(v -> showFoodDetailDialog(food));

        holder.btnIncrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(holder.tvQuantity.getText().toString());
            quantity++;
            holder.tvQuantity.setText(String.valueOf(quantity));
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
        return foodList.size();
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

    private void showFoodDetailDialog(Food food){

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_food_detail, null);

        ImageView ivFoodImage = dialogView.findViewById(R.id.ivFoodImage);
        TextView tvFoodDescription = dialogView.findViewById(R.id.tvFoodDescription);
        ImageButton btnFavorite = dialogView.findViewById(R.id.toggleFavorite);

        ivFoodImage.setImageResource(food.getImageResourceId());
        tvFoodDescription.setText(food.getDescription());

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            private boolean isFavorite = false;
            @Override
            public void onClick(View v) {
                isFavorite = !isFavorite;
                btnFavorite.setImageResource(isFavorite ? R.drawable.redhearticon : R.drawable.hearticon);
            }
        });

        new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
}
