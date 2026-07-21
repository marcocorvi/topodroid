/* @file DrawingScaleReference.java
 *
 * @author Andrea Russino
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
package com.topodroid.TDX;

import com.topodroid.util.TDMath;
import com.topodroid.util.TDLog;
import com.topodroid.util.TDColor;
import com.topodroid.prefs.TDSetting;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
// import android.util.FloatMath;

class DrawingScaleReference
{
  private final static float PDF_SCALE = 0.25f;

  private static final float MIN_WIDTH_PERCENT = 0.2f;
  private static final float MAX_WIDTH_PERCENT = 1.0f;
  private static final int HEIGHT_BARS = 6;

  private Point mLocation; // where to draw the scale bar
  private float mMaxWidthPercent;
  private Paint mPaint;
  // private String mUnits;
  // private boolean mExtendAzimuth = false;
  private boolean mHasDecl;  // declination [degrees]
  private float mSdecl; // sin of declination
  private float mCdecl; // cos of declination

  private final static float[] mValues = { 0, 0.01f, 0.05f, 0.1f, 0.5f, 1, 2, 5, 10, 20, 50, 100, 200, 500 };


  // /** cstr - UNUSED
  //  * @param loc bottom-right location of the scale reference on the screen
  //  *            (negative values are allowed with the meaning of negative offset from screen bottom-right)
  //  * @param widthPercent maximum width of scale reference in percentage of screen width
  //  *                     (valid value are in range [0.2, 1.0]
  //  */
  // DrawingScaleReference( Point loc, float widthPercent ) // boolean with_azimuth 
  // {
  //   this( null, loc, widthPercent ); //  with_azimuth 
  // }

  /** cstr
   * @param p   paint used to draw the scale reference. If null, a default painter will be used
   * @param loc negative-Y: bottom-right location of the scale reference on the screen
   *            positive-Y" top-right location
   * @param widthPercent maximum width of scale reference in percentage of screen width
   *                     (valid value are in range [0.2, 1.0]
   * @param decl declination [degrees]
   */
  DrawingScaleReference( Paint p, Point loc, float widthPercent, float decl ) // boolean with_azimuth 
  {
    if (p == null)
    {
      mPaint = new Paint();
      // mPaint.setColor(0xff66a8dd); /* Android Blue */
      mPaint.setColor( TDColor.LIGHT_BLUE );
      mPaint.setStrokeWidth(2);
      mPaint.setTextAlign(Paint.Align.CENTER);
      mPaint.setTextSize( TDSetting.mStationSize );
    } else {
      mPaint = p;
    }

    if (widthPercent < MIN_WIDTH_PERCENT) mMaxWidthPercent = MIN_WIDTH_PERCENT;
    else if (widthPercent > MAX_WIDTH_PERCENT) mMaxWidthPercent = MAX_WIDTH_PERCENT;
    else mMaxWidthPercent = widthPercent;

    mHasDecl = (Math.abs(decl) > 0.01); 
    mSdecl = TDMath.sind( decl );
    mCdecl = TDMath.cosd( decl );
    // TDLog.v("Scale ref decl " + decl + " " + mHasDecl + " c " + mCdecl + " s " + mSdecl );
    mLocation = loc;
    // mUnits = getUnits( TDSetting.mUnitGrid )
    // mExtendAzimuth = with_azimuth;
  }

  /** @return the units of unit
   * @param sketch_unit    sketch_unit length [m]
   */
  private String getUnits( float sketch_unit )
  {
    return  ( sketch_unit > 0.99f)? " m"  // 1.0 m
          : ( sketch_unit > 0.8f)? " yd"  // about 0.98 m
          : ( sketch_unit > 0.2f)? " ft"  // about 0.33 m
          : " dm";                 // 0.1 m
  }

  // /** @return scale reference current color (or white) - UNUSED
  //  */
  // int color() { return mPaint.getColor(); }

  // /** set new paint to be used to draw the scale reference. - UNUSED
  //  * @param paint the new paint to be used. If null, the setting is ignored.
  //  */
  // void setPaint( Paint paint ) {
  //   if ( paint != null ) {
  //     mPaint = paint;
  //   }
  // }

  // /** set text size - UNUSED
  //  * @param size   text size
  //  */
  // void setTextSize( int size )
  // {
  //   if ( size > 0 ) mPaint.setTextSize( size );
  // }

  /** Calculate reference scale
   * @param width canvas width
   * @param canvas_unit  canvas unit
   * @param sketch_unit  sketch unit
   * @return reference length
   */
  private float getReferenceLength( float width, float canvas_unit, float sketch_unit )
  {
    float refLen = width * mMaxWidthPercent / canvas_unit;
    // canvas_unit 1:m 0.914:y 0.6096:2ft
    if ( sketch_unit < 0.2f )      { refLen *= 10; } // using m instead of dm
    else if ( sketch_unit < 0.8f ) { refLen *=  2; } // using ft instead of 2ft
      
    int k = mValues.length - 1;
    while ( k > 0 && refLen < mValues[k] ) --k;
    refLen = mValues[k];
    return (k > 0)? refLen : -1; // neg. --> cannot draw
  }

  /** display the scale reference
   * @param canvas canvas to draw in
   * @param zoom zoom factor used
   * @param sketch_unit  sketch unit length
   */
  void draw( Canvas canvas, float zoom, boolean landscape, float sketch_unit )
  {
    draw( canvas, zoom, landscape, mLocation.x, mLocation.y, sketch_unit );
  }

  /** display the scale reference
   * @param canvas     canvas to draw in
   * @param zoom       zoom factor used
   * @param landscape  whether in landscape presentation
   * @param locx       X coord of the point where to draw the scale-reference
   * @param locy       Y coord of the point where to draw the scale-reference
   * @param sketch_unit  sketch unit length
   */
  void draw( Canvas canvas, float zoom, boolean landscape, float locx, float locy, float sketch_unit )
  {
    if (canvas != null)
    {
      float canvasUnit = DrawingUtil.SCALE_FIX * zoom; /* Length of 1 unit at zoom */


      /* Calculate reference scale */
      float referenceLen = getReferenceLength( canvas.getWidth(), canvasUnit, sketch_unit );

      /* Draw reference scale */
      if ( referenceLen > 0 ) 
      {
        float canvasLen = canvasUnit * referenceLen;
        canvasLen *= sketch_unit;
        if ( sketch_unit < 0.2f ) { } // using m instead of dm
	else if ( sketch_unit < 0.8f ) canvasLen /= 2;

        String refstr = (( referenceLen < 1 )?  Float.toString(referenceLen) : Integer.toString((int)referenceLen)) + getUnits( sketch_unit );
        float locX = (locx > 0) ? locx : canvas.getWidth()  + locx - referenceLen;
        float locY = (locy > 0) ? locy : canvas.getHeight() + locy;
	// TDLog.v( "reference " + referenceLen + units );

        float arrowlen = (float)canvas.getWidth() / 10;
        // if ( landscape ) {
        //   locX *= 8;
        //   arrowlen = (float)canvas.getHeight() / 10; // /= 4;
        // }
        float arrowtip = arrowlen / 5;

        float x = 2 * locX;
        float y = locY;

        canvas.drawLine( x, y, x + canvasLen, y, mPaint);
        canvas.drawLine( x, y, x, y - HEIGHT_BARS, mPaint);
        canvas.drawLine( x + canvasLen, y, x + canvasLen, y - HEIGHT_BARS, mPaint);
        canvas.drawText( refstr, x + canvasLen / 2, y - HEIGHT_BARS, mPaint);

        // North arrow line
	if ( landscape ) {
	  x = locX/2 + arrowlen;
          y = locY + 0.8f * arrowlen; // - 8 * HEIGHT_BARS;	  
          if ( mSdecl > 0 ) y -= arrowlen * mSdecl;
          drawArrowTipLandscape( canvas, x, y, arrowlen, arrowtip, mPaint );
          if ( mHasDecl ) {
            float xd = x - arrowlen * mCdecl;
            float yd = y + arrowlen * mSdecl;
            canvas.drawLine( x, y, xd, yd, BrushManager.lightBluePaint);
          }
          // if ( mExtendAzimuth && TDAzimuth.mFixedExtend == 0 ) {
          //   canvas.drawLine( x, y, x - TDMath.cosd( TDAzimuth.mRefAzimuth ), y - TDMath.sind( TDAzimuth.mRefAzimuth ), mPaint );
          // }
        } else {
          x = locX/2 + HEIGHT_BARS;
          y = locY + 0.4f * arrowlen; // - 4 * HEIGHT_BARS;
          if ( mSdecl > 0 ) x += arrowlen * mSdecl;
          drawArrowTipPortrait( canvas, x, y, arrowlen, arrowtip, mPaint );
          if ( mHasDecl ) {
            float xd = x - arrowlen * mSdecl;
            float yd = y - arrowlen * mCdecl;
            canvas.drawLine( x, y, xd, yd, BrushManager.lightBluePaint);
          }
          // if ( mExtendAzimuth && TDAzimuth.mFixedExtend == 0 ) {
          //   canvas.drawLine( x, y, x + TDMath.sind( TDAzimuth.mRefAzimuth ), y - TDMath.cosd( TDAzimuth.mRefAzimuth ), mPaint );
          // }
        }
      }
    }
  }

  /** draw tip of North arrow
   */
  private void drawArrowTipLandscape( Canvas canvas, float x, float y, float arrow_len, float arrow_tip, Paint paint )
  {
    canvas.drawLine( x, y, x - arrow_len, y, paint);
    canvas.drawLine( x - arrow_len + arrow_tip, y - arrow_tip, x - arrow_len, y, paint);
    canvas.drawLine( x - arrow_len + arrow_tip, y + arrow_tip, x - arrow_len, y, paint);
  }

  private void drawArrowTipPortrait( Canvas canvas, float x, float y, float arrow_len, float arrow_tip, Paint paint )
  {
    canvas.drawLine( x, y, x, y - arrow_len, paint);
    canvas.drawLine( x - arrow_tip, y - arrow_len + arrow_tip, x, y - arrow_len, paint);
    canvas.drawLine( x + arrow_tip, y - arrow_len + arrow_tip, x, y - arrow_len, paint);
  }

  /** display the scale reference
   * @param canvas canvas to draw in
   * @param zoom zoom factor used
   * @param sketch_unit  sketch unit length
   * @param xor_color    xoring color
   * @note text size is reduced by 25 %
   */
  void draw( Canvas canvas, float zoom, boolean landscape, float sketch_unit, int xor_color )
  {
    draw( canvas, zoom, landscape, mLocation.x, mLocation.y, sketch_unit, xor_color );
  }

  /** display the scale reference
   * @param canvas     canvas to draw in
   * @param zoom       zoom factor used
   * @param landscape  whether in landscape presentation
   * @param locx       X coord of the point where to draw the scale-reference
   * @param locy       Y coord of the point where to draw the scale-reference
   * @param sketch_unit  sketch unit length
   * @param xor_color    xoring color
   * @note text size is reduced by 25 %
   */
  void draw( Canvas canvas, float zoom, boolean landscape, float locx, float locy, float sketch_unit, int xor_color )
  {
    if (canvas != null)
    {
      float canvasUnit = DrawingUtil.SCALE_FIX * zoom; /* Length of 1 unit at zoom */

      /* Calculate reference scale */
      float referenceLen = getReferenceLength( canvas.getWidth(), canvasUnit, sketch_unit );

      /* Draw reference scale */
      if ( referenceLen > 0 ) 
      {
        Paint paint = DrawingPath.xorPaint( mPaint, xor_color );
        paint.setTextSize( PDF_SCALE * mPaint.getTextSize() );
        float canvasLen = canvasUnit * referenceLen;
        canvasLen *= sketch_unit;
        if ( sketch_unit < 0.2f ) { } // using m instead of dm
	else if ( sketch_unit < 0.8f ) canvasLen /= 2;

        String refstr = (( referenceLen < 1 )?  Float.toString(referenceLen) : Integer.toString((int)referenceLen)) + getUnits( sketch_unit );
        float locX = (locx > 0) ? locx : canvas.getWidth()  + locx - referenceLen;
        float locY = (locy > 0) ? locy : canvas.getHeight() + locy;
	// TDLog.v( "reference " + referenceLen + units );

        float arrowlen = (float)canvas.getWidth() / 10;
        // if ( landscape ) {
        //   locX *= 8;
        //   arrowlen = (float)canvas.getHeight() / 10; // /= 4;
        // }
        float arrowtip = arrowlen / 5;

        float x = 2 * locX;
        float y = locY;

        canvas.drawLine( locX, locY, locX + canvasLen, locY, paint);
        canvas.drawLine( locX, locY, locX, locY - HEIGHT_BARS, paint);
        canvas.drawLine( locX + canvasLen, locY, locX + canvasLen, locY - HEIGHT_BARS, paint);
        canvas.drawText( refstr, locX + canvasLen / 2, locY - HEIGHT_BARS, paint);

	if ( landscape ) {
	  x = locX/2 + arrowlen;
          y = locY + 0.8f * arrowlen; // - 8 * HEIGHT_BARS;	  
          drawArrowTipLandscape( canvas, x, y, arrowlen, arrowtip, paint );
          if ( mHasDecl ) {
            float xd = x - arrowlen * mCdecl; // FIXME might be wrong
            float yd = y + arrowlen * mSdecl;
            canvas.drawLine( x, y, xd, yd, paint);
          }
          // if ( mExtendAzimuth && TDAzimuth.mFixedExtend == 0 ) {
          //   canvas.drawLine( x, y, x - TDMath.cosd( TDAzimuth.mRefAzimuth ), y - TDMath.sind( TDAzimuth.mRefAzimuth ), paint );
          // }
        } else {
          x = locX/2 + HEIGHT_BARS;
          y = locY + 0.4f * arrowlen;
          drawArrowTipPortrait( canvas, x, y, arrowlen, arrowtip, paint );
          if ( mHasDecl ) {
            float xd = x - arrowlen * mSdecl;
            float yd = y - arrowlen * mCdecl;
            canvas.drawLine( x, y, xd, yd, paint);
          }
          // if ( mExtendAzimuth && TDAzimuth.mFixedExtend == 0 ) {
          //   canvas.drawLine( x, y, x + TDMath.sind( TDAzimuth.mRefAzimuth ), y - TDMath.cosd( TDAzimuth.mRefAzimuth ), paint );
          // }
        }
      }
    }
  }

}
