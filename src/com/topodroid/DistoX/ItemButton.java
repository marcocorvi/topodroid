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
 */
package com.topodroid.DistoX;

import android.content.Context;

import android.graphics.Paint;
// import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Matrix;

import android.widget.Button;

public class ItemButton extends Button 
{
  private Paint mPaint;
  private Path  mPath;
  // private Rect  mClip;

  public ItemButton( Context context )
  {
    super( context );
    setDefault();
  }

  public ItemButton( Context context, android.util.AttributeSet attr )
  {
    super( context, attr );
    setDefault();
  }

  public ItemButton( Context context, android.util.AttributeSet attr, int a )
  {
    super( context, attr, a );
    setDefault();
  }

  // public ItemButton( Context context, android.util.AttributeSet attr, int a, int b )
  // {
  //   super( context, attr, a, b );
  //   setDefault();
  // }

  private void setDefault()
  {
    setBackgroundColor( TDColor.BLACK );
    setPadding(5, 5, 5, 5 );
    mPath  = null;
    mPaint = null;
  }

  public ItemButton(Context context, Paint paint, Path path, float sx, float sy )
  {
    super(context);
    setBackgroundColor( TDColor.BLACK );
    setPadding(5, 5, 5, 5 );
    reset( paint, path, sx, sy );
  }

  public ItemButton(Context context, Paint paint, Path path, float sx, float sy, int pad )
  {
    super(context);
    setBackgroundColor( TDColor.BLACK );
    setPadding(pad, pad, pad, pad );
    reset( paint, path, sx, sy );
  }

  public void reset(Paint paint, Path path, float sx, float sy )
  {
    setMinimumWidth( (int)(40*sx) );
    setMinimumHeight( (int)(30*sy) );
    mPaint = paint;
    resetPath( path, sx, sy );
    // mClip = new Rect( 0, 0, (int)(40*sx), (int)(30*sy) );
  }

  void resetPath( Path path, float sx, float sy )
  {
    mPath = new Path(path);
    Matrix m = new Matrix();
    m.setScale( sx, sy );
    mPath.transform( m );
    mPath.offset( 20*sx, 15*sy );
  }

  public void onDraw(Canvas canvas) 
  {
    // draw the button background
    if ( mPath != null ) {
      // canvas.clipRect( mClip, Region.Op.REPLACE );
      canvas.drawPath( mPath, mPaint );
    }
  }
}

