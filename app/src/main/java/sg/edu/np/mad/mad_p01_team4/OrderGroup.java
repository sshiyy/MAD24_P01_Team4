package sg.edu.np.mad.mad_p01_team4;


import java.util.List;


public class OrderGroup {
    private long timestamp;
    private List<Order> orders;

    public OrderGroup(long timestamp, List<Order> orders) {
        this.timestamp = timestamp;
        this.orders = orders;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Order> getOrders() {
        return orders;
    }
}


