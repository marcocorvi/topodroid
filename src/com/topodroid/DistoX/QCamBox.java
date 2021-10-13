/* @file QCamBox.java
 *
 * @author marco corvi
 * @date jan. 2017
 *
 * @brief TopoDroid quick cam drawing surface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;

import android.content.Context;

import android.graphics.Canvas;
// import android.graphics.Color;
import android.graphics.Paint;
// import android.graphics.Path;

import android.view.View;

class QCamBox extends View
{
  private Paint mWhite;
  
  QCamBox( Context context )
  {
    super( context );
    mWhite = new Paint();
    mWhite.setColor( 0xffffffff );
    mWhite.setStrokeWidth( 2 );
    mWhite.setStyle(Paint.Style.FILL_AND_STROKE);
  }

  @Override
  protected void onDraw( Canvas canvas )
  {
    super.onDraw( canvas );

    int x = /* canvas. */ getWidth() / 2;
    int y = /* canvas. */ getHeight() / 2;
    int d = x / 4;
    canvas.drawLine( x, 0, x, y-d, mWhite );
    canvas.drawLine( x, y+d, x, 2*y, mWhite );
    canvas.drawLine( 0, y, x-d, y, mWhite );
    canvas.drawLine( x+d, y, 2*x, y, mWhite );
  }
}
