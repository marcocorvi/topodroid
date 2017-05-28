/* @file CutNPaste.java
 *
 * @author marco corvi
 * @date dec 2015
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Gravity;
import android.view.MotionEvent;

import android.graphics.Paint.FontMetrics;

import android.util.TypedValue;

class CutNPaste
{
  final static int BUTTON_HEIGHT = 22;

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

    Button btn_cut = makePopupButton( context, cut, layout, lWidth, lHeight,
      new View.OnClickListener( ) {
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
    Button btn_copy = makePopupButton( context, copy, layout, lWidth, lHeight,
      new View.OnClickListener( ) {
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
    Button btn_paste = makePopupButton( context, paste, layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          if ( mClipboardText != null && popup_et != null ) {
            popup_et.setText( mClipboardText );
          }
          dismissPopup();
        }
      } );

    FontMetrics fm = btn_cut.getPaint().getFontMetrics();
    int w = (int)( Math.abs( len * fm.ascent ) * 1.3); // 0.7
    int h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 2.7); // 1.7
    // int h1 = (int)( textview0.getHeight() * 7 * 1.1 ); this is 0
    btn_cut.setWidth( w );
    btn_copy.setWidth( w );
    btn_paste.setWidth( w );

    popup = new PopupWindow( layout, w, h );
    popup.showAsDropDown( popup_et );
  }

  static Button makeButton( Context context, String text, int color, int size )
  {
    Button button = new Button( context );
    // button.set???( R.layout.popup_item );

    // THIS CRASHES THE APP
    // button.setBackgroundResource( R.drawable.popup_bgcolor );
    button.setTextColor( color );
    button.setBackgroundColor( 0xff333333 );

    button.setHeight( 3*size );
    button.setText( text );
    button.setTextSize( TypedValue.COMPLEX_UNIT_DIP, size );
    button.setSingleLine( true );
    button.setGravity( 0x03 ); // left
    button.setPadding( 4, 4, 4, 4 );
    return button;
  }

  static Button makePopupButton( Context context, String text,
                                 LinearLayout layout, int w, int h, View.OnClickListener listener )
  {
    Button button = makeButton( context, text, 0xffffffff, BUTTON_HEIGHT );
    layout.addView( button, new LinearLayout.LayoutParams(h, w));
    button.setOnClickListener( listener );
    button.setOnTouchListener( new View.OnTouchListener( ) {
      @Override public boolean onTouch( View v, MotionEvent ev ) { v.setBackgroundColor( 0xffff6600 ); return false; }
    } );
    return button;
  }

  static PopupWindow mPopupBT = null;

  /** show BT popup under button b
   * @param b button
   */
  static PopupWindow showPopupBT( final Context context, ILister ilister, final TopoDroidApp app, View b, boolean gm_data )
  {
    final ListerHandler lister = new ListerHandler( ilister );
    LinearLayout popup_layout  = new LinearLayout( context );
    popup_layout.setOrientation(LinearLayout.VERTICAL);
    int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
    int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;

    Resources res = context.getResources();
    // ----- RESET BT
    //
    String text = res.getString(R.string.remote_reset);
    int len = text.length();
    Button textview0 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          app.resetComm();
          dismissPopupBT();
          Toast.makeText( context, R.string.bt_reset, Toast.LENGTH_SHORT).show();
        }
      } );

    Button textview1 = null;
    Button textview2 = null;
    Button textview3 = null;
    Button textview4 = null;
    if ( app.distoType() == Device.DISTO_X310 ) {
      // ----- TURN LASER ON
      //
      text = res.getString(R.string.remote_on);
      if ( len < text.length() ) len = text.length();
      textview1 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            app.setX310Laser( 1, null );
            dismissPopupBT();
          }
        } );

      // ----- TURN LASER OFF
      //
      text = res.getString(R.string.remote_off);
      if ( len < text.length() ) len = text.length();
      textview2 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            app.setX310Laser( 0, null );
            dismissPopupBT();
          }
        } );

      if ( gm_data ) {
      // ----- MEASURE ONE CALIB DATA
      //
      text = res.getString( R.string.popup_do_gm_data );
      if ( len < text.length() ) len = text.length();
      textview3 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            new DeviceX310TakeShot( null, app, 1 ).execute();
            dismissPopupBT();
          }
        } );

      } else {
        // ----- MEASURE ONE SPLAY AND DOWNLOAD IT
        //
        text = res.getString( R.string.popup_do_splay );
        if ( len < text.length() ) len = text.length();
        textview3 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              new DeviceX310TakeShot( lister, app, 1 ).execute();
              dismissPopupBT();
            }
          } );

        // ----- MEASURE ONE LEG AND DOWNLOAD IT
        //
        text = res.getString(R.string.popup_do_leg);
        if ( len < text.length() ) len = text.length();
        textview4 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              new DeviceX310TakeShot( lister, app, TDSetting.mMinNrLegShots ).execute();
              dismissPopupBT();
            }
          } );
      }
    }

    FontMetrics fm = textview0.getPaint().getFontMetrics();
    int w = (int)( Math.abs( len * fm.ascent ) * 0.7);
    int h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
    // int h = (int)( BUTTON_HEIGHT * 7 * 1.1 ); 
    textview0.setWidth( w );
    if ( app.distoType() == Device.DISTO_X310 ) {
      textview1.setWidth( w );
      textview2.setWidth( w );
      textview3.setWidth( w );
      if ( ! gm_data ) textview4.setWidth( w );
    }
    mPopupBT = new PopupWindow( popup_layout, w, h ); 
    mPopupBT.showAsDropDown(b); 
    return mPopupBT;
  }

  static boolean dismissPopupBT()
  {
    if ( mPopupBT != null ) {
      mPopupBT.dismiss();
      mPopupBT = null;
      return true;
    }
    return false;
  }

}
