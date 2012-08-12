package com.remirran.digitalmenu;

import com.remirran.digitalmenu.data.Dish;
import com.remirran.digitalmenu.data.ExtData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MenuImage extends ImageView {
	private Dish product;
	private static final int RADIUS = 20;

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
		ExtData.fillImg(dish.getImgUri(), new Handler(), this);
	}
	
	public Dish getProduct() {
		return product;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		BitmapDrawable drawable = (BitmapDrawable) getDrawable();
		if (drawable == null) {
			return;
		}
		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}
		Bitmap fullSizeBitmap = drawable.getBitmap();
		if (fullSizeBitmap == null) {
			return;
		}
		
		int scaledWidth = getMeasuredWidth();
		int scaledHeight = getMeasuredHeight();
		
		Bitmap mScaledBitmap;
		Bitmap mBack = Bitmap.createScaledBitmap(((BitmapDrawable)getContext().getResources().getDrawable(R.drawable.background_dish)).getBitmap(), scaledWidth, scaledHeight, true);
		if (scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight()) {
			mScaledBitmap = fullSizeBitmap;
		} else {
			mScaledBitmap = Bitmap.createScaledBitmap(fullSizeBitmap, scaledWidth, scaledHeight, true /* filter */);
		}
		Bitmap roundBitmap = setRoundCorners(mScaledBitmap, RADIUS, scaledWidth, scaledHeight);
		
		canvas.drawBitmap(roundBitmap, 0, 0, null);
		canvas.drawBitmap(mBack, 0, 0, null);
	}
	
	private Bitmap setRoundCorners(Bitmap source, int radius, int w, int h) {
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		
		/*TODO: add possibility to fill it from config*/
		final int color = 0xffcccccc;
		final Paint paint = new Paint();
		final Rect rect = new Rect(5, 5, w-5, h-5);
		final RectF rectF = new RectF(rect);
		final float roundPX = radius * ExtData.DENSITY;
		
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(source, rect, rect, paint);
		return output;
	}

}
