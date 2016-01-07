/* @file CutNPaste.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid shot stations cut-n-paste
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.content.res.Resources;

import android.widget.PopupWindow;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.view.View;
import android.view.Gravity;

import android.graphics.Paint.FontMetrics;

import android.util.TypedValue;

class CutNPaste
{
  static String mClipboardText = null;
  static PopupWindow popup = null;
  static EditText    popup_et = null;

  static boolean dismissPopup()
  {
    if ( popup != null ) {
      popup.dismiss();
      popup = null;
      return true;
    }
    return false;
  }

  static void makePopup( final Context context, EditText et )
  {
    if ( popup != null ) {
      popup.dismiss();
      popup = null;
      return;
    }
    popup_et = et;

    LinearLayout layout = new LinearLayout( context );
    layout.setOrientation(LinearLayout.VERTICAL);
    int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
    int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;

    Resources res = context.getResources();
    String cut   = res.getString( R.string.cut );
    String copy  = res.getString( R.string.copy );
    String paste = res.getString( R.string.paste );
    int len = cut.length();
    if ( len < copy.length() ) len = copy.length();
    if ( len < paste.length() ) len = paste.length();

    Button btn_cut = makeButton( context, cut, 0xffffffff, 20 );
    btn_cut.setOnClickListener( new View.OnClickListener( ) {
      public void onClick(View v) {
        if ( popup_et != null ) {
          mClipboardText = popup_et.getText().toString();
          popup_et.setText("");
          String str = String.format( context.getResources().getString( R.string.copied ), mClipboardText );
          Toast t = Toast.makeText( context, str, Toast.LENGTH_SHORT );
          t.setGravity( Gravity.LEFT | Gravity.TOP, 10, 10);
          t.show();
        }
        dismissPopup();
      }
    } );
    Button btn_copy = makeButton( context, copy, 0xffffffff, 20 );
    btn_copy.setOnClickListener( new View.OnClickListener( ) {
      public void onClick(View v) {
        if ( popup_et != null ) {
          mClipboardText = popup_et.getText().toString();
          String str = String.format( context.getResources().getString( R.string.copied ), mClipboardText );
          Toast t = Toast.makeText( context, str, Toast.LENGTH_SHORT );
          t.setGravity( Gravity.LEFT | Gravity.TOP, 10, 10);
          t.show();
        }
        dismissPopup();
      }
    } );
    Button btn_paste = makeButton( context, paste, 0xffffffff, 20 );
    btn_paste.setOnClickListener( new View.OnClickListener( ) {
      public void onClick(View v) {
        if ( mClipboardText != null && popup_et != null ) {
          popup_et.setText( mClipboardText );
        }
        dismissPopup();
      }
    } );

    layout.addView( btn_cut, new LinearLayout.LayoutParams(lHeight, lWidth));
    layout.addView( btn_copy, new LinearLayout.LayoutParams(lHeight, lWidth));
    layout.addView( btn_paste, new LinearLayout.LayoutParams(lHeight, lWidth));

    FontMetrics fm = btn_cut.getPaint().getFontMetrics();
    int w = (int)( Math.abs( ( len + 1 ) * fm.ascent ) * 1.3); // 0.7
    int h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 2.7); // 1.7
    // int h1 = (int)( myTextView0.getHeight() * 7 * 1.1 ); this is 0
    btn_cut.setWidth( w );
    btn_copy.setWidth( w );
    btn_paste.setWidth( w );

    popup = new PopupWindow( layout, w, h );
    popup.showAsDropDown( popup_et );
  }

  static Button makeButton( Context context, String text, int color, int size )
  {
      Button myTextView = new Button( context );
      myTextView.setHeight( 3*size );

      myTextView.setText( text );
      myTextView.setTextColor( color );
      myTextView.setTextSize( TypedValue.COMPLEX_UNIT_DIP, size );
      myTextView.setBackgroundColor( 0xff333333 );
      myTextView.setSingleLine( true );
      myTextView.setGravity( 0x03 ); // left
      myTextView.setPadding( 4, 4, 4, 4 );
      // Log.v(TopoDroidApp.TAG, "makeButton " + text );
      return myTextView;
  }

}
