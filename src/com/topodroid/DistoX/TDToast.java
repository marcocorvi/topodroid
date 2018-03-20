/* @file TDColor.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid colors
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.widget.Toast;
import android.widget.TextView;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.content.Context;

class TDToast
{
  static final private int mBgColor = 0xff6699ff;

  static void make( Context context, int r )
  {
    show( Toast.makeText( context, r, Toast.LENGTH_SHORT ) );
  }

  static void make( Context context, String text )
  {
    show( Toast.makeText( context, text, Toast.LENGTH_SHORT ) );
  }
  
  static Toast makeToast( Context context, int r )
  {
    Toast toast = Toast.makeText( context, r, Toast.LENGTH_SHORT );
    show( toast );
    return toast;
  }

  static void makeLong( Context context, int r )
  {
    show( Toast.makeText( context, r, Toast.LENGTH_LONG ) );
  }

  static void makeLong( Context context, String text )
  {
    show( Toast.makeText( context, text, Toast.LENGTH_LONG ) );
  }

  static void makeBG( Context context, int r, int color )
  {
    Toast toast = Toast.makeText( context, r, Toast.LENGTH_SHORT );
    toast.getView().setBackgroundColor( color );
    toast.setGravity( Gravity.BOTTOM | Gravity.CENTER, 0, 0 );
    toast.show();
  }

  static void makeColor( Context context, int r, int color )
  {
    Toast toast = Toast.makeText( context, r, Toast.LENGTH_SHORT );
    toast.getView().setBackgroundColor( mBgColor );
    toast.setGravity( Gravity.BOTTOM | Gravity.CENTER, 0, 0 );
    TextView tv = (TextView)toast.getView().findViewById( android.R.id.message );
    tv.setTextColor( color );
    toast.show();
  }

  static void makeBG( Context context, String str, int color )
  {
    Toast toast = Toast.makeText( context, str, Toast.LENGTH_SHORT );
    toast.getView().setBackgroundColor( color );
    toast.setGravity( Gravity.BOTTOM | Gravity.CENTER, 0, 0 );
    toast.show();
  }

  static void makeColor( Context context, String str, int color )
  {
    Toast toast = Toast.makeText( context, str, Toast.LENGTH_SHORT );
    toast.getView().setBackgroundColor( mBgColor );
    toast.setGravity( Gravity.BOTTOM | Gravity.CENTER, 0, 0 );
    TextView tv = (TextView)toast.getView().findViewById( android.R.id.message );
    tv.setTextColor( color );
    toast.show();
  }

  static void makeGravity( Context context, String str, int gravity )
  {
    Toast toast = Toast.makeText( context, str, Toast.LENGTH_SHORT );
    toast.getView().setBackgroundColor( mBgColor );
    toast.setGravity( gravity, 10, 10 );
    show( toast );
  }

  static private void show( Toast toast )
  {
    toast.getView().setBackgroundColor( mBgColor );
    toast.setGravity( Gravity.BOTTOM | Gravity.CENTER, 0, 0 );
    // View view = toast.getView();
    // TextView text_view = (TextView) view.findViewById( android.R.id.message );
    // text_view.setBackgroundColor( mBgColor );
    // text_view.setTextColor( 0x66cc99 );
    // text_view.setPadding( 10, 5, 10, 5 );
    toast.show();
  }
}
  
