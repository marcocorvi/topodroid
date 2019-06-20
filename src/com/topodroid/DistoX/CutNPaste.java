/* @file CutNPaste.java
 *
 * @author marco corvi
 * @date dec 2015
 *
 * @brief TopoDroid shot stations cut-n-paste
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;

import android.widget.PopupWindow;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
// import android.widget.Toast;

import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.View.OnTouchListener;
import android.view.Gravity;
import android.view.MotionEvent;

import android.graphics.Paint.FontMetrics;

import android.util.TypedValue;

class CutNPaste
{
  final static private int BUTTON_HEIGHT = 22;

  static private String mClipboardText = null;
  static private PopupWindow mPopup = null;
  static private WeakReference<EditText> mEditText;

  static boolean dismissPopup()
  {
    if ( mPopup != null ) {
      mPopup.dismiss();
      mPopup = null;
      return true;
    }
    return false;
  }

  static void makePopup( final Context context, EditText et )
  {
    if ( mPopup != null ) {
      mPopup.dismiss();
      mPopup = null;
      return;
    }
    mEditText = new WeakReference<EditText>( et );

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
          if ( mEditText.get() != null ) {
            mClipboardText = mEditText.get().getText().toString();
            mEditText.get().setText(TDString.EMPTY);
            String str = String.format( context.getResources().getString( R.string.copied ), mClipboardText );
            TDToast.makeGravity( str, Gravity.LEFT | Gravity.TOP );
          }
          dismissPopup();
        }
      } );
    Button btn_copy = makePopupButton( context, copy, layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          if ( mEditText.get() != null ) {
            mClipboardText = mEditText.get().getText().toString();
            String str = String.format( context.getResources().getString( R.string.copied ), mClipboardText );
            TDToast.makeGravity( str, Gravity.LEFT | Gravity.TOP );
          }
          dismissPopup();
        }
      } );
    Button btn_paste = makePopupButton( context, paste, layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          if ( mClipboardText != null && mEditText.get() != null ) {
            mEditText.get().setText( mClipboardText );
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

    mPopup = new PopupWindow( layout, w, h );
    mPopup.showAsDropDown( et );
  }

  static private Button makeButton( Context context, String text, int color, int size )
  {
    Button button = new Button( context );
    // button.set???( R.layout.popup_item );

    // THIS CRASHES THE APP
    // button.setBackgroundResource( R.drawable.popup_bgcolor );
    button.setTextColor( color );
    button.setBackgroundColor( TDColor.VERYDARK_GRAY );

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
    Button button = makeButton( context, text, TDColor.WHITE, BUTTON_HEIGHT );
    layout.addView( button, new LinearLayout.LayoutParams(h, w));
    button.setOnClickListener( listener );
    button.setOnTouchListener( new View.OnTouchListener( ) {
      @Override public boolean onTouch( View v, MotionEvent ev ) { v.setBackgroundColor( TDColor.DARK_ORANGE ); return false; }
    } );
    return button;
  }

  static private PopupWindow mPopupBT = null;

  /** show BT mPopup under button b
   * @param b button
   */
  static PopupWindow showPopupBT( final Context context, final ILister ilister, final TopoDroidApp app, View b, boolean gm_data )
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
          TDToast.make( R.string.bt_reset );
        }
      } );

    Button textview1 = null;
    Button textview2 = null;
    Button textview3 = null;
    Button textview4 = null;
    if ( TDInstance.deviceType() == Device.DISTO_X310 ) {
      // ----- TURN LASER ON
      //
      text = res.getString(R.string.remote_on);
      if ( len < text.length() ) len = text.length();
      textview1 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            app.setX310Laser( 1, 0, null );
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
            app.setX310Laser( 0, 0, null );
            dismissPopupBT();
          }
        } );

      if ( gm_data ) {
      // ----- MEASURE ONE CALIB DATA AND DOWNLOAD IF MODE IS CONTINUOUS
      //
      text = res.getString( R.string.popup_do_gm_data );
      if ( len < text.length() ) len = text.length();
      textview3 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            // ilister.enableBluetoothButton(false);
            new DeviceX310TakeShot( ilister, (TDSetting.mCalibShotDownload ? lister : null), app, 1 ).execute();
            dismissPopupBT();
          }
        } );

      } else {
        // ----- MEASURE ONE SPLAY AND DOWNLOAD IT IF MODE IS CONTINUOUS
        //
        text = res.getString( R.string.popup_do_splay );
        if ( len < text.length() ) len = text.length();
        textview3 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              // ilister.enableBluetoothButton(false);
              new DeviceX310TakeShot( ilister, (TDSetting.isConnectionModeContinuous() ? lister : null), app, 1 ).execute();
              dismissPopupBT();
            }
          } );

        // ----- MEASURE ONE LEG AND DOWNLOAD IT IF MODE IS CONTINUOUS
        //
        text = res.getString(R.string.popup_do_leg);
        if ( len < text.length() ) len = text.length();
        textview4 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              // ilister.enableBluetoothButton(false);
              new DeviceX310TakeShot( ilister, (TDSetting.isConnectionModeContinuous()? lister : null), app, TDSetting.mMinNrLegShots ).execute();
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
    if ( TDInstance.deviceType() == Device.DISTO_X310 ) {
      if ( textview1 != null) textview1.setWidth( w );
      if ( textview2 != null) textview2.setWidth( w );
      if ( textview3 != null) textview3.setWidth( w );
      if ( ! gm_data ) if ( textview4 != null ) textview4.setWidth( w );
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
