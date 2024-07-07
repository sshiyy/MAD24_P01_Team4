package sg.edu.np.mad.mad_p01_team4;

public class Food {
    private static Food instance;
    private String name;
    private int price;
    private String img;
    private String description;
    private int quantity;
    private String category;
    private int imageResourceId; // Add imageResourceId field

    // No-argument constructor required for Firestore deserialization
    public Food() {
    }

    public Food(String name, int price, String img, String description, int quantity, String category, int imageResourceId) {
        this.name = name;
        this.price = price;
        this.img = img;
        this.description = description;
        this.quantity = 0;
        this.category = category;
        this.imageResourceId = imageResourceId;
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


    public int incrementQuantity() {
        return quantity++;
    }

    public int decreamentQuantity() {
        if (quantity > 0) {
            quantity--;
        }
        return quantity;
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
