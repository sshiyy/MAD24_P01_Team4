package sg.edu.np.mad.mad_p01_team4;

import java.util.List;
import java.util.Map;

public class Order {
    private String userId;
    private String foodName;
    private int price;
    private List<Map<String, Object>> modifications;
    private String specialRequest;

    // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    public Order() {}

    public Order(String userId, String foodName, int price, List<Map<String, Object>> modifications, String specialRequest) {
        this.userId = userId;
        this.foodName = foodName;
        this.price = price;
        this.modifications = modifications;
        this.specialRequest = specialRequest;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<Map<String, Object>> getModifications() {
        return modifications;
    }

    public void setModifications(List<Map<String, Object>> modifications) {
        this.modifications = modifications;
    }

    public String getSpecialRequest() {
        return specialRequest;
    }

    public void setSpecialRequest(String specialRequest) {
        this.specialRequest = specialRequest;
    }
}
