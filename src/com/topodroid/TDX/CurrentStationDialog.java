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
                           , IGeoCoder
{
  private final ShotWindow mParent;
  private String mStationName;    // station name
  private EditText mName;
  private EditText mComment;
 
  private String mGeoCode = "";

  private Button mBtnPush;
  private Button mBtnPop;
  private Button mBtnOK;
  private Button mBtnClear;
  private Button mBtnGeoCode;
  private Button mBtnPhoto;
  // private Button mBtnCancel;

  private CheckBox mBtnFixed;
  private CheckBox mBtnPainted;

  private ListView mList;
  private StationInfoAdapter mAdapter;

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
    mStationName = ( station == null )? mApp.getCurrentOrLastStation() : station ;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate( savedInstanceState );
    boolean landscape = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    doInit( landscape );
  }
  
  /** initialize the dialog screen presentation
   * @param landscape  whether the app is in landscape mode
   */
  public void doInit( boolean landscape )
  {
    // TDLog.v("Do INIT landscape " + landscape );
    if ( landscape ) {
      initLayout( R.layout.current_station_dialog_landscape, R.string.title_current_station );
    } else {
      initLayout( R.layout.current_station_dialog_portrait, R.string.title_current_station );
    }

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
    mBtnGeoCode = (Button) findViewById(R.id.button_code );
    mBtnPhoto   = (Button) findViewById(R.id.button_photo );

    if ( TDLevel.overExpert ) {
      GeoCodes geocodes = TopoDroidApp.getGeoCodes();
      if ( geocodes.size() > 0 ) {
        mBtnGeoCode.setOnClickListener( this );
      } else {
        mBtnGeoCode.setVisibility( View.GONE );
      }
      mBtnPhoto.setOnClickListener( this );
    } else {
      mBtnPhoto.setVisibility( View.GONE );
      mBtnGeoCode.setVisibility( View.GONE );
    }

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

    mName.setText( (mStationName == null)? "" : mStationName );
    setCommentFlagsAndCode( mStationName );

    updateList();

    findViewById( R.id.current_station ).invalidate();
  }

  /** update the list of station infos
   */
  private void updateList()
  {
    // mApp.fillCurrentStationAdapter( adapter );
    ArrayList< StationInfo > stations = TopoDroidApp.mData.getStations( TDInstance.sid );
    mAdapter = new StationInfoAdapter( mContext, R.layout.message, stations );
    mList.setAdapter( mAdapter );
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
      TDLog.e("current station view instance of " + view.toString() );
      return;
    }
    String name = ((TextView) view).getText().toString();
    int pos = name.indexOf(' ');
    if ( pos > 0 ) name = name.substring(0,pos);
    name = name.trim();
    // TDLog.v( "STATION <" + name + ">" );
    setNameAndComment( name );

    // StationInfo station = mAdapter.get( position );
    // setNameAndComment( station.mName );
  }

  
  /** set the display from an existing station data
   * @param name    saved-station name
   * @note called from onItemClick, therefore the station exists
   */
  private void setNameAndComment( String name )
  {
    if ( TDString.isNullOrEmpty( name ) ) return; // safety check
    mStationName = name;
    StationInfo cs = ( name == null )? null : TopoDroidApp.mData.getStation( TDInstance.sid, name, null ); // null: do not create
    if ( cs == null ) {
      mName.setText( TDString.EMPTY );
      mComment.setText( null );
      mGeoCode = "";
    } else {
      mName.setText( cs.mName );
      mComment.setText( cs.mComment );
      mGeoCode = cs.getGeoCode();
    }
    setFlags( cs );
  }
  
  /** set the display comment field
   * @param name   station name
   */
  private void setCommentFlagsAndCode( String name )
  {
    StationInfo cs = ( name == null )? null : TopoDroidApp.mData.getStation( TDInstance.sid, name, null ); // null: do not create
    if ( cs == null ) {
      mComment.setText( "" );
      setFlags( null );
      mGeoCode = "";
    } else {
      mComment.setText( ( cs == null )? "" : cs.mComment );
      setFlags( cs );
      mGeoCode = cs.getGeoCode();
    }
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
    mStationName = TDString.EMPTY;
    mGeoCode = "";
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

  /** commit a saved station to the database
   * @param name     name of the saved station
   * @return true if success
   */
  private boolean storeStation( String name )
  {
    if ( name.length() == 0 ) {
      mName.setError( mContext.getResources().getString( R.string.error_name_required ) );
      return false;
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

    mStationName = name;
    TopoDroidApp.mData.insertStation( TDInstance.sid, name, comment, flag, name, mGeoCode ); // PRESENTATION = name
    updateList();
    return true;
  }

  /** take a station photo
   */
  public void takePhoto()
  {
    int camera   = TDandroid.AT_LEAST_API_21 ? PhotoInfo.CAMERA_TOPODROID_2 : PhotoInfo.CAMERA_TOPODROID;
    long photoId = mParent.doTakePhoto( 0, mStationName, "", camera, "", MediaInfo.TYPE_STATION ); // shot_id=0, comment="", mGeoCode=""
    TDLog.v("current station took photo " + photoId );
    if ( photoId > 0 ) {
      storeStationPhoto( mStationName, photoId );
    }

  }

  /** commit a station and its photo
   * @param name    station name
   * @param photoId photo id
   * @note called by the StationPhotoDialog
   */
  public void storeStationPhoto( String name, long photoId )
  {
    if ( name != null && photoId > 0 ) {
      if ( storeStation( name ) ) {
        TopoDroidApp.mData.updateStationPhoto( TDInstance.sid, name, photoId );
      }
    }
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
      storeStation( name );
      // mStationName = name; already in store station
      return;

    } else if ( b == mBtnPop ) { // DELETE saved station and dismiss
      if ( name.length() == 0 ) {
        mName.setError( mContext.getResources().getString( R.string.error_name_required ) );
        return;
      }
      long photoId = TopoDroidApp.mData.deleteStation( TDInstance.sid, name );
      if ( photoId > 0 ) {
        String photoFile = "st-" + photoId + ".jpg";
        TDLog.v("TODO delete photo file " + photoFile );
      }
      // clear();
      // updateList();
      // return;
    } else if ( b == mBtnClear ) { // CLEAR
      clear();
      return;
    } else if ( b == mBtnOK ) { // make station ACTIVE and dismiss
      if ( name.length() > 0 ) {
        mApp.setCurrentStationName( name );
      } else {
        mApp.setCurrentStationName( null );
      }
      if ( mParent != null ) mParent.updateDisplay();
    } else if ( b == mBtnGeoCode ) { // GEOCODE
      if ( storeStation( name ) ) {
        (new GeoCodeDialog( mContext, this, mGeoCode )).show();
      }
      return;
    } else if ( b == mBtnPhoto ) { // PHOTO
      if ( storeStation( name ) ) {
        StationInfo cs = TopoDroidApp.mData.getStation( TDInstance.sid, name, null ); // null: do not create
        if ( cs == null ) {
          TDToast.makeWarn( R.string.station_not_saved );
        } else {
          (new StationPhotoDialog( mContext, this, cs )).show();
        }
      }
      return;
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

  /** update the station geocode
   * @param geocode   geocode
   */
  public void setGeoCode( String geocode ) 
  { 
    mGeoCode = (geocode == null)? "" : geocode;
    if ( mStationName != null ) {
      mApp.mData.updateStationGeocode( TDInstance.sid, mStationName, mGeoCode );
    }
  }

}
