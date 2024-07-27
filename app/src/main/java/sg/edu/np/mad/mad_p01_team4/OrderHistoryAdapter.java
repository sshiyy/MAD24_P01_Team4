package sg.edu.np.mad.mad_p01_team4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {
    private List<OrderGroup> orderGroups;

    public OrderHistoryAdapter(List<OrderGroup> orderGroups) {
        this.orderGroups = orderGroups;
    }

    public void updateOrderGroups(List<OrderGroup> newOrderGroups) {
        this.orderGroups = newOrderGroups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customhistorycard, parent, false);
        return new OrderHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryViewHolder holder, int position) {
        OrderGroup orderGroup = orderGroups.get(position);
        holder.bind(orderGroup);
    }

    @Override
    public int getItemCount() {
        return orderGroups.size();
    }

    class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView orderNumberTextView;
        private TextView timeDateDisplayTextView;
        private RecyclerView itemsRecyclerView;
        private OrderItemAdapter orderItemAdapter;
        private TextView tvTotalPrice, tvGST, tvTotalWithGST;

        public OrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumberTextView = itemView.findViewById(R.id.ordernumber);
            timeDateDisplayTextView = itemView.findViewById(R.id.timedatedisplay);
            itemsRecyclerView = itemView.findViewById(R.id.items_recyclerview);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvGST = itemView.findViewById(R.id.tvGST);
            tvTotalWithGST = itemView.findViewById(R.id.tvTotalWithGST);

            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            orderItemAdapter = new OrderItemAdapter(new ArrayList<>());
            itemsRecyclerView.setAdapter(orderItemAdapter);
        }

        public void bind(OrderGroup orderGroup) {
            orderNumberTextView.setText("Order #" + orderGroup.getOrderId());
            orderItemAdapter.updateOrderItems(orderGroup.getOrders());

            if (!orderGroup.getOrders().isEmpty()) {
                Order firstOrder = orderGroup.getOrders().get(0);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore")); // Set to Singapore time
                String dateTime = sdf.format(firstOrder.getTimestamp());
                timeDateDisplayTextView.setText(dateTime);
            }

            // Calculate and display total price, GST, and total with GST
            double totalPrice = calculateTotalPrice(orderGroup.getOrders());
            double gst = totalPrice * 0.07; // Assuming GST is 7%
            double totalWithGST = totalPrice + gst;

            tvTotalPrice.setText("Total Price: $" + String.format(Locale.US, "%.2f", totalPrice));
            tvGST.setText("GST: $" + String.format(Locale.US, "%.2f", gst));
            tvTotalWithGST.setText("Total with GST: $" + String.format(Locale.US, "%.2f", totalWithGST));
        }

        private double calculateTotalPrice(List<Order> orders) {
            double totalPrice = 0;
            for (Order order : orders) {
                totalPrice += order.getPrice();
            }
            return totalPrice;
        }
    }
}
