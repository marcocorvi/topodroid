/* @file CalibAlgo.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration algorithm
 *
 * The calibration transform maps the G-M values, Gs and Ms, in the (non-orthogonal) sensor frame
 * to the values, Md and Gd, in the (orthogonal) device frame (with X axis aligned to the laser direction):
 *
 *    Md = Am Ms + Bm
 *    Gd = Ag Gs + Bg
 * 
 * The G-transform can have a non-linear term, a contribution  nl_i * Gs_i ^ 2 to the i-th
 * component of Gd
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;
// import com.topodroid.prefs.TDSetting;

import java.lang.Math;

// import java.util.Locale;

// used by logCoeff

public class CalibAlgo extends CalibTransform
{
  protected TDVector[] g = null;
  protected TDVector[] m = null;
  protected long[] group = null;
  protected float[] err = null;  // errors of the data [radians] 

  protected int idx;
  protected int num;

  protected float mDelta    = 0.0f; // average data error [degrees]
  protected float mDelta2   = 0.0f; // std-dev data error [degrees]
  protected float mMaxError = 0.0f; // max error [degrees]
  protected float mDeltaBH  = 0.0f; // original delta BH algo
  protected float mDip      = 0.0f; // magnetic dip angle [degrees]
  protected float mRoll     = 0.0f; // average roll discrepancy [degrees]

  // ==============================================================


  /** construct a CalibAlgo from the saved coefficients
   * @param coeff   coefficients array
   * @param nonLinear whether the transform is non-linear
   */
  public CalibAlgo( byte[] coeff, boolean nonLinear )
  {
    super( coeff, nonLinear );
  }

  /** cstr
   * @param N         number of data
   * @param nonLinear whether to use non-linear algo
   */
  public CalibAlgo( int N, boolean nonLinear )
  {
    super( nonLinear );
    num = 0;
    if ( N > 0 ) Reset( N );
  }

  float computeRoll( TDVector v0, TDVector v )
  {
    float s = v0.y * v.z - v0.z * v.y;
    float c = v0.y * v.y + v0.z * v.z;
    float d = TDMath.sqrt( s*s + c*c );
    float r = TDMath.atan2( s/d, c/d ) * TDMath.RAD2DEG;
    return ( r < 0 )? r + 360 : r;
  }

  /** compute the maximum roll difference between G vector and M vector of groups
   */
  public void rollDifference()
  {
    // float max = 0;
    int ns = g.length;
    long grp = -1L;
    TDVector g10 = null;
    TDVector m10 = null;
    int n = 0;
    mRoll = 0;
    for ( int k = 0; k < ns; ++k ) {
      if ( group[k] <= 0 ) continue;
      if ( group[k] != grp ) {
        g10 = getTransformedG( g[k] );
        m10 = getTransformedM( m[k] );
        grp = group[k];
      } else {
        TDVector g1 = getTransformedG( g[k] );
        TDVector m1 = getTransformedM( m[k] );
        if ( g10 != null ) {
          float g_roll = computeRoll( g10, g1 );
          float m_roll = computeRoll( m10, m1 );
          float x = TDMath.abs( g_roll - m_roll );
          // if ( x > max ) max = x;
          mRoll += x;
          n ++;
        }
      }
    }
    mRoll /= n;
    // TDLog.v("Max roll difference " + max + " average " + roll );
    // return roll;
  }
    

  // void setAlgorithm( boolean nonLinear ) { mNonLinear = nonLinear; }

  /** @return the BH delta value
   */
  public float DeltaBH()      { return mDeltaBH; }

  /** @return the delta value
   */
  public float Delta() { return mDelta; }

  /** @return the delta2 value
   */
  public float Delta2() { return mDelta2; }

  // public float Error( int k ) { return err[k]; }

  /** @return the array of errors
   */
  public float[] Errors() { return err; }

  /** @return the maximum error
   */
  public float MaxError( ) { return mMaxError; }

  /** @return the dip angle [degrees]
   */
  public float Dip() { return mDip; }

  /** @return the average roll discrepancy [degrees] 
   */
  public float Roll() { return mRoll; } // FIXME ROLL_DIFFERENCE

  // public int nrCoeff() { return mNonLinear ? 52 : 48; }

  /** add a calib data
   * @param b calib data
   */
  public void AddValues( CBlock b )
  {
    // add also group-0 CBlocks to keep CBlock list and calib vectors aligned
    AddValues( b.gx, b.gy, b.gz, b.mx, b.my, b.mz, b.mGroup );
  }

  /** insert G-M values
   * @param gx   X-component of G
   * @param gy   Y-component of G
   * @param gz   Z-component of G
   * @param mx   X-component of M
   * @param my   Y-component of M
   * @param mz   Z-component of M
   * @param group0 group (if positive - otherwise the group is set to 0)
   */
  private void AddValues( long gx, long gy, long gz, long mx, long my, long mz, long group0 )
  {
    if ( idx >= num ) {
      return;
    }
    // g[idx] = new TDVector( gx/TDUtil.FV, gy/TDUtil.FV, gz/TDUtil.FV );
    // m[idx] = new TDVector( mx/TDUtil.FV, my/TDUtil.FV, mz/TDUtil.FV );
    g[idx] = scaledVector( new TDVector(gx, gy, gz) );
    m[idx] = scaledVector( new TDVector(mx, my, mz) );
    group[idx] = (group0 < 0)? 0 : group0;

    // if ( TDLog.LOG_CALIB ) {
    //   TDLog.DoLog(
    //     String.format(Locale.US, "Add %d G %d %d %d M %d %d %d Grp %d", idx, gx, gy, gz, mx, my, mz, group0 ) );
    // }
    idx ++;
  }

  // public int Size() { return idx; }

  /** reset the number of data
   * @param N   new number of data
   */
  public void Reset( int N )
  {
    if ( N != num ) {
      num = N;
      g = new TDVector[N];
      m = new TDVector[N];
      group = new long[N];
      err   = new float[N];
    }
    idx = 0;
    aG = null;
    bG = null;
    aM = null;
    bM = null;
    // TDLog.Log( TDLog.LOG_CALIB, "Reset calibration " + N + " data");
  }
    
