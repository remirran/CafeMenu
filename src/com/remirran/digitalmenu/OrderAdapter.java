package com.remirran.digitalmenu;

import java.util.HashMap;

import com.remirran.digitalmenu.data.Dish;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class OrderAdapter extends BaseAdapter {
	private Context ctx;
	private LayoutInflater linf;
	
	public OrderAdapter(Context context, ) {
		ctx = context;
		linf = (LayoutInflater) ctx.getSystemService(ctx.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = linf.inflate(R.layout.main_order_item, parent, false);
		}
		return null;
	}
	
}
