/* @file TDToast.java
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

import com.topodroid.utils.TDColor;

import android.annotation.SuppressLint;
import android.widget.Toast;
import android.widget.TextView;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
// import android.view.ViewGroup;
// import android.view.LayoutInflater;

// import android.content.Context;

public class TDToast
{
  static final private int mBgDrawable = R.drawable.toast_bg; // 0xff333333;
  static final private int mBgColor = 0xff333333;

  static final private int mGravity = Gravity.BOTTOM | Gravity.CENTER | Gravity.FILL_HORIZONTAL;
  static final private int SHORT    = Toast.LENGTH_SHORT;
  static final private int LONG     = Toast.LENGTH_LONG;

  @SuppressLint("ShowToast")
  public static void make( int r ) { show( Toast.makeText( TDInstance.context, r, SHORT ) ); }

  @SuppressLint("ShowToast")
  public static void makeBad( int r )  { makeBG( r, TDColor.TOAST_ERROR ); }
  public static void makeWarn( int r ) { makeBG( r, TDColor.TOAST_WARNING ); }

  @SuppressLint("ShowToast")
  public static void make( String text ) { show( Toast.makeText( TDInstance.context, text, SHORT ) ); }
  
  @SuppressLint("ShowToast")
  public static void makeBad( String text )  { makeBG( text, TDColor.TOAST_ERROR ); }
  public static void makeWarn( String text ) { makeBG( text, TDColor.TOAST_WARNING ); }
  
  public static Toast makeToast( int r )
  {
    Toast toast = Toast.makeText( TDInstance.context, r, SHORT );
    show( toast );
    return toast;
  }

  @SuppressLint("ShowToast")
  public static void makeLong( int r ) { show( Toast.makeText( TDInstance.context, r, LONG ) ); }

  @SuppressLint("ShowToast")
  public static void makeLong( String text ) { show( Toast.makeText( TDInstance.context, text, LONG ) ); }

  public static void makeBG( int r, int color )
  {
    Toast toast = Toast.makeText( TDInstance.context, r, SHORT );
    getView( toast, color );
    toast.setGravity( mGravity, 0, 0 ); // ANDROID-11 no-op
    toast.show();
  }

  public static void makeColor( int r, int color )
  {
    Toast toast = Toast.makeText( TDInstance.context, r, SHORT );
    View view = getView( toast );  // ANDROID-11 returns null
    toast.setGravity( mGravity, 0, 0 ); // ANDROID-11 no-op
    if ( view != null ) {
      TextView tv = (TextView)view.findViewById( android.R.id.message );
      tv.setTextColor( color );
    }
    toast.show();
  }

  public static void makeBG( String str, int color )
  {
    Toast toast = Toast.makeText( TDInstance.context, str, SHORT );
    getView( toast, color ); // ANDROID-11 return null
    toast.setGravity( mGravity, 0, 0 );  // ANDROID-11 no-op
    toast.show();
  }

  public static void makeColor( String str, int color )
  {
    Toast toast = Toast.makeText( TDInstance.context, str, SHORT );
    View view = getView( toast, color ); // ANDROID-11 return null
    toast.setGravity( mGravity, 0, 0 );  // ANDROID-11 no-op
    if ( view != null ) {
      TextView tv = (TextView)view.findViewById( android.R.id.message );
      tv.setTextColor( color );
    }
    toast.show();
  }

  public static void makeGravity( String str, int gravity )
  {
    Toast toast = Toast.makeText( TDInstance.context, str, SHORT );
    getView( toast );
    toast.setGravity( gravity, 10, 10 ); // ANDROID-11 no-op
    toast.show();
  }

  // ---------------------------------------------------------------------
  
  static private View getView( Toast toast )
  {
    View view = toast.getView(); // ANDROID-11 returns null
    if ( view != null ) {
      view.setOnClickListener( new OnClickListener() { public void onClick( View v ) { v.setVisibility( View.GONE ); } } );
      if ( TDandroid.ABOVE_API_26 ) { // Android-8 (O) 
        view.setBackgroundResource( mBgDrawable );
      } else if ( TDandroid.BELOW_API_23 ) { // Android-6 (M) 
        view.setBackgroundColor( mBgColor );
      }
      // view.setClipToOutline( true );
      TextView tv = (TextView)view.findViewById( android.R.id.message );
      tv.setTextColor( TDColor.TOAST_NORMAL );
    }
    return view;
  }

  static private View getView( Toast toast, int color )
  {
    View view = toast.getView(); // ANDROID-11 returns null
    if ( view != null ) {
      view.setOnClickListener( new OnClickListener() { public void onClick( View v ) { v.setVisibility( View.GONE ); } } );
      if ( TDandroid.ABOVE_API_26 ) {
        view.setBackgroundResource( mBgDrawable );
      } else if ( TDandroid.BELOW_API_23 ) {
        view.setBackgroundColor( mBgColor );
      }
      // view.setClipToOutline( true );
      TextView tv = (TextView)view.findViewById( android.R.id.message );
      tv.setTextColor( color );
    }
    return view;
  }

  static private void show( Toast toast )
  {
    getView( toast );
    toast.setGravity( mGravity, 0, 0 ); // ANDROID-11 no-op
    toast.show();
  }
}
  
