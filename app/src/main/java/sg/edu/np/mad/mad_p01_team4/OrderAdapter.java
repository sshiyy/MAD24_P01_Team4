package sg.edu.np.mad.mad_p01_team4;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<OrderGroup> orderGroups;

    public OrderAdapter(List<OrderGroup> orderGroups) {
        this.orderGroups = orderGroups;
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

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        private TextView orderNumberTextView;
        private RecyclerView itemsRecyclerView;
        private OrderItemAdapter orderItemAdapter;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumberTextView = itemView.findViewById(R.id.ordernumber);
            itemsRecyclerView = itemView.findViewById(R.id.items_recyclerview);

            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            orderItemAdapter = new OrderItemAdapter(new ArrayList<>());
            itemsRecyclerView.setAdapter(orderItemAdapter);
        }

        public void bind(OrderGroup orderGroup) {
            orderNumberTextView.setText("Order #" + orderGroup.getTimestamp()); // Customize order number display as needed
            orderItemAdapter.updateOrderItems(orderGroup.getOrders());
        }
    }
}
