/* @file TDColor.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid colors
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Build;

import android.annotation.SuppressLint;
import android.widget.Toast;
import android.widget.TextView;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
// import android.view.ViewGroup;
// import android.view.LayoutInflater;

// import android.content.Context;

class TDToast
{
  static final private int mFgColor = 0xff6699ff;
  static final private int mBgDrawable = R.drawable.toast_bg; // 0xff333333;
  static final private int mBgColor = 0xff333333;

  static final private int mGravity = Gravity.BOTTOM | Gravity.CENTER | Gravity.FILL_HORIZONTAL;
  static final private int SHORT    = Toast.LENGTH_SHORT;
  static final private int LONG     = Toast.LENGTH_LONG;

  @SuppressLint("ShowToast")
  static void make( int r ) { show( Toast.makeText( TDInstance.context, r, SHORT ) ); }

  @SuppressLint("ShowToast")
  static void makeBad( int r ) { makeBG( r, TDColor.VIOLET ); }
  static void makeWarn( int r ) { makeBG( r, TDColor.ORANGE ); }

  @SuppressLint("ShowToast")
  static void make( String text ) { show( Toast.makeText( TDInstance.context, text, SHORT ) ); }
  
  @SuppressLint("ShowToast")
  static void makeBad( String text ) { makeBG( text, TDColor.VIOLET ); }
  static void makeWarn( String text ) { makeBG( text, TDColor.ORANGE ); }
  
  static Toast makeToast( int r )
  {
    Toast toast = Toast.makeText( TDInstance.context, r, SHORT );
    show( toast );
    return toast;
  }

  @SuppressLint("ShowToast")
  static void makeLong( int r ) { show( Toast.makeText( TDInstance.context, r, LONG ) ); }

  @SuppressLint("ShowToast")
  static void makeLong( String text ) { show( Toast.makeText( TDInstance.context, text, LONG ) ); }

  static void makeBG( int r, int color )
  {
    Toast toast = Toast.makeText( TDInstance.context, r, SHORT );
    getView( toast, color );
    toast.setGravity( mGravity, 0, 0 );
    toast.show();
  }

  static void makeColor( int r, int color )
  {
    Toast toast = Toast.makeText( TDInstance.context, r, SHORT );
    View view = getView( toast );
    toast.setGravity( mGravity, 0, 0 );
    TextView tv = (TextView)view.findViewById( android.R.id.message );
    tv.setTextColor( color );
    toast.show();
  }

  static void makeBG( String str, int color )
  {
    Toast toast = Toast.makeText( TDInstance.context, str, SHORT );
    View view = getView( toast, color );
    toast.setGravity( mGravity, 0, 0 );
    toast.show();
  }

  static void makeColor( String str, int color )
  {
    Toast toast = Toast.makeText( TDInstance.context, str, SHORT );
    View view = getView( toast, color );
    toast.setGravity( mGravity, 0, 0 );
    TextView tv = (TextView)view.findViewById( android.R.id.message );
    tv.setTextColor( color );
    toast.show();
  }

  static void makeGravity( String str, int gravity )
  {
    Toast toast = Toast.makeText( TDInstance.context, str, SHORT );
    View view = getView( toast );
    toast.setGravity( gravity, 10, 10 );
    toast.show();
  }

  // ---------------------------------------------------------------------
  
  static private View getView( Toast toast )
  {
    View view = toast.getView();
    view.setOnClickListener( new OnClickListener() { public void onClick( View v ) { v.setVisibility( View.GONE ); } } );
    if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.O ) {
      view.setBackgroundResource( mBgDrawable );
    } else {
      // view.setBackgroundColor( mBgColor );
    }
    // view.setClipToOutline( true );
    TextView tv = (TextView)view.findViewById( android.R.id.message );
    tv.setTextColor( mFgColor );
    return view;
  }

  static private View getView( Toast toast, int color )
  {
    View view = toast.getView();
    view.setOnClickListener( new OnClickListener() { public void onClick( View v ) { v.setVisibility( View.GONE ); } } );
    if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.O ) {
      view.setBackgroundResource( mBgDrawable );
    } else {
      // view.setBackgroundColor( mBgColor );
    }
    // view.setClipToOutline( true );
    TextView tv = (TextView)view.findViewById( android.R.id.message );
    tv.setTextColor( color );
    return view;
  }

  static private void show( Toast toast )
  {
    View view = getView( toast );
    toast.setGravity( mGravity, 0, 0 );
    toast.show();
  }
}
  
