package sg.edu.np.mad.mad_p01_team4;

import java.util.List;
import java.util.Map;

public class Order {
    private String userId;
    private String foodName;
    private int price;
    private List<Map<String, Object>> modifications;
    private String specialRequest;
    private long timestamp;
    private String img;
    private String documentId;
    private String orderId;
    private Boolean isFavorite = false;

    // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    public Order() {}

    public Order(String userId, String foodName, int price, List<Map<String, Object>> modifications, String specialRequest) {
        this.userId = userId;
        this.foodName = foodName;
        this.price = price;
        this.modifications = modifications;
        this.specialRequest = specialRequest;
        this.timestamp = System.currentTimeMillis();
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

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp=timestamp;
    }

    public String getImg(){
        return img;
    }

    public void setImg(String img){
        this.img = img;
    }
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
