package ru.dmenu;

import ru.dmenu.data.Order;
import ru.dmenu.data.Tools;

import ru.dmenu.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OrderAdapter extends BaseAdapter {
	private Context ctx;
	private LayoutInflater linf;
	private int styled_layout;
	
	public OrderAdapter(Context context, int layout) {
		ctx = context;
		linf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		styled_layout = layout;
	}
	
	@Override
	public int getCount() {
		return Order.getSize();
	}

	@Override
	public Object getItem(int position) {
		return Order.getItemByIndex(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = linf.inflate(styled_layout, parent, false);
		}
		
		((TextView) view.findViewById(R.id.order_name)).setText(Order.getItemByIndex(position).getName());
		((TextView) view.findViewById(R.id.order_count)).setText("x"+Order.getCountByIndex(position)+"=");
		((TextView) view.findViewById(R.id.order_price)).setText(Tools.formatCurrency(ctx, Order.getSumByIndex(position)));
		ImageView iv = (ImageView) view.findViewById(R.id.order_cancel);
		iv.setOnClickListener(imgRemoveClickListener);
		iv.setTag(Order.invert(position));
		return view;
	}
	
	OnClickListener imgRemoveClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Order.remove((Integer) v.getTag());
			((CafeMenuActivity) ctx).updateOrderDetails();
		}
	};
	
}
