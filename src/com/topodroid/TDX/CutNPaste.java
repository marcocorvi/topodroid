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
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import static android.view.Gravity.LEFT;

import com.topodroid.utils.TDString;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;
import com.topodroid.dev.DataType;
import com.topodroid.dev.distox2.DeviceX310TakeShot;
import com.topodroid.dev.bric.BricMode; // MODE
import com.topodroid.dev.bric.BricConst;
import com.topodroid.dev.bric.MemoryBricTask;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
// import android.os.AsyncTask;

import android.content.DialogInterface;

import android.widget.PopupWindow;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import android.view.View;
import android.view.Gravity;
import android.view.MotionEvent;

import android.graphics.Paint.FontMetrics;

import android.util.TypedValue;

public class CutNPaste
{
  final static private int BUTTON_HEIGHT = 22;

  static private String mClipboardText = null;
  static private PopupWindow mPopup = null;
  static private WeakReference<EditText> mEditText;

  /** dismiss the popup
   * @return true is a popup has been dismissed
   */
  static public boolean dismissPopup()
  {
    if ( mPopup != null ) {
      mPopup.dismiss();
      mPopup = null;
      return true;
    }
    return false;
  }

  /** make a new popup
   * @param context  context
   * @param et       popup edit text
   */
  static public void makePopup( final Context context, EditText et )
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

