/* @file CBlock.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

// import androidx.annotation.RecentlyNonNull;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.TDUtil;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Locale;

public class CBlock
{
  private static final int[] colors = { // 0xffcccccc, 0xffffcccc, 0xffccccff
    TDColor.LIGHT_GRAY,
    TDColor.LIGHT_PINK,
    TDColor.LIGHT_BLUE
  };

  public long mId;
  public long mCalibId;
  public long gx;
  public long gy;
  public long gz;
  public long mx;
  public long my;
  public long mz;
  public long  mGroup;
  public float mBearing;  // computed compass [degrees]
  public float mClino;    // computed clino [degrees]
  public float mRoll;     // computed roll
  public float mError;    // error in the calibration algo associated to this data
  public long mStatus;

  private boolean mFarness; // farness from reference item (previous item of a group)
  // float   mFarCosine;  // cos(angle) of farness (default 0 is ok)
  private boolean mOffGroup;

  public boolean isSaturated()
  { 
    return ( mx >= 32768 || my >= 32768 || mz >= 32768 );
  }

  public boolean isGZero()
  {
    return ( gx == 0 && gy == 0 && gz == 0 );
  }

  public void setOffGroup( boolean b ) { mOffGroup = b; }
  public boolean isOffGroup() { return mOffGroup; }

  public void setFarness( boolean b ) { mFarness = b; }
  public boolean isFar() { return mFarness; }
  // float   getFarCosine() { return mFarCosine; }

  // void computeFarness( CBlock ref, float thr )
  // {
  //   mFarness = isFarFrom( ref.mBearing, ref.mClino, thr );
  // }

  public CBlock()
  {
    mId = 0;
    mCalibId = 0;
    gx = 0;
    gy = 0;
    gz = 0;
    mx = 0;
    my = 0;
    mz = 0;
    mGroup = 0;
    mError = 0.0f;
    mFarness = false;
  }

  public boolean isFarFrom( float b0, float c0, float thr )
  {
    computeBearingAndClino();
    float c = c0 * TDMath.DEG2RAD;
    float b = b0 * TDMath.DEG2RAD;
    TDVector v1 = new TDVector( (float)Math.cos(c) * (float)Math.cos(b), 
                            (float)Math.cos(c) * (float)Math.sin(b),
                            (float)Math.sin(c) );
    c = mClino   * TDMath.DEG2RAD; 
    b = mBearing * TDMath.DEG2RAD;
    TDVector v2 = new TDVector( (float)Math.cos(c) * (float)Math.cos(b), 
                            (float)Math.cos(c) * (float)Math.sin(b),
                            (float)Math.sin(c) );
    float mFarCosine = v1.dot(v2);
    return mFarCosine < thr; // 0.70: approx 45 degrees
  }

  public void setId( long id, long cid )
  {
    mId = id;
    mCalibId = cid;
  }
  // FIXME ZERO-DATA
  public void setGroupIfNonZero( long g ) { mGroup = isGZero() ? 0 : g; }

  public void setGroup( long g ) { mGroup = g; }
  public void setError( float err ) { mError = err; }

  public int color() 
  {
    if ( mGroup <= 0 ) return colors[0];
    return colors[ 1 + (int)(mGroup % 2) ];
  }

  public void setStatus( long s ) { mStatus = s; }

  public void setData( long gx0, long gy0, long gz0, long mx0, long my0, long mz0 )
  {
    gx = ( gx0 > TDUtil.ZERO ) ? gx0 - TDUtil.NEG : gx0;
    gy = ( gy0 > TDUtil.ZERO ) ? gy0 - TDUtil.NEG : gy0;
    gz = ( gz0 > TDUtil.ZERO ) ? gz0 - TDUtil.NEG : gz0;
    mx = ( mx0 > TDUtil.ZERO ) ? mx0 - TDUtil.NEG : mx0;
    my = ( my0 > TDUtil.ZERO ) ? my0 - TDUtil.NEG : my0;
    mz = ( mz0 > TDUtil.ZERO ) ? mz0 - TDUtil.NEG : mz0;
  } 

  public void computeBearingAndClino()
  {
    float f = TDUtil.FV;
    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter( sw );
    // pw.format("Locale.US, G %d %d %d M %d %d %d E %.2f", gx, gy, gz, mx, my, mz, mError );
    // TDLog.Log( TDLog.LOG_DATA, sw.getBuffer().toString() );
    TDVector g = new TDVector( gx/f, gy/f, gz/f );
    TDVector m = new TDVector( mx/f, my/f, mz/f );
    doComputeBearingAndClino( g, m );
  }

  public void computeBearingAndClino( CalibAlgo calib )
  {
    float f = TDUtil.FV;
    TDVector g = new TDVector( gx/f, gy/f, gz/f );
    TDVector m = new TDVector( mx/f, my/f, mz/f );
    TDVector g0 = calib.GetAG().timesV( g );
    TDVector m0 = calib.GetAM().timesV( m );
    TDVector g1 = calib.GetBG().plus( g0 );
    TDVector m1 = calib.GetBM().plus( m0 );
    doComputeBearingAndClino( g1, m1 );
  }

  private void doComputeBearingAndClino( TDVector g, TDVector m )
  {
    g.normalize();
    m.normalize();
    TDVector e = new TDVector( 1.0f, 0.0f, 0.0f ); // laser axis (DistoX frame)
    TDVector y = m.cross( g ); // west (DistoX frame)
    TDVector x = g.cross( y ); // north 
    y.normalize();
    x.normalize();
    float ex = e.dot( x ); // north component
    float ey = e.dot( y ); // west component
    float ez = e.dot( g ); // downward component
    mBearing =   TDMath.atan2( -ey, ex );
    mClino   = - TDMath.atan2( ez, (float)Math.sqrt(ex*ex+ey*ey) );
    mRoll    =   TDMath.atan2( g.y, g.z );
    if ( mBearing < 0.0f ) mBearing += TDMath.M_2PI;
    if ( mRoll < 0.0f ) mRoll += TDMath.M_2PI;
    mClino   *= TDMath.RAD2DEG;
    mBearing *= TDMath.RAD2DEG;
    mRoll    *= TDMath.RAD2DEG;
  }

  // @RecentlyNonNull
  public String toString()
  {
    float ua = TDSetting.mUnitAngle;

    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    computeBearingAndClino();
    pw.format(Locale.US, "%d <%d> %5.1f %5.1f %5.1f %4.2f",
      mId, mGroup, mBearing*ua, mClino*ua, mRoll*ua, mError*TDMath.RAD2DEG );
    if ( TDSetting.mRawCData == 1 ) {
      pw.format( "  %d %d %d  %d %d %d", gx, gy, gz, mx, my, mz );
    } else if ( TDSetting.mRawCData == 2 ) {
      pw.format( "  %04x %04x %04x  %04x %04x %04x", gx & 0xffff, gy & 0xffff, gz & 0xffff, mx & 0xffff, my & 0xffff, mz & 0xffff );
    } else if ( TDSetting.mRawCData == 11 ) {
      pw.format( "  %d %d %d", gx, gy, gz );
    } else if ( TDSetting.mRawCData == 12 ) {
      pw.format( "  %04x %04x %04x", gx & 0xffff, gy & 0xffff, gz & 0xffff );
    } else if ( TDSetting.mRawCData == 21 ) {
      pw.format( "  %d %d %d", mx, my, mz );
    } else if ( TDSetting.mRawCData == 22 ) {
      pw.format( "  %04x %04x %04x", mx & 0xffff, my & 0xffff, mz & 0xffff );
    }
    return sw.getBuffer().toString();
  }
}

