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

import android.content.Context;
import android.content.res.Resources;

import android.widget.Button;
import android.view.View.OnClickListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.util.SparseArray;

public class MyButton extends Button
{
  static private int mGlobalSize = 42;
  private final static boolean USE_CACHE = true;

  static Bitmap mLVRseekbar = null;

  // static Random rand = new Random();

  // CACHE : using a cache for the BitmapDrawing does not dramatically improve perfoormanaces
  static private SparseArray<BitmapDrawable> mBitmapCache = USE_CACHE ? new SparseArray<BitmapDrawable>()
                                                              : null;

  public static Bitmap getLVRseekbarBackGround( Context ctx, int width, int height )
  {
    if ( mLVRseekbar == null ) {
      // TDLog.v( "width " + width + " height " + height );
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
    if ( size > 0 ) mGlobalSize = size;
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
      // TDLog.v( "My Button cache fail " + res_id );
      try {
        Bitmap bm1 = BitmapFactory.decodeResource( res, res_id );
        Bitmap bmx = Bitmap.createScaledBitmap( bm1, mGlobalSize, mGlobalSize, false );
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
      // TDLog.v( "My Button cache hit " + res_id );
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

  // for TopoGL
  public static BitmapDrawable getButtonBackground( Context c, int size, int res_id )
  {
    Bitmap bm1 = BitmapFactory.decodeResource( c.getResources(), res_id );
    BitmapDrawable bm2 = new BitmapDrawable( c.getResources(), Bitmap.createScaledBitmap( bm1, size, size, false ) );
    return bm2;
  }

  public static void setButtonBackground( Context c, Button b, int size, int res_id )
  {
    b.setBackgroundDrawable( getButtonBackground( c, size, res_id ) );
  }

  // --------------- MyButton intanace:

  private Context mContext;
  private BitmapDrawable mBitmap;
  // BitmapDrawable mBitmap2;
  // OnClickListener mListener;
  private int mSize;
  private float mX, mY;

  public MyButton( Context context, OnClickListener click_listener, int size, int res_id /*, int res_id2 */ )
  {
    super( context );
    mContext = context;
    setPadding(0,0,0,0);
    mSize = size;
    // mListener = click_listener;
    setOnClickListener( click_listener );
        
    mBitmap  = getButtonBackground( mContext, size, res_id );
    // mBitmap2 = ( res_id2 > 0 )? getButtonBackground( mContext, size, res_id2 ) : null;
    setBackgroundDrawable( mBitmap );
  }

  public BitmapDrawable getBitmap() { return mBitmap; }
}
