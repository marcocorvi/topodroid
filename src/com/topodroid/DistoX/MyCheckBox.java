/* @file MyCheckBox.java
 *
 * @author marco corvi
 * @date nsept 2015
 *
 * @brief TopoDroid checkbox button
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;

import android.util.Log;

/**
 */
public class MyCheckBox extends CompoundButton
{
  Context mContext = null;
  int mIdOn;
  int mIdOff;
  int mSize;
  boolean mState;

  public MyCheckBox( Context context )
  {
    super( context );
    mContext = context;
    mIdOn  = 0;
    mIdOff = 0;
    mSize  = 0;
    mState = false ; // state;
    init();
  }

  public MyCheckBox( Context context, int size, int id_on, int id_off )
  {
    super( context );

    // FIXME how to add margins ?
    // MarginLayoutParams params = new MarginLayoutParams( size+10, size+10 );
    // params.setMargins( 10, 0, 10, 0 );
    // setLayoutParams( params );
    // setMinimumWidth( size+20 );
    // // setMinimumHeight( size );
    // requestLayout();

    mContext = context;
    mIdOn  = id_on;
    mIdOff = id_off;
    mSize  = size;
    mState = false ; // state;
    init();
  }

  void init() 
  {
    setOnCheckedChangeListener( new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged( CompoundButton b, boolean status )
      {
        b.setBackgroundDrawable( MyButton.getButtonBackground( mContext, mContext.getResources(), (status ? mIdOn : mIdOff) ) );
      }
    } );

    setOnClickListener( new OnClickListener() {
      @Override
      public void onClick( View v )
      {
        // Log.v("DistoX", "MyCheckBox on click ");
        toggleState();
        // setState( isChecked() );
      }
    } );

    this.setBackgroundDrawable( MyButton.getButtonBackground( mContext, mContext.getResources(), (mState ? mIdOn : mIdOff) ) );
  }

  @Override
  public boolean isChecked() { return mState; }

  public void toggleState()
  {
    setState( ! mState );
  }

  public void setState( boolean state )
  {
    mState = state;
    this.setBackgroundDrawable( MyButton.getButtonBackground( mContext, mContext.getResources(), (mState ? mIdOn : mIdOff) ) );
  }

}

