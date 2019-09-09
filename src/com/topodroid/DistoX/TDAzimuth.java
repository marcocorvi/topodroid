/* @file TDAzimuth.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid azimuth reference
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

class TDAzimuth
{
  // ----------------------------------------------------------------
  static float mRefAzimuth  = SurveyInfo.EXTEND_NORMAL; // west to east
  static long  mFixedExtend = 0;

  static void resetRefAzimuth( final ShotWindow window, float azimuth )
  {
    // Log.v("DistoXE", "reset Ref Azimuth " + azimuth );
    mRefAzimuth  = azimuth;
    mFixedExtend = ( TDSetting.mAzimuthManual )? 1L : 0L;
    if ( window != null ) {
      window.runOnUiThread( new Runnable() { public void run() { window.setRefAzimuthButton(); } } );
    }
    // DrawingWindow does not have the RefAzimuth setting
  }

  // called by DistoXComm, ShotNewDialog, and setLegExtend
  static long computeLegExtend( double bearing )
  {
    if ( mFixedExtend == 0 ) {
      double ref = mRefAzimuth;
      while ( bearing < ref ) bearing += 360;
      bearing -= ref;
      if ( bearing <  90 - TDSetting.mExtendThr ) return +1L;
      if ( bearing > 270 + TDSetting.mExtendThr ) return +1L;
      if ( bearing >  90 + TDSetting.mExtendThr && bearing < 270 - TDSetting.mExtendThr ) return -1L;
      return 0L;
      // return ( bearing > 90 && bearing < 270 )? -1L : 1L; 
    } else if ( mFixedExtend == -1L ) {
      // bearing += 180; if ( bearing >= 360 ) bearing -= 360;
      bearing = TDMath.add180( bearing );
    }
    mRefAzimuth = (float)bearing;
    return mFixedExtend;
  }

  static long getFixedExtend() { return mFixedExtend; }
  static boolean isFixedExtend() { return mFixedExtend != 0; }

  // commented use for manually entered shots, 
  // commenetd used by by Compass/VisualTopo parser
  static long computeSplayExtend( double bearing )
  {
    while ( bearing < mRefAzimuth ) bearing += 360;
    bearing -= mRefAzimuth;
    return computeAbsoluteExtendSplay( bearing );
  }


  // @param b bearing [deg] in 0 .. 360
  // called only by computeSplayExtend
  static private long computeAbsoluteExtendSplay( double b )
  {
    if ( b >= 90 + TDSetting.mExtendThr && b <= 270 - TDSetting.mExtendThr ) return -1L;
    if ( b <= 90 - TDSetting.mExtendThr || b >= 270 + TDSetting.mExtendThr ) return 1L;
    return 0L;
  }

}
