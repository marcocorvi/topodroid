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
import android.view.View.OnLongClickListener;
import android.view.MotionEvent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

// import android.util.SparseArray;
import android.util.Log;

public class MyButton extends Button
{
  Context mContext;
  BitmapDrawable mBitmap;

  // HOVER
  // BitmapDrawable mBitmap2;
  // OnClickListener mClick;
  // OnLongClickListener mLongClick;
  // int mSize;
  // float mX, mY;
  // long mMillis;
  
  // CACHE
  // using a cache for the BitmapDrawing does not dramatically improve perfoormanaces
  // static SparseArray<BitmapDrawable> mCache = new SparseArray<BitmapDrawable>();
  // static void clearCache() { mCache.clear(); }

  public MyButton( Context context )
  {
    super( context );
    mContext = context;
    setPadding(0,0,0,0);
    mBitmap = null;
    // HOVER
    // mSize   = 0;
    // mClick  = null;
  }

  public MyButton( Context context, OnClickListener click, int size, int res_id, int res_id2 )
  {
    super( context );
    mContext = context;
    setPadding(0,0,0,0);
    // HOVER
    // mSize = size;
    // mClick = click;
    // mLongClick = null;
    setOnClickListener( click );

    mBitmap  = getButtonBackground2( mContext, size, res_id );
    // mBitmap2 = ( res_id2 > 0 )? getButtonBackground( mContext, size, res_id2 ) : null;
    setBackgroundDrawable( mBitmap );
  }

  // HOVER
  // @Override 
  // public void setOnLongClickListener( OnLongClickListener listener ) { mLongClick = listener; }

  // HOVER
  // @Override
  // public boolean onTouchEvent( MotionEvent ev )
  // {
  //   int action = ev.getAction();
  //   if ( action == MotionEvent.ACTION_DOWN ) {
  //     mX = ev.getX();
  //     mY = ev.getY();
  //     mMillis = ev.getEventTime();
  //     setBackgroundDrawable( mBitmap2 );
  //     return true;
  //   } else if ( action == MotionEvent.ACTION_UP ) {
  //     setBackgroundDrawable( mBitmap );
  //     if ( Math.abs( ev.getX() - mX ) < mSize && Math.abs( ev.getY() - mY ) < mSize ) {
  //       boolean consumed = false;
  //       if ( mLongClick != null && ev.getEventTime() - mMillis > 400 ) {
  //         consumed = mLongClick.onLongClick( this );
  //       } 
  //       if ( ! consumed ) mClick.onClick( this );
  //     }
  //   }
  //   return false;
  // }

  private BitmapDrawable getButtonBackground2( Context c, int size, int res_id )
  {
    // CACHE
    // BitmapDrawable ret = mCache.get( res_id );
    // if ( ret != null ) {
    //   mBitmap2 = mCache.get( -res_id );
    //   return ret;
    // }
        
    Bitmap bm1 = BitmapFactory.decodeResource( c.getResources(), res_id );
    Bitmap bmx = Bitmap.createScaledBitmap( bm1, size, size, false );

    // HOVER
    // int w = bmx.getWidth();
    // int h = bmx.getHeight();
    // int pxl[] = new int[w*h];
    // bmx.getPixels( pxl, 0, w, 0, 0, w, h );
    // for ( int k=0; k<w*h; ++k ) pxl[k] |= 0xccff9900;
    // Bitmap bmy = Bitmap.createBitmap( pxl, 0, w, w, h, Bitmap.Config.ARGB_8888 );
    // mBitmap2 = new BitmapDrawable( c.getResources(), bmy );

    return new BitmapDrawable( c.getResources(), bmx );
    // CACHE
    // ret = new BitmapDrawable( c.getResources(), bmx );
    // mCache.append( -res_id, mBitmap2 );
    // mCache.append( res_id, ret );
    // return ret;
  }

  static BitmapDrawable getButtonBackground( Context c, int size, int res_id )
  {
    // CACHE
    // BitmapDrawable ret = mCache.get( res_id );
    // if ( ret != null ) return ret;
    Bitmap bm1 = BitmapFactory.decodeResource( c.getResources(), res_id );
    return new BitmapDrawable( c.getResources(), Bitmap.createScaledBitmap( bm1, size, size, false ) );
    // CACHE
    // ret = new BitmapDrawable( c.getResources(), Bitmap.createScaledBitmap( bm1, size, size, false ) );
    // mCache.append( res_id, ret );
    // return ret;
  }

  static void setButtonBackground( Context c, Button b, int size, int res_id )
  {
    b.setBackgroundDrawable( getButtonBackground( c, size, res_id ) );
  }

}
