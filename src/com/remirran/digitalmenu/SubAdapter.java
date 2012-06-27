package com.remirran.digitalmenu;

import java.util.Vector;

import com.remirran.digitalmenu.data.Section;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class SubAdapter extends BaseAdapter {
	private Vector<Section> data;
	private LayoutInflater linf;

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = linf.inflate(R.layout.main_section_item, parent, false);
		}
		TextView text = (TextView) view.findViewById(R.id.main_section_item);
		text.setText(data.get(position).getName());
		return view;
	}
	
	public SubAdapter(Context ctx, Vector<Section> data) {
		this.data = data;
		linf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
}
