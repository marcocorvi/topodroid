/* @file FixedGpsDialog.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid GPS-location for fixed stations
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Iterator;

import java.io.File;

import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;

import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;
// import android.widget.TextView.OnEditorActionListener;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.inputmethodservice.KeyboardView;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.location.GpsSatellite;
// import android.location.GpsStatus.Listener;

import android.net.Uri;

import android.util.Log;

class FixedGpsDialog extends MyDialog
                            implements View.OnClickListener
                                     , View.OnLongClickListener
                                     , TextView.OnEditorActionListener
                                     , LocationListener
                                     , GpsStatus.Listener
{
  private FixedActivity mParent;
  // private boolean  mLocated;
  private LocationManager locManager;
  private WorldMagneticModel mWMM;

  private TextView mTVlat;
  private TextView mTVlng;
  private TextView mTValt;
  private TextView mTVasl;
  private EditText mETstation;
  private EditText mETcomment;

  private Button   mBtnLoc;
  private Button   mBtnAdd;
  private Button   mBtnView;

  private Button   mBtnStatus;
  // private Button   mBtnCancel;

  double mLat;  // decimal degrees
  double mLng;  // decimal degrees
  double mHEll; // meters
  double mHGeo; // altimetric altitude
  boolean mHasLocation;

  private GpsStatus mStatus;
  private boolean mLocating; // whether is locating

  private MyKeyboard mKeyboard;

  FixedGpsDialog( Context context, FixedActivity parent )
  {
    super(context, R.string.FixedGpsDialog );
    mParent = parent;
    locManager = (LocationManager) mContext.getSystemService( Context.LOCATION_SERVICE );
    mStatus = locManager.getGpsStatus( null );
    mHasLocation = false;
    // mLocating = false;
    // Log.v(  TopoDroidApp.TAG, "UnitLocation " + TopoDroidApp.mUnitLocation + " ddmmss " + TopoDroidApp.DDMMSS );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_LOC, "Location onCreate" );

    initLayout( R.layout.fixed_gps_dialog, R.string.title_fixed_gps );

    mTVlng = (TextView) findViewById(R.id.longitude );
    mTVlat = (TextView) findViewById(R.id.latitude  );
    mTValt = (TextView) findViewById(R.id.h_ellipsoid  );   // ellipsoid
    mTVasl = (TextView) findViewById(R.id.h_geoid );        // geoid
    mETstation = (EditText) findViewById( R.id.station );
    mETcomment = (EditText) findViewById( R.id.comment );

    mETstation.setOnEditorActionListener( this );
    mETcomment.setOnEditorActionListener( this );

    mETstation.setOnLongClickListener( this );

    mBtnLoc = (Button) findViewById( R.id.button_loc );
    mBtnStatus = mBtnLoc;
    mBtnAdd = (Button) findViewById(R.id.button_add );
    mBtnView = (Button) findViewById( R.id.button_view );

    mBtnLoc.setOnClickListener( this );
    mBtnAdd.setOnClickListener( this );
    mBtnView.setOnClickListener( this );

    mLocating = false;
    mWMM = new WorldMagneticModel( mContext );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard_base, R.xml.my_keyboard_qwerty );
    if ( TDSetting.mKeyboard ) {
      if ( TDSetting.mStationNames == 1 ) {
        MyKeyboard.registerEditText( mKeyboard, mETstation, MyKeyboard.FLAG_POINT );
      } else {
        MyKeyboard.registerEditText( mKeyboard, mETstation, MyKeyboard.FLAG_POINT_LCASE_2ND );
      }
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mETstation.setInputType( TDConst.NUMBER_DECIMAL );
      }
    }
  }


  private void addFixedPoint( )
  {
    String name = mETstation.getText().toString();
    if ( name.length() == 0 ) {
      mETstation.setError( mContext.getResources().getString( R.string.error_station_required ) );
      return;
    }
    String comment = mETcomment.getText().toString();
    // if ( comment == null ) comment = "";
    mParent.addFixedPoint( name, mLng, mLat, mHEll, mHGeo, comment, FixedInfo.SRC_TOPODROID );
  }

  @Override
  public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
  {
    CutNPaste.dismissPopup();
    return false;
  }
  //   // TDLog.Log( TDLog.LOG_INPUT, "Location onEditorAction " + actionId );
  //   // if ( actionId == 6 )
  //   {
  //     if ( (EditText)v == mETstation ) {
  //       if ( mLocating ) {
  //         setGPSoff();
  //       }
  //       mBtnLoc.setEnabled( false );
  //       // mHasLocation = false;
  //       mBtnAdd.setEnabled( false );
  //       mBtnView.setEnabled( false );
  //       CharSequence item = mETstation.getText();
  //       if ( item != null && item.length() > 0 ) {
  //         String str = item.toString();
  //         // check if station has already a location
  //         if ( mApp.mData.hasFixedStation( -1L, mApp.mSID, str ) ) {
  //           String error = mContext.getResources().getString( R.string.error_station_already_fixed );
  //           mETstation.setError( error );
  //           return false;
  //         }
  //         boolean enabled =  ( str != null && str.length() > 0 );
  //         mBtnLoc.setEnabled( enabled );
  //         mBtnAdd.setEnabled( enabled );
  //         mBtnView.setEnabled( enabled );
  //       }
  //     }
  //   }
  //   return false;
  // }

  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }

  @Override
  public void onClick(View v) 
  {
    if ( CutNPaste.dismissPopup() ) return;
    MyKeyboard.close( mKeyboard );

    boolean do_toast = false;
    Button b = (Button) v;
    if ( b == mBtnAdd ) {
      // stop GPS location and start dialog for lat/long/alt data
      if ( mLocating ) {
        setGPSoff();
      }
      if ( mHasLocation ) {
        addFixedPoint();
        dismiss();
      } else {
        do_toast = true;
      }
    } else if ( b == mBtnView ) {
      if ( mHasLocation ) {
        Uri uri = Uri.parse( "geo:" + mLat + "," + mLng + "?q=" + mLat + "," + mLng );
        mContext.startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
      } else {
        do_toast = true;
      }
    } else if ( b == mBtnLoc ) {
      if ( mLocating ) {
        setGPSoff();
      } else {      
        setGPSon();
      }
    }
    if ( do_toast ) {
      Toast.makeText( mContext, R.string.no_location_data, Toast.LENGTH_SHORT ).show();
    }
  }


  /** location is stored in decimal degrees but displayed as deg:min:sec
   * N.B. the caller must check loc != null
   */
  private void displayLocation( Location loc )
  {
    mLat  = loc.getLatitude();
    mLng  = loc.getLongitude();
    mHEll = loc.getAltitude();
    mHGeo = mWMM.ellipsoidToGeoid( mLat, mLng, mHEll ); 
    mHasLocation = true;

    mTVlng.setText( mContext.getResources().getString( R.string.longitude ) + " " + FixedInfo.double2string( mLng ) );
    mTVlat.setText( mContext.getResources().getString( R.string.latitude ) + " " + FixedInfo.double2string( mLat ) );
    mTValt.setText( mContext.getResources().getString( R.string.h_ellipsoid ) + " " + Integer.toString( (int)(mHEll) ) );
    mTVasl.setText( mContext.getResources().getString( R.string.h_geoid ) + " " + Integer.toString( (int)(mHGeo) ) );
    
  }

  // -----------------------------------------------------------

  private void setGPSoff()
  {
    mBtnLoc.setText( mContext.getResources().getString( R.string.button_gps_start ) );
    mBtnStatus.setBackgroundColor( 0xff3366ff );
    locManager.removeUpdates( this );
    locManager.removeGpsStatusListener( this );
    mLocating = false;
  }

  private void setGPSon()
  {
    // mBtnLoc.setText( mContext.getResources().getString( R.string.button_gps_stop ) );
    mHasLocation = false;
    mBtnStatus.setText( "0" );
    mBtnStatus.setBackgroundColor( 0x80ff0000 );
    locManager.addGpsStatusListener( this );
    locManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 0, this );
    mLocating = true;
  }

  @Override
  public void onLocationChanged( Location loc )
  {
    if ( loc != null ) displayLocation( loc );
    // mLocated = true;
  }

  public void onProviderDisabled( String provider )
  {
  }

  public void onProviderEnabled( String provider )
  {
  }

  public void onStatusChanged( String provider, int status, Bundle extras )
  {
    // TDLog.Log(TDLog.LOG_LOC, "onStatusChanged status " + status );
  }

  public void onGpsStatusChanged( int event ) 
  {
    if ( event == GpsStatus.GPS_EVENT_SATELLITE_STATUS ) {
      locManager.getGpsStatus( mStatus );
      Iterator< GpsSatellite > sats = mStatus.getSatellites().iterator();
      int  nr = 0;
      while( sats.hasNext() ) {
        GpsSatellite sat = sats.next();
        if ( sat.usedInFix() ) ++nr;
      }
      // TDLog.Log(TDLog.LOG_LOC, "onGpsStatusChanged nr satellites used in fix " + nr );
      mBtnStatus.setText( String.format(Locale.US, "%d", nr ) );
      switch ( nr ) {
        case 0: mBtnStatus.setBackgroundColor( 0x80ff0000 );
                break;
        case 1: mBtnStatus.setBackgroundColor( 0x80993333 );
                break;
        case 2: mBtnStatus.setBackgroundColor( 0x80663333 );
                break;
        case 3: mBtnStatus.setBackgroundColor( 0x80339933 );
                break;
        default: mBtnStatus.setBackgroundColor( 0x8000ff00 );
                break;
      }

      try {
        Location loc = locManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
        if ( loc != null ) displayLocation( loc );
      } catch ( IllegalArgumentException e ) {
        TDLog.Error( "onGpsStatusChanged IllegalArgumentException " );
      } catch ( SecurityException e ) {
        TDLog.Error( "onGpsStatusChanged SecurityException " );
      }
    }
  }

  // -----------------------------------------------------------

  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    if ( mLocating ) {
      locManager.removeUpdates( this );
      locManager.removeGpsStatusListener( this );
      mLocating = false;
    }
    dismiss();
  }

}

