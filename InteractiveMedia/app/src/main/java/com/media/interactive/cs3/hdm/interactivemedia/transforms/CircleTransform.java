package com.media.interactive.cs3.hdm.interactivemedia.transforms;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;



/**
 * Created by benny on 20.01.18.
 */

public class CircleTransform extends BitmapTransformation {

    /**
     * Instantiates a new circle transform.
     *
     * @param context the context
     */
    public CircleTransform(Context context) {
        super(context);
    }

    /**
     * Circle crop.
     *
     * @param pool   the pool
     * @param source the source
     * @return the bitmap
     */
    private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
        if (source == null) {
            return null;
        }

        final int size = Math.min(source.getWidth(), source.getHeight());
        final int x = (source.getWidth() - size) / 2;
        final int y = (source.getHeight() - size) / 2;

        final Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }

        final Canvas canvas = new Canvas(result);
        final Paint paint = new Paint();
        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        final float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        return result;
    }

    /**
     * Transform.
     *
     * @param pool        the pool
     * @param toTransform the to transform
     * @param outWidth    the out width
     * @param outHeight   the out height
     * @return the bitmap
     */
    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return circleCrop(pool, toTransform);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return getClass().getName();
    }
}