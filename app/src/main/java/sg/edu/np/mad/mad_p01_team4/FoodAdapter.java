package sg.edu.np.mad.mad_p01_team4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodViewHolder> {
    private ArrayList<Food> list_food;
    private productpage activity;
    public FoodAdapter(ArrayList<Food> list_food, productpage activity){
        this.list_food = list_food;
        this.activity = activity;
    }

    public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_productlist,parent,false);
        FoodViewHolder holder = new FoodViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(FoodViewHolder holder, int position) {
        Food list_items = list_food.get(position);
        holder.name.setText(list_items.getName());
        holder.price.setText("$" + String.format("%.2f", list_items.getPrice()));
        holder.foodimage.setImageResource(list_items.getImageResourceId());
    }

    public int getItemCount() {
        return list_food.size();
    }
}
