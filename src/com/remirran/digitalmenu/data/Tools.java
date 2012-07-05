package com.remirran.digitalmenu.data;

import java.util.Formatter;
import java.util.Locale;

import android.content.Context;

import com.remirran.digitalmenu.R;

public class Tools {
	public static String formatCurrency(Context ctx, String arg) {
		final StringBuilder sbuilder = new StringBuilder();
		final Formatter fmt = new Formatter(sbuilder, Locale.getDefault());
		
		sbuilder.delete(0, sbuilder.length());
		fmt.format(ctx.getString(R.string.price_format), arg);
		return fmt.toString();
	}
}
