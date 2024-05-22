package sg.edu.np.mad.mad_p01_team4;

public class Food {
    public String name;
    public int price;
    public int img;

    private String description;
    private int quantity;

    public Food(String name, int price, int img, String description){
        this.name = name;
        this.price = price;
        this.img = img;
        this.description = description;
        this.quantity = 1;
    }
    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResourceId() {
        return img;
    }

    public String getDescription(){
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void incrementQuantity() {
        this.quantity++;
    }
}
