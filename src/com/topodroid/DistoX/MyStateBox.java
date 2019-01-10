/* @file MyStateBox.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid multistate button
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;

// import java.util.ArrayList;

import android.widget.CompoundButton;
// import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.ViewGroup.MarginLayoutParams;
import android.graphics.drawable.BitmapDrawable;

class MyStateBox extends CompoundButton
{
  private Context mContext = null;
  private int mState;
  private BitmapDrawable[] mBG;
  
  MyStateBox( Context context )
  {
    super( context );
    mContext = context;
    mState = 0;
    mBG = null;
  }

  MyStateBox( Context context, int id0, int id1 )
  {
    super( context );
    mContext = context;
    mBG = new BitmapDrawable[2];
    mBG[0] = MyButton.getButtonBackground( mContext, mContext.getResources(), id0 );
    mBG[1] = MyButton.getButtonBackground( mContext, mContext.getResources(), id1 );
    setState( 0 );
  }

  MyStateBox( Context context, int id0, int id1, int id2 )
  {
    super( context );
    mContext = context;
    mBG = new BitmapDrawable[3];
    mBG[0] = MyButton.getButtonBackground( mContext, mContext.getResources(), id0 );
    mBG[1] = MyButton.getButtonBackground( mContext, mContext.getResources(), id1 );
    mBG[2] = MyButton.getButtonBackground( mContext, mContext.getResources(), id2 );
    setState( 0 );
  }

  public void setState( int s )
  {
    if ( s >= 0 && s < mBG.length ) {
      mState = s;
      setBackground( mBG[mState] );
    }
  }

  public int getState() { return mState; }

}




