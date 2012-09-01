package com.remirran.digitalmenu;

import com.remirran.digitalmenu.data.Dish;
import com.remirran.digitalmenu.data.ExtData;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MenuImage extends ImageView {
	private Dish product;
	public MenuImage(Context context) {
		super(context);
	}
	
	public MenuImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MenuImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void assign(Dish dish) {
		product = dish;
		setContentDescription(dish.getDesc());
		/*TODO: to think about sync*/
		ExtData.getInstance().fillImg(dish.getImgUri(), this);
	}
	
	public Dish getProduct() {
		return product;
	}
}
