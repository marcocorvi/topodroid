/* @file GMSearchDialog.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid search dialog for station-search or leg-flag search
 *
 * displays the stack of saved stations and allows to push 
 * a station on it or pop one from it
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

// import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDString;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.TDConst;
import com.topodroid.TDX.GMActivity;
import com.topodroid.TDX.R;
import com.topodroid.utils.TDLog;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;

import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;
import android.inputmethodservice.KeyboardView;

import android.widget.EditText;
import android.widget.Button;
// import android.widget.CheckBox;
// import android.widget.LinearLayout;

public class GMSearchDialog extends MyDialog
                           implements View.OnClickListener
{
  private final GMActivity mParent;
  private EditText mError;

  private Button mBtnSearch;
  private Button mBtnClear;  // clear search

  private MyKeyboard mKeyboard = null;

  public GMSearchDialog( Context context, GMActivity parent )
  {
    super( context, null, R.string.GMSearchDialog ); // null app
    mParent  = parent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.gm_search_dialog, R.string.title_gm_search );

    mError = (EditText) findViewById( R.id.error );
    mError.setText( "1.0" ); // degrees

    mBtnSearch  = (Button) findViewById(R.id.btn_search );
    mBtnSearch.setOnClickListener( this );
    mBtnClear  = (Button) findViewById(R.id.btn_clear );
    mBtnClear.setOnClickListener( this );

    ( (Button) findViewById(R.id.btn_close) ).setOnClickListener( this ); // CLOSE

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), 
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mError, flag);
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mError.setInputType( TDConst.NUMBER_DECIMAL );
      }
    }
  }

  @Override
  public void onClick(View v) 
  {
    MyKeyboard.close( mKeyboard );

    // TDLog.Log(  TDLog.LOG_INPUT, "Search Dialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnSearch ) { // SEARCH 
      float error = 1.0f; // degrees
      try { 
        error = Float.parseFloat( mError.getText().toString().trim() );
      } catch ( NumberFormatException e ) {
        TDLog.Error( e.getMessage() );
      }
      mParent.searchData( error );
    } else if ( b == mBtnClear ) {
      mParent.clearSearchResult();
    // } else if ( b == mBtnClose ) {
    //   /* nothing : dismiss */
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}
