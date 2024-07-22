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
import java.util.Map;

public class cartAdapter extends RecyclerView.Adapter<cartAdapter.cartViewHolder> {
    private List<Order> cartItems;
    private Context cartcontext;

    public cartAdapter(List<Order> cartItems, Context cartcontext) {
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
        Order order = cartItems.get(position);
        holder.tvcartName.setText(order.getFoodName());
        holder.tvcartPrice.setText("$" + order.getPrice());

        // display the special request
        String specialRequest = order.getSpecialRequest();
        if (specialRequest == null || specialRequest.trim().isEmpty()) {
            holder.tvspecialrequest.setText("None");
        } else {
            holder.tvspecialrequest.setText(specialRequest);
        }

        // Display the modifications
        List<Map<String, Object>> modifications = order.getModifications();
        if (modifications != null && !modifications.isEmpty()) {
            StringBuilder modificationsText = new StringBuilder();
            for (Map<String, Object> modification : modifications) {
                String name = (String) modification.keySet().toArray()[0];
                Boolean value = (Boolean) modification.values().toArray()[0];
                if (value) {
                    modificationsText.append(name).append("\n");
                }
            }
            // Remove the trailing newline
            if (modificationsText.length() > 0) {
                modificationsText.setLength(modificationsText.length() - 1);
            } else {
                modificationsText.append("None");
            }
            holder.tvmodifications.setText(modificationsText.toString());
        } else {
            holder.tvmodifications.setText("None");
        }

        Glide.with(cartcontext)
                .load(order.getImg()) // Assuming getImg() returns a string URL or path
                .into(holder.ivcartImage);
    }



    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class cartViewHolder extends RecyclerView.ViewHolder {
        private TextView tvcartName, tvcartPrice,tvspecialrequest,tvmodifications;
        private ImageView ivcartImage;

        public cartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvcartName = itemView.findViewById(R.id.tvcartName);
            tvcartPrice = itemView.findViewById(R.id.tvcartPrice);
            ivcartImage = itemView.findViewById(R.id.ivcartImage);
            tvspecialrequest =itemView.findViewById(R.id.tvspecialrequest);
            tvmodifications = itemView.findViewById(R.id.tvModifications);
        }
    }
}
