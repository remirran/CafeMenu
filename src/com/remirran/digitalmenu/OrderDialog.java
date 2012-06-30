package com.remirran.digitalmenu;

import com.remirran.digitalmenu.data.Dish;
import com.remirran.digitalmenu.data.Order;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OrderDialog {
	private Context ctx;
	private LayoutInflater linf;
	private Dish dish;
	private Integer count;
	private LinearLayout view;
	
	public OrderDialog(Context ctx, Dish dish) {
		this.ctx = ctx;
		this.linf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.dish = dish;
		this.count = Order.getCount(dish);
		if (count == 0) {
			count = 1;
		}
	}
	
	public Dialog draw() {
		AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
		
		adb.setTitle(R.string.dialog_add_title);
		view = (LinearLayout) linf.inflate(R.layout.order_dialog_add, null);
		adb.setView(view);
		((TextView) view.findViewById(R.id.dialog_name)).setText(dish.getName());
		((TextView) view.findViewById(R.id.dialog_price)).setText(getSum());
		((MenuImage) view.findViewById(R.id.dialog_img)).assign(dish);
		((TextView) view.findViewById(R.id.dialog_count)).setText(count.toString());
		
		((ImageButton) view.findViewById(R.id.dialog_inc)).setOnClickListener(chgCount);
		((ImageButton) view.findViewById(R.id.dialog_dec)).setOnClickListener(chgCount);
		
		return adb.create();
	}
	
	private String getSum() {
		return ""+count * dish.getPrice();
	}
	
	OnClickListener chgCount = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dialog_inc:
				count++;
				break;
			case R.id.dialog_dec:
				count--;
				break;
			}
			((TextView) view.findViewById(R.id.dialog_count)).setText(count.toString());
			((TextView) view.findViewById(R.id.dialog_price)).setText(getSum());
		}
	};
	
	OnClickListener addClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Order.setCount(dish, count);
			/*TODO: close dialog and force update orders list*/
			
		}
	};
}
