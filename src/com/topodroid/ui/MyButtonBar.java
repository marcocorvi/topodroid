/* @file MyButtonBar.java
 *
 * @author marco corvi
 * @date jul 2026
 *
 * @brief TopoDroid drawing: button bar
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

import com.topodroid.util.TDColor;
import com.topodroid.util.TDLog;
// import com.topodroid.prefs.TDSetting;

import android.annotation.SuppressLint;
import android.content.Context;

// import android.graphics.Paint;
// import android.graphics.Canvas;
// import android.graphics.Path;
// import android.graphics.Matrix;

import android.widget.Button;

import android.view.View;

/**
 * note this class must be public
 */
@SuppressLint("AppCompatCustomView")
public class MyButtonBar implements View.OnClickListener
{
  public interface IButtonBarListener
  {
    /**
     * @param idx     button index
     * @param on_off  true if click made button active, false if it made it non-active
     */
    public void onButtonBarClick( int idx, boolean on_off );
  }

  private Button[] mBtn;
  private int mActive = -1;
  private int mBtnNr;
  private IButtonBarListener mListener = null;

  /** "first default" cstr
   * @param context  context
   * @param listener
   * @param btn      button array
   * @param active   index of active button
   */
  public MyButtonBar( Context context, IButtonBarListener listener, Button[] btn, int active )
  {
    mBtn    = btn;
    mBtnNr  = btn.length;
    mActive = active;
    if ( mActive < 0 || mActive >= mBtnNr ) mActive = mBtnNr / 2;
    mListener = listener;
    for ( int k=0; k < mBtnNr; ++ k ) {
      mBtn[k].setOnClickListener( this );
      if ( k == mActive ) { 
        setButtonActive( k );
      } else {
        setButtonNonActive( k );
      }
    }
  }

  /** set the active button
   * @param k index of the active button
   */
  public void setActive( int k ) 
  {
    if ( k < 0 || k >= mBtnNr ) return;
    if ( mActive == k ) {
      // mActive = -1;
      // setButtonNonActive( k );
    } else {
      if ( mActive != -1 ) {
        setButtonNonActive( mActive );
      }
      mActive = k;
      setButtonActive( mActive );
    }
  }

  /** @return the active index (-1 if no button active)
   */
  public int getActive() { return mActive; }

  /** set button active colors
   * @param k button index
   */
  private void setButtonActive( int k )
  {
    if ( k < 0 || k >= mBtnNr ) return;
    mBtn[k].setBackgroundColor( TDColor.TEXT_ON_BG );
    mBtn[k].setTextColor( TDColor.TEXT_ON_FG );
  }

  /** set button non-active colors
   * @param k button index
   */
  private void setButtonNonActive( int k )
  {
    if ( k < 0 || k >= mBtnNr ) return;
    mBtn[k].setBackgroundColor( TDColor.TEXT_OFF_BG );
    mBtn[k].setTextColor( TDColor.TEXT_OFF_FG );
  }

  @Override
  public void onClick( View v )
  {
    if ( v instanceof Button ) {
      long id = v.getId();
      for ( int k = 0; k < mBtnNr; ++k ) {
        if ( id == mBtn[k].getId() ) {
          if ( mActive == k ) {
            // mActive = -1;
            // setButtonNonActive( k );
            if ( mListener != null ) mListener.onButtonBarClick( k, false );
          } else {
            if ( mActive != -1 ) {
              setButtonNonActive( mActive );
            }
            mActive = k;
            setButtonActive( mActive );
            if ( mListener != null ) mListener.onButtonBarClick( k, true );
          }
          break;
        }
      }
    }
  }

}
        

