/* MyColorPicker.java
 *
 * @note This class is adapted from API demo ColorPicker in Android 2.2 (api-8)
 *       therefore I leave the original copyright
 * ----------------------------------------------------------------------------
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.topodroid.ui;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.R;

import android.os.Bundle;
// import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;

import android.view.MotionEvent;
import android.view.View;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.GridLayout;

public class MyColorPicker extends MyDialog
                           implements View.OnClickListener
{
  private static final int CENTER_X = 200;
  private static final int CENTER_Y = 200;
  private static final int CENTER_RADIUS = 64;
  private static final int CENTER_RADIUS_2 = CENTER_RADIUS * CENTER_RADIUS;

  private Button mBtnOk;
  private Button mBtnClear;
  private Button mBtnClose;

  public interface IColorChanged {
    public void colorChanged(int color);
  }

  private IColorChanged mListener;
  private int mColor;
  private ColorPickerView mColorPicker;

  private LinearLayout mColorLayout;
  private GridLayout   mGridLayout;

  // content view of color picker
  private static class ColorPickerView extends View
  {
      private Paint mPaint;
      private Paint mCenterPaint;
      private final int[] mColors;
      private IColorChanged mListener;
      
      private boolean mTrackingCenter;
      private boolean mHighlightCenter;
      private RectF   mRect;
      
      /** cstr
       * @param c     context
       * @param l     listener to color changes
       * @param color initial color
       */
      ColorPickerView(Context c, IColorChanged l, int color)
      {
        super(c);
        mListener = l;
        // red cyan blue aqua green yellow red
        mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
        Shader s = new SweepGradient(0, 0, mColors, null);
        
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(s);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(64);
        
        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setColor(color);
        mCenterPaint.setStrokeWidth(5);

        float r = CENTER_X - mPaint.getStrokeWidth()*0.5f;
	mRect = new RectF( -r, -r, r, r );
      }

      @Override 
      protected void onDraw(Canvas canvas)
      {
          canvas.translate(CENTER_X, CENTER_X);
          canvas.drawOval( mRect, mPaint);            
          canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
          
          if (mTrackingCenter) {
              int c = mCenterPaint.getColor();
              mCenterPaint.setStyle(Paint.Style.STROKE);
              mCenterPaint.setAlpha( mHighlightCenter? 0xff : 0x80 );
              canvas.drawCircle(0, 0, CENTER_RADIUS + mCenterPaint.getStrokeWidth(), mCenterPaint);
              mCenterPaint.setStyle(Paint.Style.FILL);
              mCenterPaint.setColor(c);
          }
      }
      
      @Override
      protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
          setMeasuredDimension(CENTER_X*2, CENTER_Y*2);
      }
      
      /** @return rounded integer value
       * @param x  input float value
       */
      private int floatToByte(float x) { return Math.round(x); }

      /** @return byte-clipped integer, ie, in [0,255]
       * @param n input integer
       */
      private int pinToByte(int n) { return ( n < 0 )? 0 : ( n > 255 ) ? 255 : n; }
      
      /** @return the interpolation between two integers
       * @param s first integer
       * @param d second integer
       * @param p fraction of second integer - in [0,1]
       * @note (1-p) is the fraction of the first integer
       */
      private int ave(int s, int d, float p) { return s + Math.round(p * (d - s)); }
      
      /** get the color interpolating an array of colors
       * @param colors   array of colors
       * @param unit     interpolation abscissa in [0,1], 0: first color, 1:last color
       * @return interpolated color
       * @note for circular interpolation the first and the last color must be equal
       */
      private int interpColor(int[] colors, float unit)
      {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }
        float p = unit * (colors.length - 1);
        int i = (int)p;
        p -= i;
        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i+1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0),   Color.red(c1),   p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0),  Color.blue(c1),  p);
        return Color.argb(a, r, g, b);
      }
      
      /** "rotate" the color hue
       * @param color   input color
       * @param rad     rotation angle [radians]
       * @return rotated color
       */
      private int rotateColor(int color, float rad) {
          float deg = rad * 180 / 3.1415927f;
          int r = Color.red(color);
          int g = Color.green(color);
          int b = Color.blue(color);
          
          ColorMatrix cm = new ColorMatrix();
          ColorMatrix tmp = new ColorMatrix();

          cm.setRGB2YUV();
          tmp.setRotate(0, deg);
          cm.postConcat(tmp);
          tmp.setYUV2RGB();
          cm.postConcat(tmp);
          
          final float[] a = cm.getArray();

          int ir = floatToByte(a[0] * r +  a[1] * g +  a[2] * b);
          int ig = floatToByte(a[5] * r +  a[6] * g +  a[7] * b);
          int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);
          
          return Color.argb(Color.alpha(color), pinToByte(ir), pinToByte(ig), pinToByte(ib));
      }
      
      private static final float PI = 3.1415926f;

      /** react to user touch
       * @param event  touch event
       */
      @Override
      public boolean onTouchEvent(MotionEvent event)
      {
        float x = event.getX() - CENTER_X;
        float y = event.getY() - CENTER_Y;
        boolean inCenter = (x*x + y*y) <= CENTER_RADIUS_2;
        
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            mTrackingCenter = inCenter;
            if (inCenter) {
                mHighlightCenter = true;
                invalidate();
                break;
            }
          case MotionEvent.ACTION_MOVE:
            if (mTrackingCenter) {
                if (mHighlightCenter != inCenter) {
                    mHighlightCenter = inCenter;
                    invalidate();
                }
            } else {
                float angle = TDMath.atan2(y, x);
                // need to turn angle [-PI ... PI] into unit [0....1]
                float unit = angle/(2*PI);
                if (unit < 0) {
                    unit += 1;
                }
                mCenterPaint.setColor(interpColor(mColors, unit));
                invalidate();
            }
            break;
          case MotionEvent.ACTION_UP:
            if (mTrackingCenter) {
                if (inCenter) {
                    mListener.colorChanged(mCenterPaint.getColor());
                }
                mTrackingCenter = false;    // so we draw w/o halo
                invalidate();
            }
            break;
        }
        return true;
      }

      int getColor() { return mCenterPaint.getColor(); }
    }
    // ------------------------------------------------------------------------------

    /** color call button
     */
    private class MyColorCell extends Button
    {
      private int idx; // index of the color in TDColor.mTDColors

      /** cstr
       * @param context      context
       * @param k            color index
       * @param listener     color-changed listener
      */
      MyColorCell( Context ctx, int k, OnClickListener listener, int w, int h )
      {
        super( ctx );
        idx = k;
        setOnClickListener( listener );
        setMinWidth(  w );
        setMinHeight( h );
        setMaxWidth(  w );
        setMaxHeight( h );
        setBackgroundColor( TDColor.mTDColors[ idx ] );
      }

      /** @return the color index
       */
      int getIndex() { return idx; }
    }
    // ------------------------------------------------------------------------------

    /** cstr
     * @param context      context
     * @param listener     color-changed listener
     * @param initialColor initial color
     */
    public MyColorPicker( Context context, IColorChanged listener, int initialColor)
    {
      super(context, 0); // 0 no help
      
      mListener = listener;
      mColor = initialColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      IColorChanged l = new IColorChanged() {
        public void colorChanged(int color) {
          mListener.colorChanged(color);
          dismiss();
        }
      };
      
      initLayout( R.layout.my_color_picker, R.string.title_color_picker );


      mBtnOk    = (Button) findViewById( R.id.btn_ok );
      mBtnClear = (Button) findViewById( R.id.btn_clear );
      mBtnClose = (Button) findViewById( R.id.btn_close );
      mBtnClear.setOnClickListener( this );
      mBtnClose.setOnClickListener( this );

      mColorLayout = (LinearLayout) findViewById( R.id.color_layout );
      mGridLayout = (GridLayout) findViewById( R.id.grid_layout );
      if ( TDSetting.mDiscreteColors == 2 ) {
        int w = (int)(TopoDroidApp.mDisplayWidth / 5) - 50;
        int h = (int)(TopoDroidApp.mDisplayHeight / 14) - 30;
        LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 20, 10, 30, 20 );
        mColorLayout.setVisibility( View.GONE );
        mBtnOk.setVisibility( View.GONE );
        for ( int k=0; k<TDColor.mTDColors.length; ++k ) {
          MyColorCell cell = new MyColorCell( mContext, k, this, w, h );
          mGridLayout.addView( cell, lp );
        }
      } else { // TDSetting.mDiscreteColors == 1 
        mBtnOk.setOnClickListener( this );
        LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 10, 10, 20, 20 );
        mGridLayout.setVisibility( View.GONE );
        mColorPicker = new ColorPickerView(getContext(), l, mColor);
        mColorLayout.addView( mColorPicker, lp );
        mColorLayout.invalidate();
      // } else {
      //   dismiss();
      }
    }

    /** react to a user tap
     * @param v tapped view
     *
     * there are two actions:
     *   OK: set the selected color
     *   CLEAR: clear the color
     */
    @Override
    public void onClick( View v )
    {
      if ( v.getId() == R.id.btn_ok ) {
        mListener.colorChanged( mColorPicker.getColor() );
      } else if ( v.getId() == R.id.btn_clear ) {
        mListener.colorChanged( 0 ); // clear color is 0
      // } else if ( v.getId() == R.id.btn_close ) {
          /* nothing */
      } else if ( v instanceof MyColorCell ) {
        MyColorCell cell = (MyColorCell)v;
        int col = TDColor.mTDColors[ cell.getIndex() ];
        mListener.colorChanged( col );
      }
      dismiss();
    }
}
