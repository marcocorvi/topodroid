/* @file StationSearchDialog.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid station search dialog
 *
 * displays the stack of saved stations and allows to push 
 * a station on it or pop one from it
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;

import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;
import android.inputmethodservice.KeyboardView;

import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;

// import android.widget.Toast;

// import android.util.Log;

class StationSearchDialog extends MyDialog
                        implements View.OnClickListener
                        , View.OnLongClickListener
{
  private final ShotWindow mParent;
  private EditText mName;
  private String mStation;

  private Button mBtnDuplicate;
  private Button mBtnSurface;
  private Button mBtnSearch;
  // private Button mBtnCancel;

  private CheckBox mBtnSplays;

  private MyKeyboard mKeyboard = null;

  StationSearchDialog( Context context, ShotWindow parent, String station )
  {
    super( context, R.string.StationSearchDialog );
    mParent  = parent;
    mStation = station; // station name if result of a station search
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.station_search_dialog, R.string.title_station_search );

    mName = (EditText) findViewById( R.id.name );
    mName.setOnLongClickListener( this );
    if ( mStation != null ) mName.setText( mStation );

    mBtnSplays = (CheckBox) findViewById(R.id.splays);
    // mBtnSplays.setVisibility( View.GONE ); 

    mBtnDuplicate = (Button) findViewById(R.id.btn_duplicate );
    mBtnSurface   = (Button) findViewById(R.id.btn_surface );

    mBtnSearch = (Button) findViewById(R.id.btn_search);
    mBtnSearch.setOnClickListener( this );    // SEARCH
    if ( TDLevel.overExpert ) {
      mBtnDuplicate.setOnClickListener( this ); // SEARCH duplicate legs
      mBtnSurface.setOnClickListener( this );   // SEARCH surface legs
    } else {
      mBtnDuplicate.setVisibility( View.GONE );
      mBtnSurface.setVisibility( View.GONE );
    }
    // mBtnCancel = (Button) findViewById(R.id.btn_cancel);
    // mBtnCancel.setOnClickListener( this ); // CANCEL
    ( (Button) findViewById(R.id.btn_cancel) ).setOnClickListener( this ); // CANCEL

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), 
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
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

  @Override
  public void onClick(View v) 
  {
    if ( CutNPaste.dismissPopup() ) return;
    MyKeyboard.close( mKeyboard );

    // TDLog.Log(  TDLog.LOG_INPUT, "StationSearchDialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnSearch ) { // SEARCH station
      String name = mName.getText().toString().trim();
      if ( name.length() == 0 ) {
        mName.setError( mContext.getResources().getString( R.string.error_name_required ) );
        return;
      }
      mParent.searchStation( name, mBtnSplays.isChecked() );
    } else if ( b == mBtnDuplicate ) { // SEARCH duplicate
      mParent.searchShot( DBlock.FLAG_DUPLICATE );
    } else if ( b == mBtnSurface ) { // SEARCH surface
      mParent.searchShot( DBlock.FLAG_SURFACE );

    // } else if ( b == mBtnCancel ) {
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
