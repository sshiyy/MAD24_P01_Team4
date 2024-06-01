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

    public void additems(Food food) {
        for (Food item : cartitems) {
            if (item.getName().equals(food.getName())) {
                // check if item exists
                // exists -> update quantity
                item.setQuantity(item.getQuantity() + food.getQuantity()); // Update quantity
                if (cartUpdateListener != null) {
                    // notifies the listener if it is set
                    cartUpdateListener.onCartUpdated();
                }
                return;
            }
        }
        // does not exist -> adds item to cart
        cartitems.add(food);
        if (cartUpdateListener != null) {
            // notifies the listener if it is set
            cartUpdateListener.onCartUpdated();
        }
    }

    // returns list of food items in cart
    public List<Food> getCartitems() {
        return cartitems;
    }

    // clears all items from cart & notifies the cartUpdateListener
    public void clearCart() {
        cartitems.clear();
        if (cartUpdateListener != null) {
            cartUpdateListener.onCartUpdated();
        }
    }

    // check if cart empty
    public boolean isCartempty() {
        return cartitems.isEmpty();
    }

    public void updateCart(Food food) {
        for (int i = 0; i < cartitems.size(); i++) {
            // checks if the item is in the cart
            if(cartitems.get(i).getName().equals(food.getName())){
                if(food.getQuantity() > 0) {
                    // quantity > 0 -> updates the item
                    cartitems.set(i, food);
                } else {
                    // quantity < 0 -> removes item
                    cartitems.remove(i);
                }
                if (cartUpdateListener != null) {
                    // notifies listener
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
        //  GST is 9% of items total.
        return getItemsTotal() * 0.09;
    }

    // classes that wan to be notified of the cart updates implement this
    // interface & override the this method
    public interface CartUpdateListener {
        void onCartUpdated();
    }
}
