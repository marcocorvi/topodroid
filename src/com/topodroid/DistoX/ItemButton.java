/** @file ItemButton.java
 *
 * @author marco corvi
 * @date dec 2013
 *
 * @brief TopoDroid drawing: button for a symbol item
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.content.Context;

import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Matrix;

import android.widget.Button;

class ItemButton extends Button 
{
  private Paint mPaint;
  private Path  mPath;

  public ItemButton(Context context, Paint paint, Path path, float sx, float sy )
  {
    super(context);
    setBackgroundColor( Color.BLACK );
    setPadding(5, 5, 5, 5 );
    setMinimumWidth( 60 );
    setMinimumHeight( 40 );
    mPaint = paint;
    resetPath( path, sx, sy );
  }

  void resetPath( Path path, float sx, float sy )
  {
    mPath = new Path(path);
    Matrix m = new Matrix();
    m.postScale( sx, sy );
    mPath.transform( m );
    mPath.offset( 30, 20 );
  }

  public void onDraw(Canvas canvas) 
  {
    // draw the button background
    canvas.drawPath( mPath, mPaint );
  }
}

