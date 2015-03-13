/* @file CalibCBlock.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 angle units
 * 20120725 TopoDroidApp log
 * 20140329 raw data (optionally)
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Locale;

public class CalibCBlock
{
  private static final float grad2rad = TopoDroidUtil.GRAD2RAD;

  private static int[] colors = { 0xffcccccc, 0xffffcccc, 0xffccccff };

  public long mId;
  public long mCalibId;
  public long gx;
  public long gy;
  public long gz;
  public long mx;
  public long my;
  public long mz;
  public long  mGroup;
  public float mBearing;  // computed compass
  public float mClino;    // computed clino
  public float mRoll;     // computed roll
  public float mError;    // error in the calibration algo associated to this data
  public long mStatus;

  boolean isSaturated()
  { 
    return ( mx >= 32768 || my >= 32768 || mz >= 32768 );
  }

  public CalibCBlock()
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
  }

  public boolean isFarFrom( float b0, float c0, float thr )
  {
    computeBearingAndClino();
    float c = c0 * grad2rad;
    float b = b0 * grad2rad;
    Vector v1 = new Vector( (float)Math.cos(c) * (float)Math.cos(b), 
                            (float)Math.cos(c) * (float)Math.sin(b),
                            (float)Math.sin(c) );
    c = mClino   * grad2rad; 
    b = mBearing * grad2rad;
    Vector v2 = new Vector( (float)Math.cos(c) * (float)Math.cos(b), 
                            (float)Math.cos(c) * (float)Math.sin(b),
                            (float)Math.sin(c) );
    float x = v1.dot(v2);
    return x < thr; // 0.70: approx 45 degrees
  }

  public void setId( long id, long cid ) 
  {
    mId = id;
    mCalibId = cid;
  }
  public void setGroup( long g ) { mGroup = g; }
  public void setError( float err ) { mError = err; }

  public int color() 
  {
    if ( mGroup <= 0 ) return colors[0];
    return colors[ 1 + (int)(mGroup % 2) ];
  }

  void setStatus( long s ) { mStatus = s; }

  public void setData( long gx0, long gy0, long gz0, long mx0, long my0, long mz0 )
  {
    gx = ( gx0 > TopoDroidUtil.ZERO ) ? gx0 - TopoDroidUtil.NEG : gx0;
    gy = ( gy0 > TopoDroidUtil.ZERO ) ? gy0 - TopoDroidUtil.NEG : gy0;
    gz = ( gz0 > TopoDroidUtil.ZERO ) ? gz0 - TopoDroidUtil.NEG : gz0;
    mx = ( mx0 > TopoDroidUtil.ZERO ) ? mx0 - TopoDroidUtil.NEG : mx0;
    my = ( my0 > TopoDroidUtil.ZERO ) ? my0 - TopoDroidUtil.NEG : my0;
    mz = ( mz0 > TopoDroidUtil.ZERO ) ? mz0 - TopoDroidUtil.NEG : mz0;
  } 

  public void computeBearingAndClino()
  {
    float f = TopoDroidUtil.FV;
    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter( sw );
    // pw.format("Locale.ENGLISH, G %d %d %d M %d %d %d E %.2f", gx, gy, gz, mx, my, mz, mError );
    // TopoDroidLog.Log( TopoDroidLog.LOG_DATA, sw.getBuffer().toString() );
    Vector g = new Vector( gx/f, gy/f, gz/f );
    Vector m = new Vector( mx/f, my/f, mz/f );
    doComputeBearingAndClino( g, m );
  }

  public void computeBearingAndClino( Calibration calib )
  {
    float f = TopoDroidUtil.FV;
    Vector g = new Vector( gx/f, gy/f, gz/f );
    Vector m = new Vector( mx/f, my/f, mz/f );
    Vector g0 = calib.GetAG().timesV( g );
    Vector m0 = calib.GetAM().timesV( m );
    Vector g1 = calib.GetBG().plus( g0 );
    Vector m1 = calib.GetBM().plus( m0 );
    doComputeBearingAndClino( g1, m1 );
  }

  private void doComputeBearingAndClino( Vector g, Vector m )
  {
    g.Normalized();
    m.Normalized();
    Vector e = new Vector( 1.0f, 0.0f, 0.0f );
    Vector y = m.cross( g );
    Vector x = g.cross( y );
    y.Normalized();
    x.Normalized();
    float ex = e.dot( x );
    float ey = e.dot( y );
    float ez = e.dot( g );
    mBearing = (float)Math.atan2( -ey, ex );
    mClino   = - (float)Math.atan2( ez, (float)Math.sqrt(ex*ex+ey*ey) );
    mRoll    = (float)Math.atan2( g.y, g.z );
    if ( mBearing < 0.0f ) mBearing += 2*TopoDroidUtil.M_PI;
    if ( mRoll < 0.0f ) mRoll += 2*TopoDroidUtil.M_PI;
    mClino   *= TopoDroidUtil.RAD2GRAD;
    mBearing *= TopoDroidUtil.RAD2GRAD;
    mRoll    *= TopoDroidUtil.RAD2GRAD;
  }

  public String toString()
  {
    float ua = TopoDroidSetting.mUnitAngle;

    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    computeBearingAndClino();
    pw.format(Locale.ENGLISH, "%d <%d> %5.1f %5.1f %5.1f %6.4f",
      mId, mGroup, mBearing*ua, mClino*ua, mRoll*ua, mError );
    if ( TopoDroidSetting.mRawData ) {
      pw.format( "  %d %d %d  %d %d %d", gx, gy, gz, mx, my, mz );
    }
    return sw.getBuffer().toString();
  }
}

