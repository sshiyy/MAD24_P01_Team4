package sg.edu.np.mad.mad_p01_team4;

import java.util.ArrayList;
import java.util.List;

public class cart {
    private static cart instance;
    private List<Food> cartitems;

    private cart() {
        cartitems = new ArrayList<>();
    }

    public static cart getInstance(){
        if (instance == null) {
            instance = new cart();
        }
        return instance;
    }
    public void additems(Food food) {
        for (Food item : cartitems) {
            if (item.getName().equals(food.getName())) {
                item.incrementQuantity();
                return;
            }
        }
        cartitems.add(food);
    }

    public List<Food> getCartitems() {
        return cartitems;
    }

    public void clearCart() {
        cartitems.clear();
    }

    public boolean isCartempty() {
        return cartitems.isEmpty();
    }


}
