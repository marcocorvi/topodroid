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
package com.topodroid.ui;

import com.topodroid.Cave3X.TDandroid;

import android.content.Context;

import android.widget.CompoundButton;
import android.graphics.drawable.BitmapDrawable;

public class MyStateBox extends CompoundButton
{
  private Context mContext = null;
  private int mState;
  private BitmapDrawable[] mBG;
  
  public MyStateBox( Context context )
  {
    super( context );
    mContext = context;
    mState = 0;
    mBG = null;
  }

  public MyStateBox( Context context, int id0, int id1 )
  {
    super( context );
    mContext = context;
    mBG = new BitmapDrawable[2];
    mBG[0] = MyButton.getButtonBackground( mContext, mContext.getResources(), id0 );
    mBG[1] = MyButton.getButtonBackground( mContext, mContext.getResources(), id1 );
    setState( 0 );
  }

  public MyStateBox( Context context, int id0, int id1, int id2 )
  {
    super( context );
    mContext = context;
    mBG = new BitmapDrawable[3];
    mBG[0] = MyButton.getButtonBackground( mContext, mContext.getResources(), id0 );
    mBG[1] = MyButton.getButtonBackground( mContext, mContext.getResources(), id1 );
    mBG[2] = MyButton.getButtonBackground( mContext, mContext.getResources(), id2 );
    setState( 0 );
  }

  public MyStateBox( Context context, int id0, int id1, int id2, int id3 )
  {
    super( context );
    mContext = context;
    mBG = new BitmapDrawable[4];
    mBG[0] = MyButton.getButtonBackground( mContext, mContext.getResources(), id0 );
    mBG[1] = MyButton.getButtonBackground( mContext, mContext.getResources(), id1 );
    mBG[2] = MyButton.getButtonBackground( mContext, mContext.getResources(), id2 );
    mBG[3] = MyButton.getButtonBackground( mContext, mContext.getResources(), id3 );
    setState( 0 );
  }

  public void setState( int s )
  {
    if ( s >= 0 && s < mBG.length ) {
      mState = s;
      TDandroid.setButtonBackground( this, mBG[mState] );
    }
  }

  public int getState() { return mState; }

  public int getNrStates() { return (mBG == null)? 0 : mBG.length; }

}




