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

package com.topodroid.DistoX;

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

class MyColorPicker extends MyDialog
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
    void colorChanged(int color);
  }

  private IColorChanged mListener;
  private int mColor;
  private ColorPickerView mColorPicker;

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
      
      private int floatToByte(float x) { return (int)( Math.round(x) ); }
      private int pinToByte(int n) { return ( n < 0 )? 0 : ( n > 255 ) ? 255 : n; }
      
      private int ave(int s, int d, float p) { return s + Math.round(p * (d - s)); }
      
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
      mBtnOk.setOnClickListener( this );
      mBtnClear.setOnClickListener( this );
      mBtnClose.setOnClickListener( this );

      LinearLayout layout = (LinearLayout) findViewById( R.id.color_layout );
      LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 10, 10, 20, 20 );
      mColorPicker = new ColorPickerView(getContext(), l, mColor);
      layout.addView( mColorPicker, lp );
      layout.invalidate();
    }

    @Override
    public void onClick( View v )
    {
      Button b = (Button)v;
      if ( b == mBtnOk ) {
        mListener.colorChanged( mColorPicker.getColor() );
      } else if ( b == mBtnClear ) {
        mListener.colorChanged( 0 ); // clear color is 0
      } else if ( b == mBtnClose ) {
      }
      dismiss();
    }
}