    Button btn_cut = makePopupButton( context, cut, layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          EditText edit_text = mEditText.get();
          if ( edit_text != null ) {
            mClipboardText = edit_text.getText().toString();
            edit_text.setText(TDString.EMPTY);
            String str = String.format( context.getResources().getString( R.string.copied ), mClipboardText );
            TDToast.makeGravity( str, LEFT | Gravity.TOP );
          }
          dismissPopup();
        }
      } );
    float w = btn_cut.getPaint().measureText( cut );

    Button btn_copy = makePopupButton( context, copy, layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          EditText edit_text = mEditText.get();
          if ( edit_text != null ) {
            mClipboardText = edit_text.getText().toString();
            String str = String.format( context.getResources().getString( R.string.copied ), mClipboardText );
            TDToast.makeGravity( str, LEFT | Gravity.TOP );
          }
          dismissPopup();
        }
      } );
    float ww = btn_copy.getPaint().measureText( cut );
    if ( ww > w ) w = ww;

    Button btn_paste = makePopupButton( context, paste, layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          if ( mClipboardText != null ) {
            EditText edit_text = mEditText.get();
            if ( edit_text != null ) {
              edit_text.setText( mClipboardText );
            }
          }
          dismissPopup();
        }
      } );
    ww = btn_paste.getPaint().measureText( cut );
    if ( ww > w ) w = ww;
    int iw = (int)(w + 10);
    btn_cut.setWidth( iw );
    btn_copy.setWidth( iw );
    btn_paste.setWidth( iw );

    FontMetrics fm = btn_cut.getPaint().getFontMetrics();
    int ih = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 2.7); // 1.7
    mPopup = new PopupWindow( layout, iw, ih );
    mPopup.showAsDropDown( et );
  }

  /** make a button
   * @param context  context
   * @param text     button text
   * @param color    button background color
   * @param size     button size
   * @return the created button
   */
  static private Button makeButton( Context context, String text, int color, int size )
  {
    Button button = new Button( context );
    // button.set???( R.layout.popup_item );

    // THIS CRASHES THE APP
    // button.setBackgroundResource( R.drawable.popup_bg_color );
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

  /** make a popup button
   * @param context  context
   * @param text     button text
   * @param layout   ? layout ?
   * @param w        button width
   * @param h        button height
   * @param listener button click listener
   * @return the created button
   */
  static public Button makePopupButton( Context context, String text,
                                 LinearLayout layout, int w, int h, View.OnClickListener listener )
  {
    Button button = makeButton( context, text, TDColor.WHITE, BUTTON_HEIGHT );
    layout.addView( button, new LinearLayout.LayoutParams(h, w));
    button.setOnClickListener( listener );
    button.setOnTouchListener( new View.OnTouchListener( ) {
      @Override public boolean onTouch( View v, MotionEvent ev ) {
        v.setBackgroundColor( TDColor.DARK_ORANGE );
        // button.performClick(); // don't do performClick right-away, give user a short feedback
        return false;
      }
    } );
    return button;
  }

  static private PopupWindow mPopupBT = null;

  /** show BT mPopup under button b
   * @param context  context
   * @param i_lister  data lister
   * @param app      TopoDroid app
   * @param b        button
   * @param gm_data  if called from GM-activity
   * @param do_clear if add button to clear memory (BRIC only)
   * @return the new popup
   */
  static public PopupWindow showPopupBT( final Context context, final ILister i_lister, final TopoDroidApp app, View b, boolean gm_data, boolean do_clear )
  {
    final Resources res = context.getResources();
    final ListerHandler lister = new ListerHandler( i_lister );
    LinearLayout popup_layout  = new LinearLayout( context );
    popup_layout.setOrientation(LinearLayout.VERTICAL);
    int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
    int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;

    // ----- RESET BT
    //
    String text = res.getString(R.string.remote_reset);
    Button textview0 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          app.resetComm();
          dismissPopupBT();
          TDToast.make( R.string.bt_reset );
        }
      } );
    float w = textview0.getPaint().measureText( text );

    Button textview1 = null;
    Button textview2 = null;
    Button textview3 = null;
    Button textview4 = null;
    Button textview5 = null;
    // TDLog.v("BLE-CnP device type " + TDInstance.deviceType() );

    if ( TDInstance.deviceType() == Device.DISTO_X310 ) {
      // ----- TURN LASER ON
      //
      text = res.getString(R.string.remote_on);
      textview1 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            app.setX310Laser( Device.LASER_ON, 0, null, DataType.DATA_ALL );
            dismissPopupBT();
          }
        } );
      float ww = textview1.getPaint().measureText( text );
      if ( ww > w ) w = ww;

      // ----- TURN LASER OFF
      //
      text = res.getString(R.string.remote_off);
      textview2 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            app.setX310Laser( Device.LASER_OFF, 0, null, DataType.DATA_ALL );
            dismissPopupBT();
          }
        } );
      ww = textview2.getPaint().measureText( text );
      if ( ww > w ) w = ww;

      if ( gm_data ) {
        // ----- MEASURE ONE CALIB DATA AND DOWNLOAD IF MODE IS CONTINUOUS
        //
        text = res.getString( R.string.popup_do_gm_data );
        textview3 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              // i_lister.enableBluetoothButton(false);
              new DeviceX310TakeShot( i_lister, (TDSetting.mCalibShotDownload ? lister : null), app, 1, DataType.DATA_CALIB ).execute();
              dismissPopupBT();
            }
          } );
        ww = textview3.getPaint().measureText( text );
        if ( ww > w ) w = ww;

        if ( TDSetting.isConnectionModeContinuous() ) {
          // ----- MEASURE ONE CALIB GROUP AND DOWNLOAD : NEED MODE CONTINUOUS
          //
          text = res.getString( R.string.popup_do_gm_group );
          textview4 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                // i_lister.enableBluetoothButton(false);
                new DeviceX310TakeShot( i_lister, lister, app, 4, DataType.DATA_CALIB ).execute();
                dismissPopupBT();
              }
            } );
          ww = textview3.getPaint().measureText( text );
          if ( ww > w ) w = ww;
        }

      } else {
        // ----- MEASURE ONE SPLAY AND DOWNLOAD IT IF MODE IS CONTINUOUS
        //
        text = res.getString( R.string.popup_do_splay );
        textview3 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              // i_lister.enableBluetoothButton(false);
              new DeviceX310TakeShot( i_lister, (TDSetting.isConnectionModeContinuous() ? lister : null), app, 1, DataType.DATA_SHOT ).execute();
              dismissPopupBT();
            }
          } );
        ww = textview3.getPaint().measureText( text );
        if ( ww > w ) w = ww;

        // ----- MEASURE ONE LEG AND DOWNLOAD IT IF MODE IS CONTINUOUS
        //
        text = res.getString(R.string.popup_do_leg);
        textview4 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              // i_lister.enableBluetoothButton(false);
              new DeviceX310TakeShot( i_lister, (TDSetting.isConnectionModeContinuous()? lister : null), app, TDSetting.mMinNrLegShots, DataType.DATA_SHOT ).execute();
              dismissPopupBT();
            }
          } );
        ww = textview4.getPaint().measureText( text );
        if ( ww > w ) w = ww;
      }
    } 
    else if ( TDInstance.deviceType() == Device.DISTO_BRIC4 ) // -----------------------------------------------------
    {
      // ----- TURN LASER ON/OFF
      //
      text = res.getString(R.string.popup_do_laser);
      textview1 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            app.sendBricCommand( BricConst.CMD_LASER );
            dismissPopupBT();
          }
        } );
      float ww = textview1.getPaint().measureText( text );
      if ( ww > w ) w = ww;

      // ----- SHOT MEASURE 
      //
      text = res.getString( R.string.popup_do_shot );
      textview2 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            app.sendBricCommand( BricConst.CMD_SHOT );
            dismissPopupBT();
          }
        } );
      ww = textview2.getPaint().measureText( text );
      if ( ww > w ) w = ww;

      // ----- SCAN MEASURE
      //
      if ( TDSetting.mBricMode != BricMode.MODE_PRIM_ONLY ) {
        text = res.getString( R.string.popup_do_scan );
        textview3 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              app.sendBricCommand( BricConst.CMD_SCAN );
              dismissPopupBT();
            }
          } );
        ww = textview3.getPaint().measureText( text );
        if ( ww > w ) w = ww;
      }

      // ----- TURN OFF
      //
      text = res.getString(R.string.popup_do_off);
      textview4 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            app.sendBricCommand( BricConst.CMD_OFF );
            dismissPopupBT();
          }
        } );
      ww = textview4.getPaint().measureText( text );
      if ( ww > w ) w = ww;

      if ( do_clear ) { // ----- CLEAR MEMORY
        text = res.getString(R.string.popup_do_clear);
        textview5 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              TopoDroidAlertDialog.makeAlert( context, res, R.string.bric_ask_memory_clear, new DialogInterface.OnClickListener() {
                @Override public void onClick( DialogInterface dialog, int btn ) {
                  new MemoryBricTask( app ).execute();
                }
              } );
              dismissPopupBT();
            }
          } );
        ww = textview5.getPaint().measureText( text );
        if ( ww > w ) w = ww;
      }

