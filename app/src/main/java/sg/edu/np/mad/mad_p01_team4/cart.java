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

    public void updateCart(Food food) {
        for (int i = 0; i < cartitems.size(); i++) {
            if(cartitems.get(i).getName().equals(food.getName())){
                if(food.getQuantity() > 0) {
                    cartitems.set(i, food);
                } else {
                    cartitems.remove(i);
                }
            }
        }
    }
    // calculate total price for checkout
    public double getItemsTotal() {
        double total = 0 ;
        for (Food item : cartitems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public double getGST() {
        // Assuming GST is 7% of items total.
        return getItemsTotal() * 0.09;
    }
}