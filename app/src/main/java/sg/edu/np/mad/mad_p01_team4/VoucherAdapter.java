package sg.edu.np.mad.mad_p01_team4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    private List<Voucher> voucherList;
    private Context context;
    private OnVoucherUseListener onVoucherUseListener;

    public interface OnVoucherUseListener {
        void onVoucherUse(String voucherTitle, String voucherDiscount);
    }

    public VoucherAdapter(List<Voucher> voucherList, Context context, OnVoucherUseListener onVoucherUseListener) {
        this.voucherList = voucherList;
        this.context = context;
        this.onVoucherUseListener = onVoucherUseListener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.voucher_item, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.voucherTitle.setText(voucher.getTitle());
        holder.voucherDiscount.setText(voucher.getDiscount());

        holder.useButton.setOnClickListener(v -> {
            if (onVoucherUseListener != null) {
                onVoucherUseListener.onVoucherUse(voucher.getTitle(), voucher.getDiscount());
            }
        });
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        public TextView voucherTitle;
        public TextView voucherDiscount;
        public Button useButton;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            voucherTitle = itemView.findViewById(R.id.voucherTitle);
            voucherDiscount = itemView.findViewById(R.id.voucherDiscount);
            useButton = itemView.findViewById(R.id.useButton);
        }
    }
}
