package sg.edu.np.mad.mad_p01_team4;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class FoodViewHolder extends RecyclerView.ViewHolder {
    TextView name;
    TextView price;
    ImageView foodimage;

    public FoodViewHolder(View itemView){
        super(itemView);
        name = itemView.findViewById(R.id.tvName);
        price = itemView.findViewById(R.id.tvPrice);
        foodimage = itemView.findViewById(R.id.ivImage);
    }
}
