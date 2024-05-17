package sg.edu.np.mad.mad_p01_team4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<food_item> foodList;
    private Context context;

    public FoodAdapter(Context context, List<food_item> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        food_item foodItem = foodList.get(position);
        holder.foodName.setText(foodItem.getName());
        holder.foodPrice.setText("$" + foodItem.getPrice());
        holder.foodImage.setImageResource(foodItem.getImageResourceId());
        holder.tvQuantity.setText(String.valueOf(foodItem.getQuantity()));

        holder.btnDecrease.setOnClickListener(v -> {
            int quantity = foodItem.getQuantity();
            if (quantity > 0) {
                foodItem.setQuantity(quantity - 1);
                holder.tvQuantity.setText(String.valueOf(foodItem.getQuantity()));
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            int quantity = foodItem.getQuantity();
            foodItem.setQuantity(quantity + 1);
            holder.tvQuantity.setText(String.valueOf(foodItem.getQuantity()));
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView foodName, foodPrice, tvQuantity;
        ImageButton foodImage, btnDecrease, btnIncrease;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            foodImage = itemView.findViewById(R.id.foodImage);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
        }
    }
}
