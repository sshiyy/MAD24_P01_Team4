import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ModificationAdapter extends RecyclerView.Adapter<ModificationAdapter.ModificationViewHolder> {
    private List<Modification> modifications;
    private Context context;

    public ModificationAdapter(List<Modification> modifications, Context context) {
        this.modifications = modifications;
        this.context = context;
    }

    @NonNull
    @Override
    public ModificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.modification_item, parent, false);
        return new ModificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModificationViewHolder holder, int position) {
        Modification modification = modifications.get(position);
        holder.checkBox.setText(modification.getName());
        holder.checkBox.setChecked(modification.isSelected());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            modification.setSelected(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return modifications.size();
    }

    public static class ModificationViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public ModificationViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_modification);
        }
    }
}
