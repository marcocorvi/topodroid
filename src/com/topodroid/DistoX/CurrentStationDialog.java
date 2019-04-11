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
package com.topodroid.DistoX;

// import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;

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

// import android.widget.Toast;

// import android.util.Log;

class CurrentStationDialog extends MyDialog
                        implements View.OnClickListener
                        , View.OnLongClickListener
                        , OnItemClickListener
{
  private final TopoDroidApp mApp;
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

  CurrentStationDialog( Context context, ShotWindow parent, TopoDroidApp app, String station )
  {
    super( context, R.string.CurrentStationDialog );
    mParent  = parent;
    mApp = app;
    mStation = ( station == null )? mApp.getCurrentOrLastStation() : station ;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

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
    setComment( );

    updateList();
  }

  private void updateList()
  {
    MyStringAdapter adapter = new MyStringAdapter( mContext, R.layout.message );
    // mApp.fillCurrentStationAdapter( adapter );
    ArrayList< CurrentStation > stations = TopoDroidApp.mData.getStations( TDInstance.sid );
    for ( CurrentStation st : stations ) {
      adapter.add( st.toString() );
    }
    mList.setAdapter( adapter );
  }


  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String name = item.toString();
    String[] token = name.split(" ");
    if ( token.length == 1 ) {
      name = name.trim();
    } else {
      name = token[0];
    }
    // Log.v("DistoX", "get station <" + name + ">" );
    setNameAndComment( name );
  }

  
  // @param name    saved-station name
  private void setNameAndComment( String name )
  {
    mStation = name;
    mBtnFixed.setChecked( false );
    mBtnPainted.setChecked( false );
    CurrentStation cs = TopoDroidApp.mData.getStation( TDInstance.sid, mStation );
    if ( cs == null ) {
      mName.setText( TDString.EMPTY );
      mComment.setText( null );
    } else {
      mName.setText( cs.mName );
      mComment.setText( cs.mComment );
      if ( cs.mFlag == CurrentStation.STATION_FIXED ) {
        mBtnFixed.setChecked( true );
      } else if ( cs.mFlag == CurrentStation.STATION_PAINTED ) {
        mBtnPainted.setChecked( true );
      }
    }
  }
  
  private void setComment( )
  {
    mBtnFixed.setChecked( false );
    mBtnPainted.setChecked( false );
    CurrentStation cs = TopoDroidApp.mData.getStation( TDInstance.sid, mStation );
    if ( cs == null ) {
      mComment.setText( null );
    } else {
      mComment.setText( cs.mComment );
      if ( cs.mFlag == CurrentStation.STATION_FIXED ) {
        mBtnFixed.setChecked( true );
      } else if ( cs.mFlag == CurrentStation.STATION_PAINTED ) {
        mBtnPainted.setChecked( true );
      }
    }
  }

  private void clear()
  {
    mStation = TDString.EMPTY;
    mName.setText(TDString.EMPTY);
    mComment.setText(TDString.EMPTY);
    mBtnFixed.setChecked( false );
    mBtnPainted.setChecked( false );
  }

  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }

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
      
      int flag = CurrentStation.STATION_NONE;
      if ( mBtnFixed.isChecked() ) {
        flag = CurrentStation.STATION_FIXED;
      } else if ( mBtnPainted.isChecked() ) {
        flag = CurrentStation.STATION_PAINTED;
      }

      String comment = TDString.EMPTY;
      if ( mComment.getText() != null ) {
        comment = mComment.getText().toString().trim();
      }

      mStation = name;
      TopoDroidApp.mData.insertStation( TDInstance.sid, name, comment, flag );
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
