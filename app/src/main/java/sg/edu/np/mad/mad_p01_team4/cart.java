package sg.edu.np.mad.mad_p01_team4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                item.setQuantity(item.getQuantity() + food.getQuantity());
                return;
            }
        }
        cartitems.add(food);
    }

    public void updateCart(Food food) {
        for (Food item : cartitems) {
            if (item.getName().equals(food.getName())) {
                item.setQuantity(food.getQuantity());
                return;
            }
        }
    }

    public List<Food> getCartitems() {
        return cartitems;
    }

}

