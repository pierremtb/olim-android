package com.pierrejacquier.olim.helpers;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

public final class Graphics {

    public static ShapeDrawable createRoundDrawable(String color) {
        ShapeDrawable colorDrawable = new ShapeDrawable(new OvalShape());
        colorDrawable.setIntrinsicWidth(50);
        colorDrawable.setIntrinsicHeight(50);
        colorDrawable.getPaint().setStyle(Paint.Style.FILL);
        colorDrawable.getPaint().setColor(Color.parseColor(color));
        return colorDrawable;
    }

    public static String intColorToHex(int color) {
        return "#" + Integer.toHexString(color).toUpperCase();
    }
}