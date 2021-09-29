/** @file GPS.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief 3D Topo-GL activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// WITH-GPS
import com.topodroid.utils.TDLog;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.location.GpsSatellite;
// import android.location.GpsStatus.Listener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.Iterator;

class GPS implements LocationListener
          , GpsStatus.Listener
{
  private LocationManager locManager = null;
  private GpsStatus mStatus;

  boolean mIsLocating;
  // boolean mHasLocation;
  private double mLat  = 0;  // decimal degrees
  private double mLng  = 0;  // decimal degrees
  private double mAlt  = 0;  // meters
  // private double mLat0 = 0;  // decimal degrees
  // private double mLng0 = 0;  // decimal degrees
  private int mLocCount;
  private double mLatSum;
  private double mLngSum;
  private double mAltSum;


  // private double mErr2 = -1; // location error [m]
  private int mNrSatellites = 0;
  private double mDelta;

  interface GPSListener
  {
    public void notifyLocation( double lng, double lat, double alt );
  }

  private GPSListener mListener = null;

  void setListener( GPSListener listener ) { mListener = listener; }

  void setDelta( double delta ) { if ( delta > 0 ) mDelta = delta; }


  public static boolean checkLocation( Context context )
  {
    // TDLog.Log( LOG_PERM, "check location" );
    // TDLog.v("PERM " + "Check location ");
    PackageManager pm = context.getPackageManager();
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED )
        && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)
        && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
  }

  GPS( Context ctx )
  {
    mDelta = 1.0e-7;
    mIsLocating = false;
    // mNrSatellites = 0;
    if ( checkLocation( ctx ) ) { // CHECK_PERMISSIONS
      locManager = (LocationManager) ctx.getSystemService( Context.LOCATION_SERVICE );
      if ( locManager != null ) {
        mStatus = locManager.getGpsStatus( null );
      }
    }
    // mHasLocation = false;
  }

  // private final double mW0 = 0.8;
  // private final double mW1 = 1 - mW0;
  // private final double mW2 = mW1 / mW0;
  // private final double DEG2RAD = Math.PI/180.0;
  private final double EARTH_A = 6378137.0; // approx earth radius [meter]

  private final double DELTA   = 5 * 180.0 / (Math.PI * EARTH_A);  // 4 meters

  // location is stored in decimal degrees 
  private void assignLocation( Location loc ) 
  {
    double lat = loc.getLatitude();  // decimal degree
    double lng = loc.getLongitude();
    double alt = loc.getAltitude();  // meters above WGS84 ellipsoid
    if ( Math.abs( lat - mLat ) + Math.abs( lng - mLng ) > DELTA ) {
      mLocCount ++;
      mLatSum += lat;
      mLngSum += lng;
      mAltSum += alt;
      if ( mLocCount > 10 ) {
        mLat = mLatSum / mLocCount;
        mLng = mLngSum / mLocCount;
        mAlt = mAltSum / mLocCount;
        resetSums();
        if ( mListener != null ) mListener.notifyLocation( mLng, mLat, mAlt );
      }
    } else {
      resetSums();
    }
  }

  // boolean getLocation( Vector3D v )
  // {
  //   if ( ! mHasLocation ) return false;
  //   v.x = mLng;
  //   v.y = mLat;
  //   v.z = 0; // FIXME
  //   mHasLocation = false;
  //   return true;
  // }

  @SuppressLint("MissingPermission")
  void setGPSoff()
  {
    if ( locManager != null ) {
      locManager.removeUpdates( this );
      locManager.removeGpsStatusListener( this );
    }
    mIsLocating = false;
    // mHasLocation = false;
  }

  @SuppressLint("MissingPermission")
  boolean setGPSon()
  {
    // mHasLocation = false;
    // mErr2 = -1; // restart location averaging
    resetSums();
    if ( locManager == null ) return false;
    locManager.addGpsStatusListener( this );
    locManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 0, this );
    mIsLocating = true;
    mNrSatellites = 0;
    return true;
  }

  private void resetSums()
  {
    mLocCount = 0;
    mLatSum = 0;
    mLngSum = 0;
    mAltSum = 0;
  }

  @Override
  public void onLocationChanged( Location loc )
  {
    if ( loc != null && mNrSatellites > 3 ) assignLocation( loc );
  }

  public void onProviderDisabled( String provider )
  {
  }

  public void onProviderEnabled( String provider )
  {
  }

  public void onStatusChanged( String provider, int status, Bundle extras )
  {
    // TDLog.v("GPS onStatusChanged status " + status );
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
      if ( locManager == null ) return;
      mNrSatellites = getNrSatellites();
      // TDLog.v("GPS Status Changed nr satellites used in fix " + mNrSatellites );
      if ( mNrSatellites > 4 ) {
        try {
          Location loc = locManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
          if ( loc != null ) assignLocation( loc );
        } catch ( IllegalArgumentException e ) {
          TDLog.Error("GL onGpsStatusChanged IllegalArgumentException " );
        } catch ( SecurityException e ) {
          TDLog.Error("GL onGpsStatusChanged SecurityException " );
        }
      }
    }
  }

}

// end WITH-GPS
