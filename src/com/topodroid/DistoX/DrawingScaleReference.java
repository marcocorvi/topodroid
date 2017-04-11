/* @file DrawingScaleReference.java
 *
 * @author andrea russino
 * @date feb 2016
 *
 * @brief A scale reference is a reverse square bracket scaled to the current scale factor, used to
 * get provide a constant and self-adjustable scale reference on the canvas.
 * Its aspect on the screen is something like this:
 *
 *                5 m
 *        |_________________|
 *
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.FloatMath;

public class DrawingScaleReference
{
  private static final float MIN_WIDTH_PERCENT = 0.2f;
  private static final float MAX_WIDTH_PERCENT = 1.0f;
  private static final int HEIGHT_BARS = 6;

  private Point mLocation;
  private float mMaxWidthPercent;
  private Paint mPaint;


  /**
   * Costructor
   * @param loc bottom-right location of the scale reference on the screen
   *            (negative values are allowed with the meaning of negative offset from screen bottom-right)
   * @param widthPercent maximum width of scale reference in percentage of screen width
   *                     (valid value are in range [0.2, 1.0]
   */
  public DrawingScaleReference(Point loc, float widthPercent) {
    this(null, loc, widthPercent);
  }

  /**
   * Costructor
   * @param p   paint used to draw the scale reference. If null, a default painter will be used
   * @param loc bottom-right location of the scale reference on the screen
   *            (negative values are allowed with the meaning of negative offset from screen bottom-right)
   * @param widthPercent maximum width of scale reference in percentage of screen width
   *                     (valid value are in range [0.2, 1.0]
   */
  public DrawingScaleReference(Paint p, Point loc, float widthPercent) {

    if(p == null)
    {
      mPaint = new Paint();
      mPaint.setColor(0xff33ccff); /* Android Blue */
      mPaint.setStrokeWidth(2);
      mPaint.setTextAlign(Paint.Align.CENTER);
    }

    if(widthPercent < MIN_WIDTH_PERCENT) mMaxWidthPercent = MIN_WIDTH_PERCENT;
    else if(widthPercent > MAX_WIDTH_PERCENT) mMaxWidthPercent = MAX_WIDTH_PERCENT;
    else mMaxWidthPercent = widthPercent;

    mLocation = loc;
  }


  /**
   * Get the current color (or white)
   * @return scale reference color
   */
  int color() { return mPaint.getColor(); }

  /**
   * Set new paint to be used to draw the scale reference.
   *
   * @param paint th new paint to be used. If null, the setting is ignored.
   */
  public void setPaint( Paint paint ) {
    if(paint != null) {
      mPaint = paint;
    }
  }

  /**
   * Draw the scale reference
   * @param canvas canvas to draw in
   * @param zoom zoom factor used
     */
  public void draw( Canvas canvas, float zoom )
  {
    if(canvas != null)
    {
      float canvasUnit = DrawingUtil.SCALE_FIX * zoom; /* Length of 1 unit at zoom */

      /* Calculate reference scale */
      float referenceLen = canvas.getWidth() * mMaxWidthPercent / canvasUnit;
      if(referenceLen > 1)
      {
        referenceLen = FloatMath.floor(referenceLen);
      }
      else if(referenceLen > 0.75f)
      {
        referenceLen = 0.75f;
      }
      else if(referenceLen > 0.50f)
      {
        referenceLen = 0.50f;
      }
      else if(referenceLen > 0.25f)
      {
        referenceLen = 0.25f;
      }
      else if(referenceLen > 0.10f)
      {
        referenceLen = 0.10f;
      }
      else
      {
        referenceLen = 0;
      }

      /* Draw reference scale */
      if(referenceLen != 0)
      {
        float canvasLen = canvasUnit * referenceLen;
        float locX = (mLocation.x > 0) ? mLocation.x : canvas.getWidth() + mLocation.x - referenceLen;
        float locY = (mLocation.y > 0) ? mLocation.y : canvas.getHeight() + mLocation.y;

        canvas.drawLine(locX, locY, locX + canvasLen, locY, mPaint);
        canvas.drawLine(locX, locY, locX, locY - HEIGHT_BARS, mPaint);
        canvas.drawLine(locX + canvasLen, locY, locX + canvasLen, locY - HEIGHT_BARS, mPaint);
        if(referenceLen < 1) {
          canvas.drawText(referenceLen + " m", locX + canvasLen / 2, locY - HEIGHT_BARS, mPaint);
        }
        else {
          canvas.drawText((int)referenceLen + " m", locX + canvasLen / 2, locY - HEIGHT_BARS, mPaint);
        }
      }
    }
  }
}
