/** @file MyButton.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid buttons factory
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.content.res.Resources;

import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
// import android.view.View.OnLongClickListener;
// import android.view.MotionEvent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.util.SparseArray;
import android.util.Log;

public class MyButton
{
  static int mSize = 42;

  // CACHE : using a cache for the BitmapDrawing does not dramatically improve perfoormanaces
  // static SparseArray<BitmapDrawable> mBitmapCache = new SparseArray<BitmapDrawable>();

  // called with context = mApp
  static void resetCache( /* Context context, */ int size )
  {
    mSize    = size;
    // mBitmapCache.clear();
  }

  static Button getButton( Context ctx, OnClickListener click, int res_id )
  {
    Button ret = new Button( ctx );
    ret.setPadding(0,0,0,0);
    ret.setOnClickListener( click );
    ret.setBackgroundDrawable( getButtonBackground( ctx.getResources(), res_id ) );
    return ret;
  }

  static BitmapDrawable getButtonBackground( Resources res, int res_id )
  {
    BitmapDrawable ret = null;
    // ret = mBitmapCache.get( res_id );
    if ( ret == null ) {    
      Bitmap bm1 = BitmapFactory.decodeResource( res, res_id );
      Bitmap bmx = Bitmap.createScaledBitmap( bm1, mSize, mSize, false );
      ret = new BitmapDrawable( res, bmx );
      // mBitmapCache.append( res_id, ret );
    }
    return ret;
  }

}
