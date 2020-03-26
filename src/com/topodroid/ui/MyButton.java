/* @file MyButton.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid buttons factory
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.DistoX.TDandroid;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.R;

// import android.util.Log;

import android.content.Context;
import android.content.res.Resources;

import android.widget.Button;
import android.view.View.OnClickListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.util.SparseArray;

public class MyButton
{
  static private int mSize = 42;
  private final static boolean USE_CACHE = true;

  static Bitmap mLVRseekbar = null;

  // static Random rand = new Random();

  // CACHE : using a cache for the BitmapDrawing does not dramatically improve perfoormanaces
  static private SparseArray<BitmapDrawable> mBitmapCache = USE_CACHE ? new SparseArray<BitmapDrawable>()
                                                              : null;

  public static Bitmap getLVRseekbarBackGround( Context ctx, int width, int height )
  {
    if ( mLVRseekbar == null ) {
      // Log.v("DistoX", "width " + width + " height " + height );
      Bitmap bm1 = BitmapFactory.decodeResource( ctx.getResources(), R.drawable.lvrseekbar_background );
      if ( bm1 != null ) {
        mLVRseekbar = Bitmap.createScaledBitmap( bm1, width, height, false );
      }
    }
    return mLVRseekbar;
  }

  // called with context = mApp
  public static void resetCache( /* Context context, */ int size )
  {
    if ( size > 0 ) mSize = size;
    if ( USE_CACHE ) mBitmapCache.clear();
  }

  public static Button getButton( Context ctx, OnClickListener click, int res_id )
  {
    Button ret = new Button( ctx );
    ret.setPadding(10,0,10,0);
    ret.setOnClickListener( click );
    TDandroid.setButtonBackground( ret, getButtonBackground( ctx, ctx.getResources(), res_id ) );
    return ret;
  }

  public static BitmapDrawable getButtonBackground( Context ctx, Resources res, int res_id )
  {
    BitmapDrawable ret = USE_CACHE ? mBitmapCache.get( res_id )
                                   : null;
    if ( ret == null ) {    
      // Log.v("DistoX", "My Button cache fail " + res_id );
      try {
        Bitmap bm1 = BitmapFactory.decodeResource( res, res_id );
        Bitmap bmx = Bitmap.createScaledBitmap( bm1, mSize, mSize, false );
        ret = new BitmapDrawable( res, bmx );
        if ( USE_CACHE ) mBitmapCache.append( res_id, ret );
      } catch ( OutOfMemoryError err ) {
        TDLog.Error("out of memory: " + err.getMessage() );
        TDToast.makeColor( R.string.out_of_memory, TDColor.FIXED_RED );
        // try { 
        //   InputStream is = ctx.getAssets().open("iz_oom.png");
        //   ret = new BitmapDrawable( res, is );
        // } catch ( IOException e ) {
        // }
      }
    // } else {
      // Log.v("DistoX", "My Button cache hit " + res_id );
    }
    return ret;
  }

  // get tentative bitmap (do not use cache)
  public static BitmapDrawable getButtonBackground( Context ctx, Resources res, int res_id, int size )
  {
    try {
      Bitmap bm1 = BitmapFactory.decodeResource( res, res_id );
      Bitmap bmx = Bitmap.createScaledBitmap( bm1, size, size, false );
      return new BitmapDrawable( res, bmx );
    } catch ( OutOfMemoryError err ) {
      TDLog.Error("out of memory: " + err.getMessage() );
      TDToast.makeColor( R.string.out_of_memory, TDColor.FIXED_RED );
    }
    return null;
  }

}
