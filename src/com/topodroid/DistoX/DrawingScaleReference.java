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
// import android.util.FloatMath;

// import android.util.Log;

class DrawingScaleReference
{
  private static final float MIN_WIDTH_PERCENT = 0.2f;
  private static final float MAX_WIDTH_PERCENT = 1.0f;
  private static final int HEIGHT_BARS = 6;

  private Point mLocation;
  private float mMaxWidthPercent;
  private Paint mPaint;
  private String mUnits;

  private final static float[] mValues = { 0, 0.01f, 0.05f, 0.1f, 0.5f, 1, 2, 5, 10, 20, 50, 100, 200, 500 };


  /**
   * Costructor
   * @param loc bottom-right location of the scale reference on the screen
   *            (negative values are allowed with the meaning of negative offset from screen bottom-right)
   * @param widthPercent maximum width of scale reference in percentage of screen width
   *                     (valid value are in range [0.2, 1.0]
   */
  DrawingScaleReference( Point loc, float widthPercent )
  {
    this( null, loc, widthPercent );
  }

  /**
   * Costructor
   * @param p   paint used to draw the scale reference. If null, a default painter will be used
   * @param loc bottom-right location of the scale reference on the screen
   *            (negative values are allowed with the meaning of negative offset from screen bottom-right)
   * @param widthPercent maximum width of scale reference in percentage of screen width
   *                     (valid value are in range [0.2, 1.0]
   */
  DrawingScaleReference( Paint p, Point loc, float widthPercent) {

    if (p == null)
    {
      mPaint = new Paint();
      // mPaint.setColor(0xff33ccff); /* Android Blue */
      mPaint.setColor( TDColor.LIGHT_BLUE );
      mPaint.setStrokeWidth(2);
      mPaint.setTextAlign(Paint.Align.CENTER);
      mPaint.setTextSize( TDSetting.mStationSize );
    } else {
      mPaint = p;
    }

    if(widthPercent < MIN_WIDTH_PERCENT) mMaxWidthPercent = MIN_WIDTH_PERCENT;
    else if(widthPercent > MAX_WIDTH_PERCENT) mMaxWidthPercent = MAX_WIDTH_PERCENT;
    else mMaxWidthPercent = widthPercent;

    mLocation = loc;
    mUnits = ( TDSetting.mUnitGrid > 0.99f)? " m" 
           : ( TDSetting.mUnitGrid > 0.8f)? " yd"
           : ( TDSetting.mUnitGrid > 0.2f)? " ft" : " dm";
  }


  /**
   * Get the current color (or white)
   * @return scale reference color
   */
  int color() { return mPaint.getColor(); }

  /**
   * Set new paint to be used to draw the scale reference.
   *
   * @param paint the new paint to be used. If null, the setting is ignored.
   */
  void setPaint( Paint paint ) {
    if ( paint != null ) {
      mPaint = paint;
    }
  }

  /** set text size
   * @param size   text size
   */
  void setTextSize( int size )
  {
    if ( size > 0 ) mPaint.setTextSize( size );
  }

  /**
   * Draw the scale reference
   * @param canvas canvas to draw in
   * @param zoom zoom factor used
     */
  void draw( Canvas canvas, float zoom, boolean landscape )
  {
    if (canvas != null)
    {
      float canvasUnit = DrawingUtil.SCALE_FIX * zoom; /* Length of 1 unit at zoom */

      float arrowlen = (float)canvas.getWidth() / 10;
      float arrowtip = arrowlen / 5;


      /* Calculate reference scale */
      float referenceLen = canvas.getWidth() * mMaxWidthPercent / canvasUnit;
      // units 1:m 0.914:y 0.6096:2ft
      if ( TDSetting.mUnitGrid < 0.2f )      { referenceLen *= 10; } // using m instead of dm
      else if ( TDSetting.mUnitGrid < 0.8f ) { referenceLen *=  2; } // using ft instead of 2ft
      
      int k = mValues.length - 1;
      while ( k > 0 && referenceLen < mValues[k] ) --k;
      referenceLen = mValues[k];

      /* Draw reference scale */
      if ( k > 0 ) 
      {
        float canvasLen = canvasUnit * referenceLen;
        canvasLen *= TDSetting.mUnitGrid;
        if ( TDSetting.mUnitGrid < 0.2f ) { } // using m instead of dm
	else if ( TDSetting.mUnitGrid < 0.8f ) canvasLen /= 2;

        String refstr = (( referenceLen < 1 )?  Float.toString(referenceLen) : Integer.toString((int)referenceLen)) + mUnits;
        float locX = (mLocation.x > 0) ? mLocation.x : canvas.getWidth() + mLocation.x - referenceLen;
        float locY = (mLocation.y > 0) ? mLocation.y : canvas.getHeight() + mLocation.y;
	// Log.v("DistoX", "reference " + referenceLen + mUnits );

        canvas.drawLine( locX, locY, locX + canvasLen, locY, mPaint);
        canvas.drawLine( locX, locY, locX, locY - HEIGHT_BARS, mPaint);
        canvas.drawLine( locX + canvasLen, locY, locX + canvasLen, locY - HEIGHT_BARS, mPaint);
        canvas.drawText( refstr, locX + canvasLen / 2, locY - HEIGHT_BARS, mPaint);

	if ( landscape ) {
	  float x = locX + arrowlen;
          float y = locY - 8 * HEIGHT_BARS;	  
          canvas.drawLine( x, y, x - arrowlen, y, mPaint);
          canvas.drawLine( x-arrowlen+arrowtip, y-arrowtip, x - arrowlen, y, mPaint);
          canvas.drawLine( x-arrowlen+arrowtip, y+arrowtip, x - arrowlen, y, mPaint);
	  } else {
          float x = locX;
          float y = locY - 4 * HEIGHT_BARS;
          canvas.drawLine( x, y, x, y - arrowlen, mPaint);
          canvas.drawLine( x-arrowtip, y-arrowlen+arrowtip, x, y - arrowlen, mPaint);
          canvas.drawLine( x+arrowtip, y-arrowlen+arrowtip, x, y - arrowlen, mPaint);
        }
      }
    }
  }
}
