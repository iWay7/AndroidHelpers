package site.iway.androidhelpers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import site.iway.javahelpers.Scale;

public class CanvasHelper {

    public static void drawBitmap(Canvas canvas,
                                  RectF rectFOfCanvas,
                                  Bitmap bitmap,
                                  Rect rectOfBitmap,
                                  Scale scaleOfBitmap,
                                  Paint paint) {

        if (canvas == null) {
            throw new NullPointerException("canvas can not be null.");
        }
        if (rectFOfCanvas == null) {
            rectFOfCanvas = new RectF();
            rectFOfCanvas.left = 0;
            rectFOfCanvas.top = 0;
            rectFOfCanvas.right = rectFOfCanvas.left + canvas.getWidth();
            rectFOfCanvas.bottom = rectFOfCanvas.top + canvas.getHeight();
        }
        if (bitmap == null) {
            throw new NullPointerException("bitmap can not be null.");
        }
        if (rectOfBitmap == null) {
            rectOfBitmap = new Rect();
            rectOfBitmap.left = 0;
            rectOfBitmap.top = 0;
            rectOfBitmap.right = rectOfBitmap.left + bitmap.getWidth();
            rectOfBitmap.bottom = rectOfBitmap.top + bitmap.getHeight();
        }
        if (scaleOfBitmap == null) {
            scaleOfBitmap = Scale.None;
        }
        if (paint == null) {
            paint = new Paint();
        }

        canvas.save();
        canvas.clipRect(rectFOfCanvas);

        float rectFOfCanvasWidth = rectFOfCanvas.width();
        float rectFOfCanvasHeight = rectFOfCanvas.height();
        float rectOfBitmapWidth = rectOfBitmap.width();
        float rectOfBitmapHeight = rectOfBitmap.height();

        float scaledRectOfBitmapWidth = scaleOfBitmap.getScaledWidth(
                rectFOfCanvasWidth,
                rectFOfCanvasHeight,
                rectOfBitmapWidth,
                rectOfBitmapHeight);
        float scaledRectOfBitmapHeight = scaleOfBitmap.getScaledHeight(
                rectFOfCanvasWidth,
                rectFOfCanvasHeight,
                rectOfBitmapWidth,
                rectOfBitmapHeight);

        RectF rectFToDraw = new RectF();

        if (scaleOfBitmap == Scale.None) {
            rectFToDraw.left = rectFOfCanvas.left;
            rectFToDraw.top = rectFOfCanvas.top;
            rectFToDraw.right = rectFToDraw.left + scaledRectOfBitmapWidth;
            rectFToDraw.bottom = rectFToDraw.top + scaledRectOfBitmapHeight;
        } else {
            float rectFToDrawCenterX = rectFOfCanvas.centerX();
            float rectFToDrawCenterY = rectFOfCanvas.centerY();
            rectFToDraw.left = rectFToDrawCenterX - scaledRectOfBitmapWidth / 2;
            rectFToDraw.right = rectFToDrawCenterX + scaledRectOfBitmapWidth / 2;
            rectFToDraw.top = rectFToDrawCenterY - scaledRectOfBitmapHeight / 2;
            rectFToDraw.bottom = rectFToDrawCenterY + scaledRectOfBitmapHeight / 2;
        }

        canvas.drawBitmap(bitmap, rectOfBitmap, rectFToDraw, paint);
        canvas.restore();
    }

}
