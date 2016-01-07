/* @file LocationDialog.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid GPS-location for fixed stations: new-entry and listing
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Iterator;

import java.io.File;

import java.util.List;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
// import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

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


import android.util.Log;

public class LocationDialog extends Dialog
                            implements View.OnClickListener
                                     , View.OnLongClickListener
                                     , AdapterView.OnItemClickListener
                                     , TextView.OnEditorActionListener
                                     , LocationListener
                                     , GpsStatus.Listener
{
  // private boolean  mLocated;
  private LocationManager locManager;
  private Context mContext;
  private TopoDroidApp mApp;

  private TextView mTVlat;
  private TextView mTVlong;
  private TextView mTValt;
  private TextView mTVasl;
  private EditText mETstation;
  private Button   mBtnLoc;
  private Button   mBtnAdd;
  private Button   mBtnMobileTopographer;

  private Button   mBtnStatus;
  // private Button   mBtnCancel;

  boolean  mHasLocation;

  private ListView mList;
  private FixedAdapter mFixedAdapter;
  // private TextView  mSaveTextView;
  private int mSavePos;
  private FixedInfo mSaveFixed;

  double mLatitude;   // decimal degrees
  double mLongitude;  // decimal degrees
  double mHEllipsoid;   // meters
  double mHGeoid; // altimetric altitude
  private SurveyActivity mParent;
  private GpsStatus mStatus;
  private boolean mLocating; // whether is locating

  private MyKeyboard mKeyboard;

  public LocationDialog( Context context, SurveyActivity parent, TopoDroidApp app, LocationManager lm )
  {
    super(context);
    mContext = context;
    mParent = parent;
    mApp = app;
    locManager = lm;
    mStatus = locManager.getGpsStatus( null );
    // mLocating = false;
    // mHasLocation = false;
    // Log.v(  TopoDroidApp.TAG, "UnitLocation " + TopoDroidApp.mUnitLocation + " ddmmss " + TopoDroidApp.DDMMSS );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_LOC, "Location onCreate" );
    setContentView(R.layout.location_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mTVlong = (TextView) findViewById(R.id.longitude );
    mTVlat  = (TextView) findViewById(R.id.latitude  );
    mTValt  = (TextView) findViewById(R.id.h_ellipsoid  );   // ellipsoid
    mTVasl  = (TextView) findViewById(R.id.h_geoid ); // geoid
    mETstation = (EditText) findViewById( R.id.station );
    // mBtnStatus = (Button) findViewById( R.id.status );

    mETstation.setOnEditorActionListener( this );
    mETstation.setOnLongClickListener( this );

    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );

    mBtnLoc = (Button) findViewById( R.id.button_loc );
    mBtnStatus = mBtnLoc;
    mBtnAdd = (Button) findViewById(R.id.button_add );
    mBtnMobileTopographer = (Button) findViewById( R.id.button_mobile_topographer );

    File MTdir = new File(MobileTopographerDialog.POINTLISTS);
    if ( ! MTdir.exists() ) {
      mBtnMobileTopographer.setVisibility( View.GONE );
    } else {
      mBtnMobileTopographer.setOnClickListener( this );
    }

    mBtnLoc.setOnClickListener( this );
    mBtnAdd.setOnClickListener( this );

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    // mBtnLoc.setEnabled( false );
    // mBtnAdd.setEnabled( false );
    // mBtnStatus.setBackgroundColor( 0x80ff0000 );
    // mBtnLoc.setText( getResources.getString( R.string.button_gps_start ) );
    
    // mLocated = false;
    // locManager = (LocationManager) getSystemService( LOCATION_SERVICE );
    mLocating = false;
    mHasLocation = false;
    setTitle( R.string.title_location );

    refreshList();

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
        mETstation.setInputType( TopoDroidConst.NUMBER_DECIMAL );
      }
    }
  }

  public void refreshList()
  {
    List< FixedInfo > fxds = mApp.mData.selectAllFixed( mApp.mSID, TopoDroidApp.STATUS_NORMAL );
    // TDLog.Log( TDLog.LOG_DEBUG, "Location::refreshList size " + fxds.size() );
    mFixedAdapter = new FixedAdapter( mContext, R.layout.message, fxds );
    mList.setAdapter( mFixedAdapter );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TDLog.Log( TDLog.LOG_LOC, "Location::onItemClick pos " + pos );
    // CharSequence item = ((TextView) view).getText();
    // String value = item.toString();
    // // setListPos( position  );
    // mSaveTextView = (TextView) view;
    mSaveFixed = mFixedAdapter.get(pos);
    mSavePos   = pos;
    (new FixedDialog( mContext, mParent, this, mSaveFixed )).show();
  }

  //  public void updatePos( )
  //  {
  //    // mFixedAdapter.insert( mSaveFixed, mSavePos );
  //    // mList.invalidate();
  //    refreshList();
  //  }

  public void addFixedPoint( double lng, // decimal degrees
                             double lat,
                             double alt,  // meters
                             double asl
                           )
  {
    // Log.v("DistoX", "Location add Fixed Point " + lng + " " + lat + " " + alt );

    String name = mETstation.getText().toString();
    if ( name.length() == 0 ) {
      // mETstation.setError( mContext.getResources().getString( R.string.error_station_required ) );
      return;
    }
    if ( mParent.hasLocation( name ) ) {
      // mETstation.setError( mContext.getResources().getString( R.string.error_station_already_fixed ) );
      return;
    }
    FixedInfo f = mParent.addLocation( name, lng, lat, alt, asl );
    if ( f != null ) {
      // no need to update the adatper: fixeds are not many and can just request the list to the database 
      mETstation.setText(name);
      mFixedAdapter.add( f );
      mList.invalidate();
      // refreshList();
      mHasLocation = false;
    }
  }

  private void setGPSoff()
  {
    mBtnLoc.setText( mContext.getResources().getString( R.string.button_gps_start ) );
    locManager.removeUpdates( this );
    locManager.removeGpsStatusListener( this );
    mLocating = false;
    setTitle( R.string.title_location );
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
    setTitle( R.string.title_location_gps );
  }

  @Override
  public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "Location onEditorAction " + actionId );
    // if ( actionId == 6 )
    {
      if ( (EditText)v == mETstation ) {
        if ( mLocating ) {
          setGPSoff();
        }
        mBtnLoc.setEnabled( false );
        mHasLocation = false;
        mBtnAdd.setEnabled( false );
        mBtnMobileTopographer.setEnabled( false );
        CharSequence item = mETstation.getText();
        if ( item != null && item.length() > 0 ) {
          String str = item.toString();
          // check if station has already a location
          if ( mApp.mData.hasFixedStation( -1L, mApp.mSID, str ) ) {
            String error = mContext.getResources().getString( R.string.error_station_already_fixed );
            mETstation.setError( error );
            return false;
          }
          boolean enabled =  ( str != null && str.length() > 0 );
          mBtnLoc.setEnabled( enabled );
          mBtnAdd.setEnabled( enabled );
          mBtnMobileTopographer.setEnabled( enabled );
        }
      }
    }
    return false;
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

    Button b = (Button) v;
    // if ( b == mBtnCancel ) {
    //   onBackPressed();
    // }
    String station = mETstation.getText().toString();
    if ( station == null || station.length() == 0 ) {
      mETstation.setError( mContext.getResources().getString( R.string.error_station_required ) );
      return;
    }
    if ( mParent.hasLocation( station ) ) {
      mETstation.setError( mContext.getResources().getString( R.string.error_station_already_fixed ) );
      return;
    }
    if ( b == mBtnAdd ) {
      // stop GPS location and start dialog for lat/long/alt data
      if ( TDSetting.mKeyboard ) mKeyboard.hide();
      if ( mLocating ) {
        setGPSoff();
      }
      CharSequence item = mETstation.getText();
      if ( item == null || item.length() == 0 ) {
        String error = mContext.getResources().getString( R.string.error_station_required );
        mETstation.setError( error );
        return;
      }
      if ( mHasLocation ) {
        WorldMagneticModel wmm = new WorldMagneticModel( mContext );
        mHGeoid = wmm.ellipsoidToGeoid( mLatitude, mLongitude, mHEllipsoid ); 
      }
      new LongLatAltDialog( mContext, this ).show();
      // mHasLocation = false;
    } else if ( b == mBtnMobileTopographer ) {
      (new MobileTopographerDialog( mParent, this )).show();
    } else if ( b == mBtnLoc ) {
      if ( TDSetting.mKeyboard ) mKeyboard.hide();
      if ( mLocating ) {
        setGPSoff();
      } else {      
        // FIXME GPS_AVERAGE NOT USED
        // if ( TDSetting.mUseGPSAveraging ) {
        //   if ( ! mParent.tryGPSAveraging( this ) ) {
        //     TDLog.Error( "Location: failed GPSAveraging" );
        //   }
        // } else {
          setGPSon();
        // }
      }
    }
    // refreshList();
  }


  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;

    if ( TDSetting.mKeyboard ) {
      if ( mKeyboard.isVisible() ) {
        mKeyboard.hide();
        return;
      }
    }
    if ( mLocating ) {
      locManager.removeUpdates( this );
      locManager.removeGpsStatusListener( this );
      mLocating = false;
    }
    dismiss();
  }

  @Override
  public void onLocationChanged( Location loc )
  {
    if ( loc != null ) displayLocation( loc );
    // mLocated = true;
  }

  /** location is stored in decimal degrees but displayed as deg:min:sec
   * N.B. the caller must check loc != null
   */
  private void displayLocation( Location loc )
  {
    mLatitude    = loc.getLatitude();
    mLongitude   = loc.getLongitude();
    mHEllipsoid  = loc.getAltitude();
    showLocation();
  }

  void showLocation()
  {
    mTVlong.setText( mContext.getResources().getString( R.string.longitude ) + " " + FixedInfo.double2string( mLongitude ) );
    mTVlat.setText( mContext.getResources().getString( R.string.latitude ) + " " + FixedInfo.double2string( mLatitude ) );
    mTValt.setText( mContext.getResources().getString( R.string.h_ellipsoid ) + " " + Integer.toString( (int)(mHEllipsoid) ) );
    mTVasl.setText( mContext.getResources().getString( R.string.h_geoid ) + " " + Integer.toString( (int)(mHGeoid) ) );
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
      mBtnStatus.setText( Integer.toString( nr ) );
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
        mHasLocation = (loc != null);
        if ( mHasLocation ) displayLocation( loc );
      } catch ( IllegalArgumentException e ) {
        TDLog.Error( "onGpsStatusChanged IllegalArgumentException " );
      } catch ( SecurityException e ) {
        TDLog.Error( "onGpsStatusChanged SecurityException " );
      }
    }
  }

  /** callback fro SurveyActivity on GPS request result
   */
  void setPosition( double lng, double lat, double alt, double asl )
  {
    mLongitude = lng;
    mLatitude  = lat;
    mHEllipsoid  = alt;
    mHGeoid = asl;
    showLocation();
    mHasLocation = true;
  }
}

