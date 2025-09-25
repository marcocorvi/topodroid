/* @file SearchDialog.java
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

class SearchDialog extends MyDialog
                        implements View.OnClickListener
                        , View.OnLongClickListener
{
  private final ShotWindow mParent;
  private EditText mName;
  private String mStation;
  private boolean mPair;

  private Button mBtnDuplicate;
  private Button mBtnSurface;
  private Button mBtnExtend;
  private Button mBtnReverse; // reversed splays
  // private Button mBtnCancel;

  // private CheckBox mBtnSplays;
  private Button mBtnLegStation;
  private Button mBtnAllStation;
  private Button mBtnLegSearch;


  private MyKeyboard mKeyboard = null;

  SearchDialog( Context context, ShotWindow parent, String station, boolean pair )
  {
    super( context, null, R.string.SearchDialog ); // null app
    mParent  = parent;
    mStation = station; // station name if result of a station search, or station pair for leg search
    mPair    = pair;    // whether the result of a leg search
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.search_dialog, R.string.title_search );

    mName = (EditText) findViewById( R.id.name );
    mName.setOnLongClickListener( this );
    if ( mStation != null ) {
      mName.setText( mStation );
    }

    // mBtnSplays = (CheckBox) findViewById(R.id.search_splays);
    // mBtnSplays.setVisibility( View.GONE ); 

    mBtnLegStation = (Button) findViewById(R.id.btn_leg_station );
    mBtnAllStation = (Button) findViewById(R.id.btn_all_station );
    mBtnLegSearch  = (Button) findViewById(R.id.btn_leg_search );
    mBtnLegStation.setOnClickListener( this );    // SEARCH 
    mBtnAllStation.setOnClickListener( this );    // SEARCH
    mBtnLegSearch.setOnClickListener( this );    // SEARCH

    LinearLayout ll3 = (LinearLayout) findViewById( R.id.layout3 );
    LinearLayout ll4 = (LinearLayout) findViewById( R.id.layout4 );
    mBtnDuplicate = (Button) findViewById(R.id.btn_duplicate );
    mBtnSurface   = (Button) findViewById(R.id.btn_surface );
    mBtnExtend    = (Button) findViewById(R.id.btn_extend );
    mBtnReverse   = (Button) findViewById(R.id.btn_reverse );

    if ( TDLevel.overExpert ) {
      mBtnDuplicate.setOnClickListener( this ); // SEARCH duplicate legs
      mBtnSurface.setOnClickListener( this );   // SEARCH surface legs
      mBtnExtend.setOnClickListener( this );   // SEARCH legs without extend
    } else {
      ll3.setVisibility( View.GONE );
      // mBtnDuplicate.setVisibility( View.GONE );
      // mBtnSurface.setVisibility( View.GONE );
      // mBtnExtend.setVisibility( View.GONE );
    }

    if ( TDLevel.overExpert ) {
      mBtnReverse.setOnClickListener( this );    // REVERSED SPLAYS
    } else {
      ll4.setVisibility( View.GONE );
      // mBtnReverse.setVisibility( View.GONE );
    }

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
    if ( CutNPaste.dismissPopup() ) return;
    MyKeyboard.close( mKeyboard );

    // TDLog.Log(  TDLog.LOG_INPUT, "Search Dialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnLegStation ) { // SEARCH leg station
      String name = mName.getText().toString().trim();
      if ( ! checkArgsName( name, 1 ) ) return;
      mParent.searchStation( name, false );
    } else if ( b == mBtnAllStation ) { // SEARCH all station
      String name = mName.getText().toString().trim();
      if ( ! checkArgsName( name, 1 ) ) return;
      mParent.searchStation( name, true );
    } else if ( b == mBtnLegSearch ) { // SEARCH legs
      String name = mName.getText().toString().trim();
      if ( ! checkArgsName( name, 2 ) ) return;
      mParent.searchLeg( name );
    } else if ( b == mBtnDuplicate ) { // SEARCH duplicate
      mParent.searchShot( DBlock.FLAG_DUPLICATE );
    } else if ( b == mBtnSurface ) { // SEARCH surface
      mParent.searchShot( DBlock.FLAG_SURFACE );
    } else if ( b == mBtnExtend ) { // SEARCH unset extend
      mParent.searchShot( DBlock.FLAG_NO_EXTEND );
    } else if ( b == mBtnReverse ) { // SEARCH reverse splays
      mParent.searchShot( DBlock.FLAG_REVERSE_SPLAY );

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