/* ============================================================ */

  // NO_LOGS
  protected void LogNumber( String msg, int it )
  {
    // TDLog.Log( TDLog.LOG_CALIB, msg + " " + it );
  }

  // NO_LOGS
  protected void LogMatrixVector( String msg, TDMatrix m1, TDVector v1 ) 
  {
    // if ( ! TDLog.LOG_CALIB ) return;
    // TDLog.DoLog(
    //   msg + String.format(Locale.US,
    //    " M: %8.4f %8.4f %8.4f V: %8.4f\n    %8.4f %8.4f %8.4f   %8.4f\n    %8.4f %8.4f %8.4f   %8.4f",
    //    m1.x.x, m1.x.y, m1.x.z, v1.x, 
    //    m1.y.x, m1.y.y, m1.y.z, v1.y, 
    //    m1.z.x, m1.z.y, m1.z.z, v1.z ) );
  }

  // NO_LOGS
  protected void LogVectors( String msg, long group, TDVector v1, TDVector v2 )
  {
    // if ( ! TDLog.LOG_CALIB ) return;
    // TDLog.DoLog(
    //   msg + String.format(Locale.US,
    //   " %3d V1 %8.4f %8.4f %8.4f\n    V2 %8.4f %8.4f %8.4f", group, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z ) ); 
  }

  // NO_LOGS
  protected void LogSC( String msg, float s, float c )
  {
    // if ( ! TDLog.LOG_CALIB ) return;
    // TDLog.DoLog( msg + String.format(Locale.US, " S %8.4f C %8.4f", s, c ) );
  }

/* ============================================================ */

  // error accumulators
  protected int    mSumCount;
  protected double mSumErrors;
  protected double mSumErrorSquared;

  public int    getStatCount()   { return mSumCount; }
  public double getStatError()   { return mSumErrors; }
  public double getStatError2()  { return mSumErrorSquared; }

  public void initErrorStats()
  {
    mSumCount = 0;
    mSumErrors = 0;
    mSumErrorSquared = 0;
  }

  // must be overridden
  public void addStatErrors( TDVector[] g1, TDVector[] m1, float[] errors )
  {
    TDLog.Error("calib algo add error stats not overridden");
  }

  // must be overridden
  public int Calibrate() { return -1; }

        
}
  
