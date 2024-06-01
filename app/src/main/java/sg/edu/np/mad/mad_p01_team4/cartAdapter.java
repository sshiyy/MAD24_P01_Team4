package sg.edu.np.mad.mad_p01_team4;

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

import java.util.List;

public class cartAdapter extends RecyclerView.Adapter<cartAdapter.cartViewHolder> {
    private List<Food> cartItems;
    private Context cartcontext;

    public cartAdapter(List<Food> cartItems, Context cartcontext) {
        this.cartItems = cartItems;
        this.cartcontext = cartcontext;
    }

    // inflates custom layout for each cart item
    @NonNull
    @Override
    public cartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(cartcontext).inflate(R.layout.custom_cart_item, parent, false);
        return new cartViewHolder(view);
    }

    // binds data to cart item view
    @Override
    public void onBindViewHolder(@NonNull cartViewHolder holder, int position) {
        Food food = cartItems.get(position);
        holder.tvcartName.setText(food.getName());
        int price = (int) food.getPrice();
        holder.tvcartPrice.setText("$" + price);
        holder.tvcartQuantity.setText("Quantity: " + String.valueOf(food.getQuantity()));
        holder.tvQuantity.setText(String.valueOf(food.getQuantity()));

        // button to increase quantity
        holder.btnIncrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(holder.tvQuantity.getText().toString());
            quantity++;
            holder.tvQuantity.setText(String.valueOf(quantity));
            food.setQuantity(quantity);
            cart.getInstance().updateCart(food);
            ((cartpage) cartcontext).updateCartSummary();
            updatePriceQuantity(holder, food);
        });

        // button to decrease quantity
        holder.btnDecrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(holder.tvQuantity.getText().toString());
            if (quantity > 0) {
                quantity--;
                holder.tvQuantity.setText(String.valueOf(quantity));
                food.setQuantity(quantity);
                cart.getInstance().updateCart(food);
                ((cartpage) cartcontext).updateCartSummary();
                updatePriceQuantity(holder, food);
            }
        });

        // Load image using Glide
        Glide.with(cartcontext)
                .load(food.getImg()) // Assuming getImage() returns a string URL or path
                .into(holder.ivcartImage);

        // calculate and display total price for item
        int totalItemPrice = food.getPrice() * food.getQuantity();
        holder.tvcartitemttlprice.setText("$" + totalItemPrice);
    }

    // total number of items in teh cart
    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    // update price & quantity in the cart page
    // holder -> instance of cartViewHolder that holds the views for a single cart item
    // food -> an instance of the food class representing the food item in the cart
    private void updatePriceQuantity(cartViewHolder holder, Food food) {
        // retrives the current quantity of the food which displays the quantity as text
        // converts text to an int & assign to var quantity
        int quantity = Integer.parseInt(holder.tvQuantity.getText().toString());
        // calculate total price of the food
        // assigns the result to var totalPrice
        double totalPrice = quantity * food.getPrice();

        // updates tcartQuality & tvcartitemttlprice to display new quantity and price
        holder.tvcartQuantity.setText("Quantity: " + String.valueOf(quantity));
        holder.tvcartitemttlprice.setText(String.format("$%.0f", totalPrice));
    }

    // to hold view of each cart item
    public static class cartViewHolder extends RecyclerView.ViewHolder {
        private TextView tvcartName, tvcartPrice, tvcartQuantity, tvcartitemttlprice, tvQuantity;
        private ImageView ivcartImage;
        private ImageButton btnIncrease, btnDecrease;

        public cartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvcartName = itemView.findViewById(R.id.tvcartName);
            tvcartPrice = itemView.findViewById(R.id.tvcartPrice);
            ivcartImage = itemView.findViewById(R.id.ivcartImage);
            tvcartQuantity = itemView.findViewById(R.id.tvcartQuantity);
            tvcartitemttlprice = itemView.findViewById(R.id.tvtotalpriceforitem);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }

}
