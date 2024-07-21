package sg.edu.np.mad.mad_p01_team4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class OrderAgainAdapter extends RecyclerView.Adapter<OrderAgainAdapter.OrderItemViewHolder> {

    private List<Order> orderItems;
    private Context context;

    public OrderAgainAdapter(List<Order> orderItems, Context context) {
        this.orderItems = orderItems;
        this.context = context;
    }

    public void updateOrderItems(List<Order> newOrderItems) {
        this.orderItems = newOrderItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_productlist, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        Order order = orderItems.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {

        private TextView itemNameTextView;
        private TextView itemPriceTextView;
        private ImageView itemImageView;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.tvName);
            itemPriceTextView = itemView.findViewById(R.id.tvPrice);
            itemImageView = itemView.findViewById(R.id.ivImage);
        }

        public void bind(Order order) {
            itemNameTextView.setText(order.getFoodName());
            itemPriceTextView.setText("$" + order.getPrice());

            Glide.with(itemView.getContext())
                    .load(order.getImg())
                    .into(itemImageView);
        }
    }
}
