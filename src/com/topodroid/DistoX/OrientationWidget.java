/** @file OrientationWidget.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid orientation widgets
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Dialog;

import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import android.util.FloatMath;


class OrientationWidget
{
  private SeekBar  mSeekBar;
  private ImageView mIVorientation;
  private Bitmap mBitmap = null;
  private Canvas mCanvas = null;

  int mOrient;

  public OrientationWidget( Dialog parent, boolean orientable, double orient )
  {
    mOrient = (int)orient;

    mSeekBar  = (SeekBar) parent.findViewById( R.id.seekbar );
    mIVorientation = (ImageView) parent.findViewById( R.id.image );

    if ( orientable ) {
      mBitmap = Bitmap.createBitmap( 40, 40, Bitmap.Config.ARGB_8888);
      mCanvas = new Canvas( mBitmap );
      mIVorientation.setImageBitmap( mBitmap );
      drawOrientation();
      mSeekBar.setProgress( ( mOrient+180)%360 );

      mSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
          if ( fromUser ) {
            mOrient = 180 + progress;
            if ( mOrient >= 360 ) mOrient -= 360;
            drawOrientation();
          }
        }
        public void onStartTrackingTouch(SeekBar seekbar) { }
        public void onStopTrackingTouch(SeekBar seekbar) { }
      } );
      mSeekBar.setMax( 360 );
    } else {
      mIVorientation.setVisibility( View.GONE );
      mSeekBar.setVisibility( View.GONE );
    }

  }

  private void drawOrientation()
  {
    int d = 20;
    // mTVorientation.setText( Integer.toString(mOrient) );
    mCanvas.drawColor( 0xff000000 );
    float c = FloatMath.cos( mOrient * TopoDroidUtil.GRAD2RAD);
    float s = FloatMath.sin( mOrient * TopoDroidUtil.GRAD2RAD);
    float c135 = FloatMath.cos( (mOrient+135) * TopoDroidUtil.GRAD2RAD);
    float s135 = FloatMath.sin( (mOrient+135) * TopoDroidUtil.GRAD2RAD);
    float c225 = FloatMath.cos( (mOrient+225) * TopoDroidUtil.GRAD2RAD);
    float s225 = FloatMath.sin( (mOrient+225) * TopoDroidUtil.GRAD2RAD);
    float x1 = d+d*s;
    float y1 = d-d*c;
    Paint paint = DrawingBrushPaths.fixedBluePaint;
    mCanvas.drawLine( d-d*s, d+d*c, x1, y1, paint );
    mCanvas.drawLine( x1, y1, x1+10*s135, y1-10*c135, paint );
    mCanvas.drawLine( x1, y1, x1+10*s225, y1-10*c225, paint );
    mIVorientation.setImageBitmap( mBitmap );
    mIVorientation.invalidate();
  }

}
 
