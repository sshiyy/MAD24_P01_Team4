package sg.edu.np.mad.mad_p01_team4;

public class Food {
    public String name;
    public int price;
    public int img;

    public Food(String name, int price, int img){
        this.name = name;
        this.price = price;
        this.img = img;
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
}
