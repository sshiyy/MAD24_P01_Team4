package sg.edu.np.mad.mad_p01_team4;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<OrderGroup> orderGroups;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ongoingFragment fragment; // Add reference to ongoingFragment

    public OrderAdapter(List<OrderGroup> orderGroups, ongoingFragment fragment) {
        this.orderGroups = orderGroups;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.fragment = fragment; // Initialize the reference
    }

    public void updateOrderGroups(List<OrderGroup> newOrderGroups) {
        this.orderGroups = newOrderGroups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customlayoutfororders, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderGroup orderGroup = orderGroups.get(position);
        holder.bind(orderGroup);
    }

    @Override
    public int getItemCount() {
        return orderGroups.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {

        private TextView orderNumberTextView;
        private RecyclerView itemsRecyclerView;
        private OrderItemAdapter orderItemAdapter;
        private RelativeLayout receivedBtn;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumberTextView = itemView.findViewById(R.id.ordernumber);
            itemsRecyclerView = itemView.findViewById(R.id.items_recyclerview);
            receivedBtn = itemView.findViewById(R.id.receivedbtn);

            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            orderItemAdapter = new OrderItemAdapter(new ArrayList<>());
            itemsRecyclerView.setAdapter(orderItemAdapter);

            receivedBtn.setOnClickListener(v -> moveOrderToHistory(getAdapterPosition()));
        }

        public void bind(OrderGroup orderGroup) {
            orderNumberTextView.setText("Order #" + orderGroup.getOrderId()); // Customize order number display as needed
            orderItemAdapter.updateOrderItems(orderGroup.getOrders());
        }

        private void moveOrderToHistory(int position) {
            OrderGroup orderGroup = orderGroups.get(position);
            List<Order> orders = orderGroup.getOrders();

            for (Order order : orders) {
                db.collection("order_history")
                        .add(order)
                        .addOnSuccessListener(documentReference -> {
                            db.collection("ongoing_orders")
                                    .document(order.getDocumentId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Order moved to order_history and deleted from ongoing_orders");
                                        // Notify the fragment to reload orders
                                        fragment.loadOrders();
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete order from ongoing_orders", e));
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to add order to order_history", e));
            }

            // Remove the order group from the list and notify the adapter
            orderGroups.remove(position);
            notifyItemRemoved(position);
        }
    }
}
