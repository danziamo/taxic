package taxi.city.citytaxiclient.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import taxi.city.citytaxiclient.OrderDetailsActivity;
import taxi.city.citytaxiclient.R;
import taxi.city.citytaxiclient.models.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private ArrayList<Order> items;
    private int itemLayout;
    private final Context mContext;

    public OrderAdapter(ArrayList<Order> items, int layout, Context context) {
        this.items = items;
        this.itemLayout = layout;
        this.mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mInfoView;
        public TextView mAddressView;
        public TextView mPriceView;
        public TextView mDistanceView;

        public ViewHolder(final View v) {
            super(v);
            mInfoView = (TextView) itemView.findViewById(R.id.tvInfoText);
            mAddressView = (TextView) itemView.findViewById(R.id.tvAddress);
            mDistanceView = (TextView) itemView.findViewById(R.id.tvDistance);
            mPriceView = (TextView) itemView.findViewById(R.id.tvPrice);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Order order = (Order) v.getTag();
                    Intent intent = new Intent(mContext, OrderDetailsActivity.class);
                    intent.putExtra("DATA", order);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Order item = items.get(position);
        holder.mAddressView.setText(item.getStartName());
        holder.mInfoView.setText("#" + item.getId());
        holder.mPriceView.setText(String.valueOf(item.getTotalSum()));
        holder.mDistanceView.setText(String.valueOf(item.getDistance()));
        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setDataset(ArrayList<Order> dataset) {
        items = dataset;
        // This isn't working
        notifyItemRangeInserted(0, items.size());
        notifyDataSetChanged();
    }
}