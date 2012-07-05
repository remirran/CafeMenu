package com.remirran.digitalmenu;

import com.remirran.digitalmenu.data.Dish;
import com.remirran.digitalmenu.data.Order;
import com.remirran.digitalmenu.data.Tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
		adb.setPositiveButton(R.string.add, completeListener);
		adb.setNegativeButton(R.string.not_modify, completeListener);
		return adb.create();
	}
	
	public void setDish(Dish dish) {
		this.dish = dish;
		this.count = Order.getCount(dish);
		if (count == 0) {
			count = 1;
		}
	}
	
	public void updateView(View view) {
		((TextView) view.findViewById(R.id.dialog_name)).setText(dish.getName());
		((TextView) view.findViewById(R.id.dialog_price)).setText(Tools.formatCurrency(ctx, getSum()));
		((MenuImage) view.findViewById(R.id.dialog_img)).assign(dish);
		((TextView) view.findViewById(R.id.dialog_count)).setText(count.toString());
		((ImageButton) view.findViewById(R.id.dialog_inc)).setOnClickListener(chgCount);
		((ImageButton) view.findViewById(R.id.dialog_dec)).setOnClickListener(chgCount);
	}
	
	public void updateDialog(Dialog dialog) {
		LinearLayout view = (LinearLayout) dialog.getWindow().findViewById(R.id.dialog_root);
		updateView(view);
		if (Order.getCount(dish) == 0) {
			Button button = (Button) ((AlertDialog)dialog).getButton(Dialog.BUTTON_POSITIVE);
			button.setText(R.string.add);
			button = (Button) ((AlertDialog)dialog).getButton(Dialog.BUTTON_NEGATIVE);
			button.setText(R.string.not_modify);
		} else {
			Button button = (Button) ((AlertDialog)dialog).getButton(Dialog.BUTTON_POSITIVE);
			button.setText(R.string.change);
			button = (Button) ((AlertDialog)dialog).getButton(Dialog.BUTTON_NEGATIVE);
			button.setText(R.string.remove);
		}
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
				if (count < 1) count = 1;
				break;
			}
			((TextView) view.findViewById(R.id.dialog_count)).setText(count.toString());
			((TextView) view.findViewById(R.id.dialog_price)).setText(Tools.formatCurrency(ctx, getSum()));
		}
	};
	
	android.content.DialogInterface.OnClickListener completeListener = new android.content.DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == Dialog.BUTTON_POSITIVE) Order.setCount(dish, count);
			if (which == Dialog.BUTTON_NEGATIVE) Order.remove(dish);
			((CafeMenuActivity) ctx).updateOrderDetails();
		}
		
	};
}
