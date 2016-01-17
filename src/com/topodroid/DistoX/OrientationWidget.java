/** @file OrientationWidget.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid orientation widgets
 * --------------------------------------------------------
 *  Copyright: This software is distributed under GPL-3.0 or later
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
    float c = TDMath.cosd( mOrient );
    float s = TDMath.sind( mOrient );
    float c135 = TDMath.cosd( (mOrient+135) );
    float s135 = TDMath.sind( (mOrient+135) );
    float c225 = TDMath.cosd( (mOrient+225) );
    float s225 = TDMath.sind( (mOrient+225) );
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
 
