/** @file MyFileItem.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief Cave3D file list item
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

// import com.topodroid.utils.TDLog;
import java.util.ArrayList;

import android.content.Context;
// import android.app.Dialog;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;

import android.widget.TextView;
import android.widget.Button;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
// import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
// import android.text.Layout;
import android.widget.LinearLayout;

class MyFileItem extends TextView
{
  // TextView     mTextView = null;
  // LinearLayout mView;
  OnClickListener mListener;
  float mX, mY;
  boolean  mTouch;
  boolean  mIsDirectory;

  // private Context mContext;

  public MyFileItem( Context context )
  {  
    super( context );
    setTextColor( 0xff33ccff );
    // setBackgroundColor( 0xff333333 );
    setTextSize( 18 );
    setText( "" );
    setPadding( 10, 10, 5, 10 );
    mListener = null;
    mTouch = false;
    mIsDirectory = false;
  }

  public MyFileItem( Context context, OnClickListener listener, String text, boolean is_dir )
  {  
    super( context );
    // setBackgroundColor( 0xff333333 );
    setTextSize( 18 );
    setPadding( 10, 10, 5, 10 );
    mListener = listener;
    mTouch = false;
    // setOnClickListener( listener );
    mIsDirectory = is_dir;
    if ( mIsDirectory ) {
      setText( "+ " + text );
      setTextColor( 0xffffffff ); // 0xff33ff66 );
      // setTypeface( Typeface.DEFAULT_BOLD );
    } else {
      setText( text );
      setTextColor( 0xff33ccff );
    }
  }

  // void resetBgColor() { setBackgroundColor( 0xff333333 ); }

  void setListener( OnClickListener listener ) { mListener = listener; }

  // @Override
  // public boolean onTouchEvent( MotionEvent ev )
  // {
  //   int action = ev.getAction();
  //   if ( action == MotionEvent.ACTION_DOWN ) {
  //     mX = ev.getX();
  //     mY = ev.getY();
  //     mTouch = true;
  //     setBackgroundColor( 0xccff9900 );
  //     return true;
  //   } else if ( action == MotionEvent.ACTION_MOVE && mTouch ) {
  //     if ( mX - ev.getX() > 10 || Math.abs( mY - ev.getY() ) > 5 ) {
  //       mTouch = false;
  //       setBackgroundColor( 0xff333333 );
  //     }
  //     // TDLog.v( "File item X " + ev.getX() + " Y " + ev.getY() );
  //   } else if ( action == MotionEvent.ACTION_UP ) {
  //     setBackgroundColor( 0xff333333 );
  //     if ( mTouch ) mListener.onClick( this );
  //   }
  //   return false;
  // }

}

