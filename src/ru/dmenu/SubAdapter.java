package ru.dmenu;

import java.util.Vector;

import ru.dmenu.data.Section;

import ru.dmenu.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class SubAdapter extends BaseAdapter {
	private Vector<Section> data;
	private LayoutInflater linf;
	private View activeSectionView;

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
	
	public void setActiveItem(View v) {
		if (activeSectionView != null) {
			((TextView) activeSectionView).setBackgroundResource(R.drawable.button_title);
		}
		((TextView) v).setBackgroundResource(R.drawable.button_title_pressed);
		activeSectionView = v;
	}
}
