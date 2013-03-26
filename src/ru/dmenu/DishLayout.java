package ru.dmenu;

import ru.dmenu.data.Dish;
import ru.dmenu.data.Tools;

import ru.dmenu.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DishLayout extends RelativeLayout {

	public DishLayout(Context context) {
		super(context);
	}
	
	public DishLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public DishLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void fillDishValues(Dish dish) {
		MenuImage tImg = (MenuImage) this.findViewById(R.id.main_table_img);
		tImg.assign(dish);
		TextView tv = (TextView) this.findViewById(R.id.main_table_name);
		tv.setText(dish.getName());
		tv = (TextView) this.findViewById(R.id.main_table_price);
		tv.setText(Tools.formatCurrency(getContext(), dish.getPrice().toString()));
		Button addButton = (Button) this.findViewById(R.id.main_table_item_add_button);
		addButton.setTag(dish);
	}
}
