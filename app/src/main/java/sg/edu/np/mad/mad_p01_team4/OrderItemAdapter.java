package sg.edu.np.mad.mad_p01_team4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<Order> orderItems;

    public OrderItemAdapter(List<Order> orderItems) {
        this.orderItems = orderItems;
    }

    public void updateOrderItems(List<Order> newOrderItems) {
        this.orderItems = newOrderItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_ongoingorder, parent, false);
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
        private TextView tvModifications;
        private TextView tvSpecialRequest;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.tvcartName);
            itemPriceTextView = itemView.findViewById(R.id.tvcartPrice);
            itemImageView = itemView.findViewById(R.id.ivcartImage);
            tvModifications = itemView.findViewById(R.id.tvModifications);
            tvSpecialRequest = itemView.findViewById(R.id.tvspecialrequest);
        }

        public void bind(Order order) {
            itemNameTextView.setText(order.getFoodName());
            itemPriceTextView.setText("$" + order.getPrice());

            // Load the image using Glide
            Glide.with(itemView.getContext())
                    .load(order.getImg()) // Load the image from the URL
                    .into(itemImageView); // Set into ImageView

            // Display modifications
            List<Map<String, Object>> modifications = order.getModifications();
            if (modifications != null && !modifications.isEmpty()) {
                StringBuilder modificationsText = new StringBuilder();
                for (Map<String, Object> modification : modifications) {
                    for (Map.Entry<String, Object> entry : modification.entrySet()) {
                        if ((Boolean) entry.getValue()) {
                            modificationsText.append(entry.getKey()).append(", ");
                        }
                    }
                }
                // Remove trailing comma and space
                if (modificationsText.length() > 0) {
                    modificationsText.setLength(modificationsText.length() - 2);
                }
                tvModifications.setText(modificationsText.toString());
            } else {
                tvModifications.setText("None");
            }

            // Display special request
            tvSpecialRequest.setText(order.getSpecialRequest().isEmpty() ? "None" : order.getSpecialRequest());
        }
    }
}
