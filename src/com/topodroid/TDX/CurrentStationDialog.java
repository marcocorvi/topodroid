/* @file CurrentStationDialog.java
 *
 * @author marco corvi
 * @date jan 2015
 *
 * @brief TopoDroid current station dialog
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

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.StationFlag;

// import androidx.annotation.RecentlyNonNull;

// import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;
import android.content.res.Configuration;

import android.view.View;
// import android.view.View.OnClickListener;
import android.inputmethodservice.KeyboardView;

// import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.CheckBox;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

class CurrentStationDialog extends MyDialog
                           implements View.OnClickListener
                           , View.OnLongClickListener
                           , OnItemClickListener
{
  private final ShotWindow mParent;
  private String mStation;    // station name
  private EditText mName;
  private EditText mComment;

  private Button mBtnPush;
  private Button mBtnPop;
  private Button mBtnOK;
  private Button mBtnClear;
  // private Button mBtnCancel;

  private CheckBox mBtnFixed;
  private CheckBox mBtnPainted;

  private ListView mList;

  private MyKeyboard mKeyboard = null;

  /** cstr
   * @param context   context
   * @param parent    parent window
   * @param app       application (used to set the active station)
   * @param station   station name
   */
  CurrentStationDialog( Context context, ShotWindow parent, TopoDroidApp app, String station )
  {
    super( context, app, R.string.CurrentStationDialog );
    mParent  = parent;
    mStation = ( station == null )? mApp.getCurrentOrLastStation() : station ;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate( savedInstanceState );
    initLayout( R.layout.current_station_dialog, R.string.title_current_station );

    mList = (ListView) findViewById(R.id.list);
    mList.setDividerHeight( 2 );
    mList.setOnItemClickListener( this );

    mName = (EditText) findViewById( R.id.name );
    mComment = (EditText) findViewById( R.id.comment );
    mName.setOnLongClickListener( this );

    mBtnFixed   = (CheckBox) findViewById(R.id.button_fixed);
    mBtnPainted = (CheckBox) findViewById(R.id.button_painted);
    mBtnFixed.setOnClickListener( this ); 
    mBtnPainted.setOnClickListener( this ); 

    mBtnPush    = (Button) findViewById(R.id.button_push);
    mBtnPop     = (Button) findViewById(R.id.button_pop );
    mBtnOK      = (Button) findViewById(R.id.button_current );
    mBtnClear   = (Button) findViewById(R.id.button_clear );

    mBtnPush.setOnClickListener( this ); // STORE
    mBtnPop.setOnClickListener( this );  // DELETE
    mBtnOK.setOnClickListener( this );   // OK-SAVE
    mBtnClear.setOnClickListener( this );   // CLEAR

    // mBtnCancel = (Button) findViewById(R.id.button_cancel);
    // mBtnCancel.setOnClickListener( this );
    ( (Button) findViewById(R.id.button_cancel) ).setOnClickListener( this );

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

    mName.setText( mStation );
    setComment( mStation );

    updateList();
  }

  /** uodate the list of station infos
   */
  private void updateList()
  {
    MyStringAdapter adapter = new MyStringAdapter( mContext, R.layout.message );
    // mApp.fillCurrentStationAdapter( adapter );
    ArrayList< StationInfo > stations = TopoDroidApp.mData.getStations( TDInstance.sid );
    for ( StationInfo st : stations ) {
      adapter.add( st.toString() );
    }
    mList.setAdapter( adapter );
  }

  /** react to user tap on an item in the list
   * @param parent    parent adapter ?
   * @param view      item view ?
   * @param position  item position in the list
   * @param id        ???
   */
  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    if ( ! ( view instanceof TextView ) ) {
      TDLog.Error("current station view instance of " + view.toString() );
      return;
    }
    String name = ((TextView) view).getText().toString();
    int pos = name.indexOf(' ');
    if ( pos > 0 ) name = name.substring(0,pos);
    name = name.trim();
    TDLog.v( "STATION <" + name + ">" );
    setNameAndComment( name );
  }

  
  /** set the display from an existing station data
   * @param name    saved-station name
   * @note called from onItemClick, therefore the station exists
   */
  private void setNameAndComment( String name )
  {
    if ( name == null || name.length() == 0 ) return; // safety check
    mStation = name;
    StationInfo cs = TopoDroidApp.mData.getStation( TDInstance.sid, name, null ); // null: do not create
    if ( cs == null ) {
      mName.setText( TDString.EMPTY );
      mComment.setText( null );
    } else {
      mName.setText( cs.mName );
      mComment.setText( cs.mComment );
    }
    setFlags( cs );
  }
  
  /** set the display comment field
   * @param name   station name
   */
  private void setComment( String name )
  {
    StationInfo cs = TopoDroidApp.mData.getStation( TDInstance.sid, name, null ); // null: do not create
    mComment.setText( ( cs == null )? null : cs.mComment );
    setFlags( cs );
  }

  /** set the display flags boxes
   * @param cs   station info
   */
  private void setFlags( StationInfo cs )
  {
    mBtnFixed.setChecked( false );
    mBtnPainted.setChecked( false );
    if ( cs == null ) return; // safety check
    if ( cs.mFlag.isFixed() ) {
      mBtnFixed.setChecked( true );
    } else if ( cs.mFlag.isPainted() ) {
      mBtnPainted.setChecked( true );
    }
  }

  /** clear the input fields 
   */
  private void clear()
  {
    mStation = TDString.EMPTY;
    mName.setText(TDString.EMPTY);
    mComment.setText(TDString.EMPTY);
    mBtnFixed.setChecked( false );
    mBtnPainted.setChecked( false );
  }

  /** react to user long-tap - used for station name cun-n-paste
   * @param v tapped view
   */
  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }

  /** react to a user tap
   * @param v tapped view
   */
  @Override
  public void onClick(View v) 
  {
    CutNPaste.dismissPopup();
    MyKeyboard.close( mKeyboard );

    // TDLog.Log(  TDLog.LOG_INPUT, "CurrentStationDialog onClick() " );
    Button b = (Button) v;
    String name = mName.getText().toString().trim();
    if ( b == mBtnFixed ) {
      mBtnPainted.setChecked( false );
      return;
    } else if ( b == mBtnPainted ) {
      mBtnFixed.setChecked( false );
      return;

    } else if ( b == mBtnPush ) { // STORE
      if ( name.length() == 0 ) {
        mName.setError( mContext.getResources().getString( R.string.error_name_required ) );
        return;
      }
      
      int flag = StationFlag.STATION_NONE;
      if ( mBtnFixed.isChecked() ) {
        flag = StationFlag.STATION_FIXED;
      } else if ( mBtnPainted.isChecked() ) {
        flag = StationFlag.STATION_PAINTED;
      }

      String comment = TDString.EMPTY;
      if ( mComment.getText() != null ) {
        comment = mComment.getText().toString().trim();
      }

      mStation = name;
      TopoDroidApp.mData.insertStation( TDInstance.sid, name, comment, flag, name ); // PRESENTATION = name
      updateList();
      return;

    } else if ( b == mBtnPop ) { // DELETE
      if ( name.length() == 0 ) {
        mName.setError( mContext.getResources().getString( R.string.error_name_required ) );
        return;
      }
      TopoDroidApp.mData.deleteStation( TDInstance.sid, name );
      clear();
      updateList();
      return;
    } else if ( b == mBtnClear ) {
      clear();
      return;
    } else if ( b == mBtnOK ) {
      if ( name.length() > 0 ) {
        mApp.setCurrentStationName( name );
      } else {
        mApp.setCurrentStationName( null );
      }
      if ( mParent != null ) mParent.updateDisplay();

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
