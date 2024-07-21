package sg.edu.np.mad.mad_p01_team4;

import java.util.List;

public class OrderGroup {
    private String orderId; // Use String for orderId
    private List<Order> orders;

    public OrderGroup(String orderId, List<Order> orders) {
        this.orderId = orderId;
        this.orders = orders;
    }

    public String getOrderId() {
        return orderId;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
