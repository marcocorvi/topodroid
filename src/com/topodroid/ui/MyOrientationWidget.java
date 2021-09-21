/* @file MyOrientationWidget.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid orientation widgets
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

import com.topodroid.utils.TDMath;
import com.topodroid.Cave3X.BrushManager;
import com.topodroid.Cave3X.R;

import android.app.Dialog;

import android.widget.ImageView;
import android.widget.SeekBar;
// import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class MyOrientationWidget
{
  // private SeekBar  mSeekBar;
  private ImageView mIVorientation;
  private Bitmap mBitmap = null;
  private Canvas mCanvas = null;

  public int mOrient;

  public MyOrientationWidget( Dialog parent, boolean orientable, double orient )
  {
    mOrient = (int)orient;

    SeekBar seekBar  = (SeekBar) parent.findViewById( R.id.seekbar );
    mIVorientation = (ImageView) parent.findViewById( R.id.image );

    if ( orientable ) {
      mBitmap = Bitmap.createBitmap( 40, 40, Bitmap.Config.ARGB_8888);
      mCanvas = new Canvas( mBitmap );
      mIVorientation.setImageBitmap( mBitmap );
      drawOrientation();
      seekBar.setProgress( ( mOrient+180)%360 );

      seekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
          if ( fromUser ) {
            // mOrient = 180 + progress; if ( mOrient >= 360 ) mOrient -= 360;
            mOrient = TDMath.add180( progress );
            drawOrientation();
          }
        }
        public void onStartTrackingTouch(SeekBar seekbar) { }
        public void onStopTrackingTouch(SeekBar seekbar) { }
      } );
      seekBar.setMax( 360 );
    } else {
      mIVorientation.setVisibility( View.GONE );
      seekBar.setVisibility( View.GONE );
    }

  }

  private void drawOrientation()
  {
    int d = 20;
    // mTVorientation.setText( Integer.toString(mOrient) );
    mCanvas.drawColor( 0xff000000 );
    float c = TDMath.cosd( mOrient );
    float s = TDMath.sind( mOrient );
    float c135 = TDMath.cosd( (mOrient+135) );
    float s135 = TDMath.sind( (mOrient+135) );
    float c225 = TDMath.cosd( (mOrient+225) );
    float s225 = TDMath.sind( (mOrient+225) );
    float x1 = d+d*s;
    float y1 = d-d*c;
    Paint paint = BrushManager.fixedBluePaint;
    mCanvas.drawLine( d-d*s, d+d*c, x1, y1, paint );
    mCanvas.drawLine( x1, y1, x1+10*s135, y1-10*c135, paint );
    mCanvas.drawLine( x1, y1, x1+10*s225, y1-10*c225, paint );
    mIVorientation.setImageBitmap( mBitmap );
    mIVorientation.invalidate();
  }

}
 
