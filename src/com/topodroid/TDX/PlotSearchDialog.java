/* @file PlotSearchDialog.java
 *
 * @author marco corvi
 * @date mar 2025
 *
 * @brief TopoDroid dialog for station-search in plot
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

class PlotSearchDialog extends MyDialog
                        implements View.OnClickListener
                        , View.OnLongClickListener
{
  private final DrawingWindow mParent;
  private EditText mName;

  private MyKeyboard mKeyboard = null;

  PlotSearchDialog( Context context, DrawingWindow parent )
  {
    super( context, null, R.string.PlotSearchDialog ); // null app
    mParent  = parent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.plot_search_dialog, R.string.title_plot_search );

    mName = (EditText) findViewById( R.id.name );
    mName.setOnLongClickListener( this );

    // mBtnCancel = (Button) findViewById(R.id.btn_cancel);
    // mBtnCancel.setOnClickListener( this ); // CANCEL
    ( (Button) findViewById(R.id.btn_station) ).setOnClickListener( this ); // SEARCH
    ( (Button) findViewById(R.id.btn_clear) ).setOnClickListener( this ); // CLEAR
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

  @Override
  public boolean onLongClick(View v) 
  {
    if ( v == mName ) {
      CutNPaste.makePopup( mContext, (EditText)v );
      return true;
    }
    return false;
  }

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
    return true;
  }

  @Override
  public void onClick(View v) 
  {
    if ( CutNPaste.dismissPopup() ) return;
    MyKeyboard.close( mKeyboard );

    // TDLog.Log(  TDLog.LOG_INPUT, "Search Dialog onClick() " );
    Button b = (Button) v;
    if ( v.getId() == R.id.btn_station ) {
      String name = mName.getText().toString().trim();
      if ( ! checkArgsName( name, 1 ) ) return;
      mParent.highlightStation( name );
    } else if ( v.getId() == R.id.btn_clear ) {
      mParent.highlightStation( null );
    // } else if ( v.getId() == R.id.btn_cancel ) {
    //   /* nothing : dismiss */
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}
