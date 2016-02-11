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
import android.view.View.OnClickListener;
import android.view.MotionEvent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class MyButton extends Button
{
  Context mContext;
  BitmapDrawable mBitmap;
  BitmapDrawable mBitmap2;

  public MyButton( Context context, OnClickListener click_listener, int size, int res_id, int res_id2 )
  {
    super( context );
    mContext = context;
    setPadding(0,0,0,0);
    setOnClickListener( click_listener );
    mBitmap  = getButtonBackground( mContext, size, res_id );
    mBitmap2 = ( res_id2 > 0 )? getButtonBackground( mContext, size, res_id2 ) : null;
    setBackgroundDrawable( mBitmap );
  }

  // @Override
  // public boolean onTouchEvent( MotionEvent ev )
  // {
  //   int action = ev.getAction();
  //   if ( action == MotionEvent.ACTION_DOWN ) {
  //     setBackgroundDrawable( mBitmap2 );
  //     setBackgroundDrawable( mBitmap );
  //   }
  //   return false;
  // }

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
