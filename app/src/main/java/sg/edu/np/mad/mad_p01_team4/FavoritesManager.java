package sg.edu.np.mad.mad_p01_team4;

import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static List<Food> favoriteItems = new ArrayList<>();

    public static void addToFavorites(Food food) {
        if (!favoriteItems.contains(food)) {
            favoriteItems.add(food);
        }
    }

    public static void removeFromFavorites(Food food) {
        favoriteItems.remove(food);
    }

    public static boolean isFavorite(Food food) {
        return favoriteItems.contains(food);
    }

    public static List<Food> getFavoriteItems() {
        return new ArrayList<>(favoriteItems);  // Return a copy to avoid external modification
    }
}
