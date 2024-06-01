package sg.edu.np.mad.mad_p01_team4;

import java.util.ArrayList;
import java.util.List;

public class cart {
    private static cart instance;
    private List<Food> cartitems;
    private CartUpdateListener cartUpdateListener; // Declare the cartUpdateListener variable

    private cart() {
        cartitems = new ArrayList<>();
    }

    public static cart getInstance(){
        if (instance == null) {
            instance = new cart();
        }
        return instance;
    }

    public void setCartUpdateListener(CartUpdateListener listener) {
        this.cartUpdateListener = listener;
    }

    public void additems(Food food) {
        for (Food item : cartitems) {
            if (item.getName().equals(food.getName())) {
                item.setQuantity(item.getQuantity() + food.getQuantity()); // Update quantity
                if (cartUpdateListener != null) {
                    cartUpdateListener.onCartUpdated();
                }
                return;
            }
        }
        cartitems.add(food);
        if (cartUpdateListener != null) {
            cartUpdateListener.onCartUpdated();
        }
    }

    public List<Food> getCartitems() {
        return cartitems;
    }

    public void clearCart() {
        cartitems.clear();
        if (cartUpdateListener != null) {
            cartUpdateListener.onCartUpdated();
        }
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
                if (cartUpdateListener != null) {
                    cartUpdateListener.onCartUpdated();
                }
                return;
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
        // Assuming GST is 9% of items total.
        return getItemsTotal() * 0.09;
    }

    public interface CartUpdateListener {
        void onCartUpdated();
    }
}
