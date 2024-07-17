package sg.edu.np.mad.mad_p01_team4;

import java.util.List;
import java.util.Map;

public class Food {
    private static Food instance;
    private String name;
    private int price;
    private String img;
    private String description;
    private int quantity;
    private String category;
    private int imageResourceId; // Add imageResourceId field
    private List<Map<String,Object>> modifications;
    private String specialrequest;

    // No-argument constructor required for Firestore deserialization
    public Food() {
    }

    public Food(String name, int price, String img, String description, int quantity, String category, int imageResourceId, List<Map<String, Object>>modifications) {
        this.name = name;
        this.price = price;
        this.img = img;
        this.description = description;
        this.quantity = 0;
        this.category = category;
        this.imageResourceId = imageResourceId;
        this.modifications = modifications;
    }

    // Getters and setters for all fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;

    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public List<Map<String,Object>>getModifications()
    {
        return modifications;
    }

    public void setModifications(List<Map<String,Object>>modifications)
    {
        this.modifications = modifications;
    }

    public String getSpecialrequest() {
        return specialrequest;
    }

    public void setSpecialrequest(String specialrequest) {
        this.specialrequest = specialrequest;
    }

    public int getQuantity() {
        return quantity;
    }
    public static Food getInstance(){
        if (instance == null) {
            instance = new Food();
        }
        return instance;
    }
}