/*
      // ----- MEASURE ONE SPLAY AND DOWNLOAD IT IF MODE IS CONTINUOUS
      //
      text = res.getString( R.string.popup_do_splay );
      textview3 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            app.sendBricCommand( BricConst.CMD_SPLAY );
            dismissPopupBT();
          }
        } );
      ww = textview3.getPaint().measureText( text );
      if ( ww > w ) w = ww;

      // ----- MEASURE ONE LEG AND DOWNLOAD IT IF MODE IS CONTINUOUS
      //
      text = res.getString(R.string.popup_do_leg);
      textview4 = makePopupButton( context, text, popup_layout, lWidth, lHeight,
        new View.OnClickListener( ) {
          public void onClick(View v) {
            app.sendBricCommand( BricConst.CMD_LEG );
            dismissPopupBT();
          }
        } );
      ww = textview4.getPaint().measureText( text );
      if ( ww > w ) w = ww;
*/
    }
    int iw = (int)(w + 10);
    textview0.setWidth( iw );
    // if ( TDInstance.deviceType() == Device.DISTO_X310 ) {
      if ( textview1 != null) textview1.setWidth( iw );
      if ( textview2 != null) textview2.setWidth( iw );
      if ( textview3 != null) textview3.setWidth( iw );
    //   if ( ! gm_data ) {
        if ( textview4 != null ) textview4.setWidth( iw );
        if ( textview5 != null ) textview5.setWidth( iw );
    //   }
    // }

    FontMetrics fm = textview0.getPaint().getFontMetrics();
    int ih = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
    mPopupBT = new PopupWindow( popup_layout, iw, ih ); 
    mPopupBT.showAsDropDown(b); 
    return mPopupBT;
  }

  /** dismiss the BlueTooth popup
   * @return true if success
   */
  static public boolean dismissPopupBT()
  {
    if ( mPopupBT != null ) {
      mPopupBT.dismiss();
      mPopupBT = null;
      return true;
    }
    return false;
  }

}
