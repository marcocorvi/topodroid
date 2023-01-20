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
package com.topodroid.TDX;

// import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

// import com.topodroid.mag.Geodetic;
import com.topodroid.mag.WorldMagneticModel;

import java.util.Iterator;

import java.util.Locale;

// import android.app.Dialog;
import android.annotation.SuppressLint;
// import android.annotation.TargetApi;
// import android.os.Build;
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
import android.location.GnssStatus; // TODO

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
  private LocationManager mLocManager = null;
  private WorldMagneticModel mWMM;

  private TextView mTVlat;
  private TextView mTVlng;
  // private TextView mTVh_ell;
  private TextView mTVh_geo;
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
  private double mHEll = 0; // ellipsoid altitude [meters]
  private double mHGeo; // altimetric (geoid) altitude
  private boolean mLocStarted = false;
  private double mErrH = -1;
  private double mErrV = -1;
  // private double mErr  = -1; // location error [m]
  // private double mErr2H = -1; // H location error [m]
  // private double mErr2V = -1; // V location error [m]
  private boolean mHasLocation;
  private int mNrSatellites = 0;

  private double errMin = 1000;
  private double errMax =    0;
  private boolean errOk = false; // when error is getting stable: error decreases, then increases, then decreases again
                                 // or after one minute has passed
  private long retStart = -1L;

  private boolean useGps = TDandroid.BELOW_API_24;
  // private GpsStatus mGpsStatus = null;   // deprecated API-24 crash API-31
  // private GnssStatus mGnssStatus = null; // added API-24
  private GnssStatus.Callback mGnssStatusCallback = null; // added API-24
  private boolean mLocating; // whether is locating
  private boolean mGpsEnabled = false;

  private long mFineLocationTime = 60000L; // default 1 minute

  private MyKeyboard mKeyboard;

  @SuppressLint("MissingPermission")
  FixedGpsDialog(Context context, FixedActivity parent )
  {
    super( context, null, R.string.FixedGpsDialog ); // null app
    mParent = parent;
    if ( TDandroid.checkLocation( context ) ) { // CHECK_PERMISSIONS
      mLocManager = (LocationManager) mContext.getSystemService( Context.LOCATION_SERVICE );
      if ( mLocManager != null ) {
        if ( useGps /* TDandroid.BELOW_API_31 */ ) {
          // mGpsStatus = mLocManager.getGpsStatus( null );
          mGpsEnabled = true; // FIXME
        } else { // TODO
          mGpsEnabled = mLocManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
          // TDLog.v("GNSS gps enabled " + mGpsEnabled );
        }
      }
    }
    mHasLocation = false;
    mFineLocationTime = 1000L * TDSetting.mFineLocation;
    // mLocating = false;
    // TDLog.v(  "GPS Unit Location " + TopoDroidApp.mUnitLocation + " ddmmss " + TDUtil.DDMMSS );
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
    // mTVh_ell = (TextView) findViewById(R.id.h_ellipsoid  );   // ellipsoid
    mTVh_geo = (TextView) findViewById(R.id.h_geoid );    // geoid
    mTVerr = (TextView) findViewById(R.id.error );        // location error
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
    if ( mParent.hasFixed( name ) ) {
      mETstation.setError( mContext.getResources().getString( R.string.error_station_fixed ) );
      return false;
    }
    String comment = mETcomment.getText().toString();
    // if ( comment == null ) comment = "";
    mParent.addFixedPoint( name, mLng, mLat, mHEll, mHGeo, comment, FixedInfo.SRC_TOPODROID, mErrH, mErrV );
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
      // TDLog.v("GNSS locating " + mLocating );
      if ( mLocating ) {
        setGPSoff();
      } else {      
        setGPSon();
        errMin = 1000;
        errMax =    0;
        errOk  = false;
        retStart = -1L;
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
  // private final double mW2 = mW1 / mW0;
  // private final double mR = Geodetic.EARTH_A; // approx earth radius

  private void displayLocation( Location loc /*, boolean do_error*/ )
  {
    double err3 = 0;
    mErrH = -1;
    mErrV = -1;
    if ( ! mLocStarted ) {
      mLat  = loc.getLatitude();  // decimal degree
      mLng  = loc.getLongitude();
      mHEll = loc.getAltitude();  // meter
      mLocStarted = true;
      // mErr  = 1000;              // start with a large value
      // mErr2H = 1000;
      // mErr2V = 1000;
    } else {
      double lat0 = loc.getLatitude();
      double lng0 = loc.getLongitude();
      double hel0 = loc.getAltitude();
      double lat  = mW1 * lat0 + mW0 * mLat;
      double lng  = mW1 * lng0 + mW0 * mLng;
      double hell = mW1 * hel0 + mW0 * mHEll;
      if ( loc.hasAccuracy() ) {
        mErrH = loc.getAccuracy(); // meters
        err3 = mErrH;
        if ( TDandroid.BELOW_API_26 ) {
          mErrV = Math.abs( hel0 - mHEll );
        } else {
          if ( loc.hasVerticalAccuracy() ) {
            mErrV = loc.getVerticalAccuracyMeters();
            err3 = Math.sqrt( mErrV * mErrV + mErrH * mErrH );
          // } else {
          //   mErrV = 0;
          }
        }
      } else {
        err3 = -1;
        // mErrH =-1;
        // mErrV =-1;
      }
      mLat  = lat;
      mLng  = lng;
      mHEll = hell;
      if ( err3 > 0 ) {
        // double dlat = (lat0-mLat) * mR * TDMath.DEG2RAD;
        // double dlng = (lng0-mLng) * mR * TDMath.DEG2RAD * Math.cos( mLat * TDMath.DEG2RAD );
        // double dhel = hel0 - mHEll;
        // double err2H = dlat*dlat + dlng*dlng;
        // double err2V = dhel*dhel;
        // double err2  = err2H + err2V;
        // mErr2  = mW0 * mErr2  + mW2 * err2;
        // mErr2H = mW0 * mErr2H + mW2 * err2H;
        // mErr2V = mW0 * mErr2V + mW2 * err2V;
        // err3  = 10 * Math.sqrt( mErr2 );  // FIXME multiplied by 10
        // mErrH  = 10 * Math.sqrt( mErr2H );
        // mErrV  = 10 * Math.sqrt( mErr2V );
        if ( errOk ) {
          if ( err3 > errMax ) { 
            errMax = err3;
          } else if ( err3 < errMin ) {
            errMin = err3;
          }
        } else {
          if ( retStart < 0 ) {
            retStart = System.currentTimeMillis();
            TDToast.makeLong( R.string.location_start_fine );
          } else if ( System.currentTimeMillis() - retStart > mFineLocationTime ) {
            errOk  = true;
            errMin = err3;
            errMax = err3;
          }
        }
      }
    }
    mHGeo = mWMM.ellipsoidToGeoid( mLat, mLng, mHEll ); 
    mHasLocation = true;

    // mTVlng.setText( String.format( mContext.getResources().getString( R.string.fmt_longitude ), FixedInfo.double2string( mLng ) ) );
    // mTVlat.setText( String.format( mContext.getResources().getString( R.string.fmt_latitude ), FixedInfo.double2string( mLat ) ) );
    mTVlng.setText( String.format( mContext.getResources().getString( R.string.fmt_longitude_dd_dms ),
      FixedInfo.double2degree( mLng ), FixedInfo.double2ddmmss( mLng ) ) );
    mTVlat.setText( String.format( mContext.getResources().getString( R.string.fmt_latitude_dd_dms ),
      FixedInfo.double2degree( mLat ), FixedInfo.double2ddmmss( mLat ) ) );
    // mTVh_ell.setText( String.format(Locale.US, mContext.getResources().getString( R.string.fmt_h_ellipsoid ), mHEll ) );
    mTVh_geo.setText( String.format(Locale.US, mContext.getResources().getString( R.string.fmt_h_geoid ), mHGeo ) );
    if ( errOk && err3 >= 0 /* && err3 < (errMax + errMin)/2 */ ) { 
      if ( TDandroid.BELOW_API_26 ) {
        mTVerr.setText( String.format(Locale.US, mContext.getResources().getString( R.string.fmt_error_h ), mErrH ) ); // TODO only if mErrH >= 0
      } else if ( mErrV > 0 ) {
        mTVerr.setText( String.format(Locale.US, mContext.getResources().getString( R.string.fmt_error_m ), mErrH, mErrV ) );
      } else { 
        mTVerr.setText( String.format(Locale.US, mContext.getResources().getString( R.string.fmt_error_h ), mErrH ) );
      }
    } else {
      mTVerr.setText( R.string.error_m );
    }
    // if ( do_error ) {
    //   mTVerr.setTextColor( 0xff00ff00 );
    // } else {
    //   mTVerr.setTextColor( 0xff00ffff );
    // }
  }

  // -----------------------------------------------------------

  private void setGPSoff()
  {
    mBtnLoc.setText( mContext.getResources().getString( R.string.button_gps_start ) );
    mBtnStatus.setBackgroundColor( 0xff3366ff );
    stopLocating();
  }

  private void stopLocating()
  {
    if ( mLocManager != null ) {
      // TDLog.v("GNSS stop locating ");
      mLocManager.removeUpdates( this );
      if ( useGps /* TDandroid.BELOW_API_31 */ ) {
        mLocManager.removeGpsStatusListener( this );
      } else { // TODO
        mLocManager.unregisterGnssStatusCallback( mGnssStatusCallback ); // API_24
        mGnssStatusCallback = null;
      }
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
    mLocStarted = false;
    // mErr  = -1; // restart location averaging
    if ( mLocManager != null && mGpsEnabled ) {
      // TDLog.v("GNSS start locating ");
      if ( useGps /* TDandroid.BELOW_API_31 */ ) { 
        mLocManager.addGpsStatusListener( this );
      } else { // TODO
        if ( mGnssStatusCallback == null ) {
          mGnssStatusCallback = new GnssStatus.Callback() { // API_24
            @Override public void onFirstFix( int millis ) { TDLog.v("GNSS first fix " + millis ); }

            @Override public void onSatelliteStatusChanged( GnssStatus status )
            {
              int nr = status.getSatelliteCount(); // API_24
              int nr_sat = 0;
              for ( int k = 0; k<nr; ++k ) if ( status.usedInFix( k ) ) nr_sat++; // API_24
              // TDLog.v("GNSS satellites " + nr_sat );
              setNrSatellites( nr_sat );
            }

            @Override public void onStarted() { TDLog.v("GNSS started" ); }

            @Override public void  onStopped() { TDLog.v("GNSS stopped" ); }

          };       
        }
        mLocManager.registerGnssStatusCallback( mGnssStatusCallback ); // API_24
      }
      mLocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 0, this );
      mLocating = true;
    }
  }

  @Override
  public void onLocationChanged( Location loc )
  {
    // TDLog.v( "Location Changed nr satellites used in fix " + mNrSatellites );
    if ( loc != null && mNrSatellites > 3 ) displayLocation( loc /*, true */ );
    // mLocated = true;
  }

  @Override
  public void onProviderDisabled( String provider )
  {
  }

  @Override
  public void onProviderEnabled( String provider )
  {
  }

  @Override
  public void onStatusChanged( String provider, int status, Bundle extras )
  {
    // TDLog.v("onStatusChanged provider " + provider + " status " + status );
  }

  @SuppressLint("MissingPermission")
  private int getNrSatellites()
  {
    int  nr = 0;
    if ( useGps /* TDandroid.BELOW_API_31 */ ) {
      GpsStatus gps_status = mLocManager.getGpsStatus( null );
      Iterator< GpsSatellite > sats = gps_status.getSatellites().iterator();
      while( sats.hasNext() ) {
        GpsSatellite sat = sats.next();
        if ( sat.usedInFix() ) ++nr;
      }
    } else { // TODO
      // GnssStatus gnss_status = (new GnssStatus.Builder()).build();
      // nr = gnss_status.getSatelliteCount();
      // TDLog.v("GNSS satellites " + nr );
      nr = mNrSatellites;
    }
    return nr;
  }

  @Override
  public void onGpsStatusChanged( int event ) 
  {
    // TDLog.v("GNSS gps status change " + event );
    if ( event == GpsStatus.GPS_EVENT_SATELLITE_STATUS ) {
      // TDLog.v("GNSS gps status satellite change");
      if ( mLocManager == null ) {
        return;
      }
      setNrSatellites( getNrSatellites() );
    }
  }

  /** set the number of satellites (used in fix) and update interface accordingly
   * @param nr number of satellites
   */
  private void setNrSatellites( int nr )
  {
    mNrSatellites = nr;
    // TDLog.Log(TDLog.LOG_LOC, "onGpsStatusChanged nr satellites used in fix " + mNrSatellites );
    // TDLog.v( "GPS Status Changed nr satellites used in fix " + mNrSatellites );
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
        Location loc = mLocManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
        if ( loc != null ) displayLocation( loc /*, false*/ );
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
    stopLocating();
    dismiss();
  }

}

