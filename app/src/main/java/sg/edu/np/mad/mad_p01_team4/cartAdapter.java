package sg.edu.np.mad.mad_p01_team4;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class cartAdapter extends RecyclerView.Adapter<cartAdapter.cartViewHolder> {
    private List<Food> cartItems;
    private Context cartcontext;

    public cartAdapter(List<Food> cartItems, Context cartcontext) {
        this.cartItems = cartItems;
        this.cartcontext = cartcontext;
    }

    @NonNull
    @Override
    public cartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(cartcontext).inflate(R.layout.custom_cart_item, parent, false);
        return new cartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull cartViewHolder holder, int position) {
        Food food = cartItems.get(position);
        holder.tvcartName.setText(food.getName());
        int price = (int) food.getPrice();
        holder.tvcartPrice.setText("$" + price);
        holder.ivcartImage.setImageResource(food.getImageResourceId());
        holder.tvcartQuantity.setText("Quantity: " + String.valueOf(food.getQuantity()));
        holder.tvcartitemttlprice.setText("$" + (price*food.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class cartViewHolder extends RecyclerView.ViewHolder {
        private TextView tvcartName, tvcartPrice, tvcartQuantity, tvcartitemttlprice;
        private ImageView ivcartImage;

        public cartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvcartName = itemView.findViewById(R.id.tvcartName);
            tvcartPrice = itemView.findViewById(R.id.tvcartPrice);
            ivcartImage = itemView.findViewById(R.id.ivcartImage);
            tvcartQuantity = itemView.findViewById(R.id.tvcartQuantity);
            tvcartitemttlprice = itemView.findViewById(R.id.tvtotalpriceforitem);
        }
    }






}

