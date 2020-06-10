/* @file FixedGpsDialog.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid GPS-location for fixed stations
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import com.topodroid.mag.Geodetic;
import com.topodroid.mag.WorldMagneticModel;

// import android.util.Log;

import java.util.Iterator;

// import java.io.File;

// import java.util.List;
import java.util.Locale;

// import android.app.Dialog;
import android.annotation.SuppressLint;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;

// import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
// import android.view.ViewGroup.LayoutParams;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;
// import android.widget.TextView.OnEditorActionListener;

import android.view.View;
// import android.widget.ListView;
import android.inputmethodservice.KeyboardView;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.location.GpsSatellite;
// import android.location.GpsStatus.Listener;

import android.net.Uri;

class FixedGpsDialog extends MyDialog
                            implements View.OnClickListener
                                     , View.OnLongClickListener
                                     , TextView.OnEditorActionListener
                                     , LocationListener
                                     , GpsStatus.Listener
{
  private final FixedActivity mParent;
  // private boolean  mLocated;
  private LocationManager locManager = null;
  private WorldMagneticModel mWMM;

  private TextView mTVlat;
  private TextView mTVlng;
  private TextView mTValt;
  private TextView mTVasl;
  private TextView mTVerr;
  private EditText mETstation;
  private EditText mETcomment;

  private Button   mBtnLoc;
  private Button   mBtnAdd;
  private Button   mBtnView;

  private Button   mBtnStatus;
  // private Button   mBtnCancel;

  private double mLat = 0;  // decimal degrees
  private double mLng = 0;  // decimal degrees
  private double mHEll = 0; // meters
  private double mHGeo; // altimetric altitude
  private double mErr2 = -1; // location error [m]
  private boolean mHasLocation;
  private int mNrSatellites = 0;

  private GpsStatus mStatus;
  private boolean mLocating; // whether is locating

  private MyKeyboard mKeyboard;

  @SuppressLint("MissingPermission")
  FixedGpsDialog(Context context, FixedActivity parent )
  {
    super(context, R.string.FixedGpsDialog );
    mParent = parent;
    if ( TDandroid.checkLocation( context ) ) { // CHECK_PERMISSIONS
      locManager = (LocationManager) mContext.getSystemService( Context.LOCATION_SERVICE );
      if ( locManager != null ) {
        mStatus = locManager.getGpsStatus( null );
      }
    }
    mHasLocation = false;
    // mLocating = false;
    // Log.v(  TopoDroidApp.TAG, "UnitLocation " + TopoDroidApp.mUnitLocation + " ddmmss " + TDUtil.DDMMSS );
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
    mTVerr = (TextView) findViewById(R.id.error );          // location error
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


  private boolean addFixedPoint( )
  {
    String name = mETstation.getText().toString();
    if ( name.length() == 0 ) {
      mETstation.setError( mContext.getResources().getString( R.string.error_station_required ) );
      return false;
    }
    String comment = mETcomment.getText().toString();
    // if ( comment == null ) comment = "";
    mParent.addFixedPoint( name, mLng, mLat, mHEll, mHGeo, comment, FixedInfo.SRC_TOPODROID );
    return true;
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
  //         if ( mApp.mData.hasFixedStation( -1L, TDInstance.sid, str ) ) {
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
        if ( addFixedPoint() ) dismiss();
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
      TDToast.makeBad( R.string.no_location_data );
    }
  }


  /** location is stored in decimal degrees but displayed as deg:min:sec
   * N.B. the caller must check loc != null
   */
  private final double mW0 = 0.8;
  private final double mW1 = 1 - mW0;
  private final double mW2 = mW1 / mW0;
  private final double mR = Geodetic.EARTH_A; // approx earth radius

  private void displayLocation( Location loc /*, boolean do_error*/ )
  {
    double ret = 0;
    if ( mErr2 < 0 ) {	  
      mLat  = loc.getLatitude();  // decimal degree
      mLng  = loc.getLongitude();
      mHEll = loc.getAltitude();  // meter
      mErr2 = 10000;              // start with a large value
    } else {
      double lat0 = loc.getLatitude();
      double lng0 = loc.getLongitude();
      double hel0 = loc.getAltitude();
      double lat  = mW1 * lat0 + mW0 * mLat;
      double lng  = mW1 * lng0 + mW0 * mLng;
      double hell = mW1 * hel0 + mW0 * mHEll;
      double dlat = (lat0-mLat) * mR * TDMath.DEG2RAD;
      double dlng = (lng0-mLng) * mR * TDMath.DEG2RAD * Math.cos( mLat * TDMath.DEG2RAD );
      double dhel = hel0 - mHEll;
      double err2 = ( dlat*dlat + dlng*dlng + dhel*dhel );
      mErr2 = mW0 * mErr2 + mW2 * err2;
      mLat  = lat;
      mLng  = lng;
      mHEll = hell;
      ret   = Math.sqrt( mErr2 );

      if ( ret < 1 ) {
	ret = 1; // FIXME hard lower bound
      } else if ( ret > 100 ) {
	ret = 100;
      }
    }
    mHGeo = mWMM.ellipsoidToGeoid( mLat, mLng, mHEll ); 
    mHasLocation = true;

    mTVlng.setText( String.format( mContext.getResources().getString( R.string.fmt_longitude ), FixedInfo.double2string( mLng ) ) );
    mTVlat.setText( String.format( mContext.getResources().getString( R.string.fmt_latitude ), FixedInfo.double2string( mLat ) ) );
    mTValt.setText( String.format(Locale.US, mContext.getResources().getString( R.string.fmt_h_ellipsoid ), mHEll ) );
    mTVasl.setText( String.format(Locale.US, mContext.getResources().getString( R.string.fmt_h_geoid ), mHGeo ) );
    mTVerr.setText( String.format(Locale.US, mContext.getResources().getString( R.string.fmt_error_m ), ret ) );
    // if ( do_error ) {
    //   mTVerr.setTextColor( 0xff00ff00 );
    // } else {
    //   mTVerr.setTextColor( 0xff00ffff );
    // }
    // return ret;
  }

  // -----------------------------------------------------------

  private void setGPSoff()
  {
    mBtnLoc.setText( mContext.getResources().getString( R.string.button_gps_start ) );
    mBtnStatus.setBackgroundColor( 0xff3366ff );
    if ( locManager != null ) {
      locManager.removeUpdates( this );
      locManager.removeGpsStatusListener( this );
    }
    mLocating = false;
  }

  @SuppressLint("MissingPermission")
  private void setGPSon()
  {
    // mBtnLoc.setText( mContext.getResources().getString( R.string.button_gps_stop ) );
    mHasLocation = false;
    mBtnStatus.setText( TDString.ZERO );
    mBtnStatus.setBackgroundColor( 0x80ff0000 );
    mErr2 = -1; // restart location averaging
    if ( locManager != null ) {
      locManager.addGpsStatusListener( this );
      locManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 0, this );
      mLocating = true;
    }
  }

  @Override
  public void onLocationChanged( Location loc )
  {
    // Log.v("DistoX", "Location Changed nr satellites used in fix " + mNrSatellites );
    if ( loc != null && mNrSatellites > 3 ) displayLocation( loc /*, true */ );
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

  @SuppressLint("MissingPermission")
  private int getNrSatellites()
  {
    locManager.getGpsStatus( mStatus );
    Iterator< GpsSatellite > sats = mStatus.getSatellites().iterator();
    int  nr = 0;
    while( sats.hasNext() ) {
      GpsSatellite sat = sats.next();
      if ( sat.usedInFix() ) ++nr;
    }
    return nr;
  }

  public void onGpsStatusChanged( int event ) 
  {
    if ( event == GpsStatus.GPS_EVENT_SATELLITE_STATUS ) {
      if ( locManager == null ) {
        return;
      }
      mNrSatellites = getNrSatellites();
      // TDLog.Log(TDLog.LOG_LOC, "onGpsStatusChanged nr satellites used in fix " + mNrSatellites );
      // Log.v("DistoX", "GPS Status Changed nr satellites used in fix " + mNrSatellites );
      mBtnStatus.setText( String.format(Locale.US, "%d", mNrSatellites ) );

      switch ( mNrSatellites ) {
        case 0: mBtnStatus.setBackgroundColor( 0x80ff0000 );
                break;
        case 1: mBtnStatus.setBackgroundColor( 0x80993333 );
                break;
        case 2: mBtnStatus.setBackgroundColor( 0x80666633 );
                break;
        case 3: mBtnStatus.setBackgroundColor( 0x80339933 );
                break;
        default: mBtnStatus.setBackgroundColor( 0x8000ff00 );
                break;
      }

      if ( mNrSatellites > 3 ) {
        try {
          Location loc = locManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
          if ( loc != null ) displayLocation( loc /*, false*/ );
        } catch ( IllegalArgumentException e ) {
          TDLog.Error( "onGpsStatusChanged IllegalArgumentException " );
        } catch ( SecurityException e ) {
          TDLog.Error( "onGpsStatusChanged SecurityException " );
        }
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
      if ( locManager != null ) {
        locManager.removeUpdates( this );
        locManager.removeGpsStatusListener( this );
      }
      mLocating = false;
    }
    dismiss();
  }

}

