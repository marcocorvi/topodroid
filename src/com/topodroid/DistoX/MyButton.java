/** @file MyButton.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid buttons
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.util.Log;

public class MyButton extends Button
{
  Context mContext;
  BitmapDrawable mBitmap;
  BitmapDrawable mBitmap2;
  OnClickListener mListener;
  int mSize;
  float mX, mY;

  public MyButton( Context context, OnClickListener click_listener, int size, int res_id, int res_id2 )
  {
    super( context );
    mContext = context;
    setPadding(0,0,0,0);
    mSize = size;
    mListener = click_listener;
    // setOnClickListener( click_listener );
        
    mBitmap  = getButtonBackground2( mContext, size, res_id );
    // mBitmap2 = ( res_id2 > 0 )? getButtonBackground( mContext, size, res_id2 ) : null;
    setBackgroundDrawable( mBitmap );
  }

  @Override
  public boolean onTouchEvent( MotionEvent ev )
  {
    int action = ev.getAction();
    if ( action == MotionEvent.ACTION_DOWN ) {
      mX = ev.getX();
      mY = ev.getY();
      setBackgroundDrawable( mBitmap2 );
      // Log.v("DistoX", "Touch DOWN");
      return true;
    } else if ( action == MotionEvent.ACTION_UP ) {
      setBackgroundDrawable( mBitmap );
      if ( Math.abs( ev.getX() - mX ) < mSize && Math.abs( ev.getY() - mY ) < mSize ) {
        mListener.onClick( this );
      }
    }
    return false;
  }

  private BitmapDrawable getButtonBackground2( Context c, int size, int res_id )
  {
    Bitmap bm1 = BitmapFactory.decodeResource( c.getResources(), res_id );
    Bitmap bmx = Bitmap.createScaledBitmap( bm1, size, size, false );
    int w = bmx.getWidth();
    int h = bmx.getHeight();
    int pxl[] = new int[w*h];
    bmx.getPixels( pxl, 0, w, 0, 0, w, h );
    for ( int k=0; k<w*h; ++k ) pxl[k] |= 0xccff9900;
    Bitmap bmy = Bitmap.createBitmap( pxl, 0, w, w, h, Bitmap.Config.ARGB_8888 );
    mBitmap2 = new BitmapDrawable( c.getResources(), bmy );

    BitmapDrawable bm2 = new BitmapDrawable( c.getResources(), bmx );
    return bm2;
  }


  static BitmapDrawable getButtonBackground( Context c, int size, int res_id )
  {
    Bitmap bm1 = BitmapFactory.decodeResource( c.getResources(), res_id );
    BitmapDrawable bm2 = new BitmapDrawable( c.getResources(), Bitmap.createScaledBitmap( bm1, size, size, false ) );
    return bm2;
  }

  static void setButtonBackground( Context c, Button b, int size, int res_id )
  {
    b.setBackgroundDrawable( getButtonBackground( c, size, res_id ) );
  }

}
