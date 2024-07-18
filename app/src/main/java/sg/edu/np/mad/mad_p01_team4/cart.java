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





}
