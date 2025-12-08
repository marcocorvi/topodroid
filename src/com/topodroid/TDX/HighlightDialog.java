/* @file HighlightDialog.java
 *
 * @author marco corvi
 * @date sept 2024
 *
 * @brief TopoDroid search dialog for station-search in 3D viewer
 *
 * displays the stack of saved stations and allows to push 
 * a station on it or pop one from it
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;

import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;
// import android.inputmethodservice.KeyboardView;

import android.widget.EditText;
import android.widget.Button;
// import android.widget.CheckBox;
import android.widget.LinearLayout;

class HighlightDialog extends MyDialog
                      implements View.OnClickListener
                      // , View.OnLongClickListener
{
  private final TopoGL mParent;
  private EditText mName;

  private Button mBtnStation;
  // private Button mBtnCancel;


  private MyKeyboard mKeyboard = null;

  HighlightDialog( Context context, TopoGL parent )
  {
    super( context, null, R.string.HighlightDialog ); // null app
    mParent  = parent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.highlight_dialog, R.string.title_search );

    mName = (EditText) findViewById( R.id.name );
    // mName.setOnLongClickListener( this );

    mBtnStation = (Button) findViewById(R.id.btn_station );
    mBtnStation.setOnClickListener( this );

    // mBtnCancel = (Button) findViewById(R.id.btn_cancel);
    // mBtnCancel.setOnClickListener( this ); // CANCEL
    ( (Button) findViewById(R.id.btn_cancel) ).setOnClickListener( this ); // CANCEL

    // mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    mKeyboard = MyKeyboard.getMyKeyboard( mContext, findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );

    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mName, flag);
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mName.setInputType( TDConst.NUMBER_DECIMAL );
      }
    }
  }

  // @Override
  // public boolean onLongClick(View v) 
  // {
  //   if ( v == mName ) {
  //     CutNPaste.makePopup( mContext, (EditText)v );
  //     return true;
  //   }
  //   return false;
  // }

  /**
   * @param args args
   * @param nr   expected number of strings
   */
  private boolean checkArgsName( String args, int nr )
  {
    if ( args.length() == 0 ) {
      mName.setError( mContext.getResources().getString( R.string.error_name_required ) );
      return false;
    }
    String[] vals = TDString.splitOnSpaces( args );
    if ( vals.length != nr ) {
      mName.setError( mContext.getResources().getString( R.string.error_station_number ) );
      return false;
    }
    return true;
  }

  @Override
  public void onClick(View v) 
  {
    // if ( CutNPaste.dismissPopup() ) return;
    MyKeyboard.close( mKeyboard );

    // TDLog.Log(  TDLog.LOG_INPUT, "Search Dialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnStation ) { // SEARCH station
      String name = TDString.noSpaces( mName.getText().toString() );
      if ( ! checkArgsName( name, 1 ) ) return;
      mParent.highlightStations( name );
    // } else if ( b == mBtnCancel ) {
    //   /* nothing : dismiss */
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    // if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}
