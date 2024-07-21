package sg.edu.np.mad.mad_p01_team4;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderAgainAdapter extends RecyclerView.Adapter<OrderAgainAdapter.OrderItemViewHolder> {

    private List<Order> orderItems;
    private Context context;
    private FirebaseFirestore db;

    public OrderAgainAdapter(List<Order> orderItems, Context context) {
        this.orderItems = orderItems;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
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

    class OrderItemViewHolder extends RecyclerView.ViewHolder {

        private TextView itemNameTextView;
        private TextView itemPriceTextView;
        private ImageView itemImageView;
        private ImageButton favButton;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.tvName);
            itemPriceTextView = itemView.findViewById(R.id.tvPrice);
            itemImageView = itemView.findViewById(R.id.ivImage);
            favButton = itemView.findViewById(R.id.favBtn);
        }

        public void bind(Order order) {
            itemNameTextView.setText(order.getFoodName());
            itemPriceTextView.setText("$" + order.getPrice());

            // Load the image using Glide
            Glide.with(itemView.getContext())
                    .load(order.getImg())
                    .into(itemImageView);

            // Set favorite button status
            if (order.isFavorite()) {
                favButton.setImageResource(R.drawable.redfavbtn); // Assuming you have this drawable for filled heart
            } else {
                favButton.setImageResource(R.drawable.favbtn); // Assuming you have this drawable for empty heart
            }

            // Handle favorite button click
            favButton.setOnClickListener(v -> {
                if (order.isFavorite()) {
                    removeFromFavorites(order);
                } else {
                    addToFavorites(order);
                }
            });
        }

        private void addToFavorites(Order order) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                return;
            }

            String userId = currentUser.getUid();
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("userId", userId);
            favoriteData.put("foodName", order.getFoodName());
            // Add other order details if needed

            db.collection("favorites")
                    .add(favoriteData)
                    .addOnSuccessListener(documentReference -> {
                        order.setFavorite(true);
                        favButton.setImageResource(R.drawable.redfavbtn);
                    })
                    .addOnFailureListener(e -> Log.e("OrderAgainAdapter", "Error adding to favorites", e));
        }

        private void removeFromFavorites(Order order) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                return;
            }

            String userId = currentUser.getUid();
            db.collection("favorites")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("foodName", order.getFoodName())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            db.collection("favorites").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        order.setFavorite(false);
                                        favButton.setImageResource(R.drawable.favbtn);
                                    })
                                    .addOnFailureListener(e -> Log.e("OrderAgainAdapter", "Error removing from favorites", e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("OrderAgainAdapter", "Error querying favorites", e));
        }
    }
}
