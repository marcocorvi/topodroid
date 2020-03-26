/* @file CalibAlgo.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration algorithm
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;
// import com.topodroid.prefs.TDSetting;

import java.lang.Math;

import java.util.Locale;

// used by logCoeff
// import android.util.Log;

class CalibAlgo
{
  protected TDMatrix aG = null;
  protected TDMatrix aM = null;
  protected TDVector bG = null;
  protected TDVector bM = null;

  protected TDVector[] g = null;
  protected TDVector[] m = null;
  protected long[] group = null;
  protected float[] err = null;  // errors of the data [radians] 

  protected int idx;
  protected int num;

  protected boolean mNonLinear;
  protected TDVector nL;

  protected float b0=0.0f, c0=0.0f; // bearing and clino

  protected float mDelta    = 0.0f; // average data error [degrees]
  protected float mDelta2   = 0.0f; // std-dev data error [degrees]
  protected float mMaxError = 0.0f; // max error [degrees]
  protected float mDeltaBH  = 0.0f; // original delta BH algo

  // ==============================================================

  static protected TDVector scaledVector( TDVector v ) { return scaledVector( v.x, v.y, v.z ); }

  static private TDVector scaledVector( float x, float y, float z )
  {
    return new TDVector( x/TDUtil.FV, y/TDUtil.FV, z/TDUtil.FV );
  }

  static final private double MAX_M_VALUE = 1.99993896;

  // this should never happen with BH algo
  //
  boolean hasSaturatedCoeff()
  {
    if ( Math.abs( aM.x.x ) >= MAX_M_VALUE ) return true;
    if ( Math.abs( aM.y.y ) >= MAX_M_VALUE ) return true;
    if ( Math.abs( aM.z.z ) >= MAX_M_VALUE ) return true;
    return false;
  }

  // void checkCoeffOverflow()
  // {
  //   double mx = Math.abs( aM.x.x );
  //   double my = Math.abs( aM.y.y );
  //   double mz = Math.abs( aM.z.z );
  //   if ( my > mx ) mx = my;
  //   if ( mz > mx ) mx = mz;
  //   if ( mx >= MAX_M_VALUE && mx < 2.01 ) {
  //     float f = (float)( MAX_M_VALUE / (mx + 0.00000001) );
  //     aM.x.x *= f;  aM.x.y *= f;  aM.x.z *= f;
  //     aM.y.x *= f;  aM.y.y *= f;  aM.y.z *= f;
  //     aM.z.x *= f;  aM.z.y *= f;  aM.z.z *= f;
  //     bM.x *= f;    bM.y *= f;    bM.z *= f;
  //   }
  // }

  void EnforceMax2( TDVector b, TDMatrix a )
  {
    double max = Math.abs( b.x );
    double m;
    m = Math.abs( b.y ); if ( m > max ) max = m;
    m = Math.abs( b.z ); if ( m > max ) max = m;
    m = Math.abs( a.x.x ); if ( m > max ) max = m;
    m = Math.abs( a.x.y ); if ( m > max ) max = m;
    m = Math.abs( a.x.z ); if ( m > max ) max = m;
    m = Math.abs( a.y.x ); if ( m > max ) max = m;
    m = Math.abs( a.y.y ); if ( m > max ) max = m;
    m = Math.abs( a.y.z ); if ( m > max ) max = m;
    m = Math.abs( a.z.x ); if ( m > max ) max = m;
    m = Math.abs( a.z.y ); if ( m > max ) max = m;
    m = Math.abs( a.z.z ); if ( m > max ) max = m;
    if ( max >= MAX_M_VALUE ) {
      float m1 = (float)(MAX_M_VALUE / (max + 0.00000001) );
      TDLog.Log( TDLog.LOG_CALIB, "EnforceMax2 scale by " + m1 );
      b.x *= m1;
      b.y *= m1;
      b.z *= m1;
      a.x.x *= m1;
      a.x.y *= m1;
      a.x.z *= m1;
      a.y.x *= m1;
      a.y.y *= m1;
      a.y.z *= m1;
      a.z.x *= m1;
      a.z.y *= m1;
      a.z.z *= m1;
    }
  }

  /** construct a CalibAlgo from the saved coefficients
   */
  CalibAlgo( byte[] coeff, boolean nonLinear )
  {
    mNonLinear = nonLinear;
    bG = new TDVector();
    bM = new TDVector();
    aG = new TDMatrix();
    aM = new TDMatrix();
    nL = new TDVector();
    coeffToG( coeff, bG, aG );
    coeffToM( coeff, bM, aM );
    coeffToNL( coeff, nL );
  }

  // void dump()
  // {
  //   // Log.v("DistoX", String.format("G %8.4f %8.4f %8.4f", bG.x, bG.y, bG.z ) );
  //   // Log.v("DistoX", "aG " + aG.x.x + " " + aG.x.y + " " + aG.x.z );
  //   // Log.v("DistoX", "   " + aG.y.x + " " + aG.y.y + " " + aG.y.z );
  //   // Log.v("DistoX", "   " + aG.z.x + " " + aG.z.y + " " + aG.z.z );
  //
  //   // Log.v("DistoX", String.format("M %8.4f %8.4f %8.4f", bM.x, bM.y, bM.z ) );
  //   // 
  //   // Log.v("DistoX", "aM " + aM.x.x + " " + aM.x.y + " " + aM.x.z );
  //   // Log.v("DistoX", "   " + aM.y.x + " " + aM.y.y + " " + aM.y.z );
  //   // Log.v("DistoX", "   " + aM.z.x + " " + aM.z.y + " " + aM.z.z );
  //
  //   // Log.v("DistoX", String.format("NL %8.4f %8.4f %8.4f", nL.x, nL.y, nL.z ) );
  // }

  // @param N         number of data
  // @param nonLinear whether to use non-linear algo
  CalibAlgo( int N, boolean nonLinear )
  {
    num = 0;
    if ( N > 0 ) Reset( N );
    mNonLinear = nonLinear;
  }

  // void setAlgorith( boolean nonLinear ) { mNonLinear = nonLinear; }

  float DeltaBH()      { return mDeltaBH; }
  float Delta()        { return mDelta; }
  float Delta2()       { return mDelta2; }
  public float Error( int k ) { return err[k]; }
  float[] Errors()     { return err; }
  float MaxError( )    { return mMaxError; }

  TDMatrix GetAG() { return aG; }
  TDMatrix GetAM() { return aM; }
  TDVector GetBG() { return bG; }
  TDVector GetBM() { return bM; }
  TDVector GetNL() { return nL; }

  // public int nrCoeff() { return mNonLinear ? 52 : 48; }

  static private long roundV( float x )
  {
    long v = Math.round(x * TDUtil.FV);
    if ( v > TDUtil.ZERO ) v = TDUtil.NEG - v;
    return v;
  }

  static private long roundM( float x )
  {
    long v = Math.round(x * TDUtil.FM);
    if ( v > TDUtil.ZERO ) v = TDUtil.NEG - v;
    return v;
  }

  byte[] GetCoeff()
  {
    if ( aG == null ) return null;
    byte[] coeff = new byte[ 52 ]; // FIXME nrCoeff()
    long v;
    v  = roundV( bG.x );
    coeff[ 0] = (byte)(v & 0xff);
    coeff[ 1] = (byte)((v>>8) & 0xff);
    v = roundM( aG.x.x );
    coeff[ 2] = (byte)(v & 0xff);
    coeff[ 3] = (byte)((v>>8) & 0xff);
    v = roundM( aG.x.y );
    coeff[ 4] = (byte)(v & 0xff);
    coeff[ 5] = (byte)((v>>8) & 0xff);
    v = roundM( aG.x.z );
    coeff[ 6] = (byte)(v & 0xff);
    coeff[ 7] = (byte)((v>>8) & 0xff);

    v = roundV( bG.y );
    coeff[ 8] = (byte)(v & 0xff);
    coeff[ 9] = (byte)((v>>8) & 0xff);
    v = roundM( aG.y.x );
    coeff[10] = (byte)(v & 0xff);
    coeff[11] = (byte)((v>>8) & 0xff);
    v = roundM( aG.y.y );
    coeff[12] = (byte)(v & 0xff);
    coeff[13] = (byte)((v>>8) & 0xff);
    v = roundM( aG.y.z );
    coeff[14] = (byte)(v & 0xff);
    coeff[15] = (byte)((v>>8) & 0xff);

    v = roundV( bG.z );
    coeff[16] = (byte)(v & 0xff);
    coeff[17] = (byte)((v>>8) & 0xff);
    v = roundM( aG.z.x );
    coeff[18] = (byte)(v & 0xff);
    coeff[19] = (byte)((v>>8) & 0xff);
    v = roundM( aG.z.y );
    coeff[20] = (byte)(v & 0xff);
    coeff[21] = (byte)((v>>8) & 0xff);
    v = roundM( aG.z.z );
    coeff[22] = (byte)(v & 0xff);
    coeff[23] = (byte)((v>>8) & 0xff);
    
    v = roundV(bM.x );
    coeff[24] = (byte)(v & 0xff);
    coeff[25] = (byte)((v>>8) & 0xff);
    v = roundM( aM.x.x );
    coeff[26] = (byte)(v & 0xff);
    coeff[27] = (byte)((v>>8) & 0xff);
    v = roundM( aM.x.y );
    coeff[28] = (byte)(v & 0xff);
    coeff[29] = (byte)((v>>8) & 0xff);
    v = roundM( aM.x.z );
    coeff[30] = (byte)(v & 0xff);
    coeff[31] = (byte)((v>>8) & 0xff);

    v = roundV( bM.y );
    coeff[32] = (byte)(v & 0xff);
    coeff[33] = (byte)((v>>8) & 0xff);
    v = roundM( aM.y.x );
    coeff[34] = (byte)(v & 0xff);
    coeff[35] = (byte)((v>>8) & 0xff);
    v = roundM( aM.y.y );
    coeff[36] = (byte)(v & 0xff);
    coeff[37] = (byte)((v>>8) & 0xff);
    v = roundM( aM.y.z );
    coeff[38] = (byte)(v & 0xff);
    coeff[39] = (byte)((v>>8) & 0xff);

    v = roundV( bM.z );
    coeff[40] = (byte)(v & 0xff);
    coeff[41] = (byte)((v>>8) & 0xff);
    v = roundM( aM.z.x );
    coeff[42] = (byte)(v & 0xff);
    coeff[43] = (byte)((v>>8) & 0xff);
    v = roundM( aM.z.y );
    coeff[44] = (byte)(v & 0xff);
    coeff[45] = (byte)((v>>8) & 0xff);
    v = roundM( aM.z.z );
    coeff[46] = (byte)(v & 0xff);
    coeff[47] = (byte)((v>>8) & 0xff);

    if ( mNonLinear ) {
      coeff[48] = floatToByteNL( nL.x );
      coeff[49] = floatToByteNL( nL.y );
      coeff[50] = floatToByteNL( nL.z );
      // Log.v("DistoX", "NL to coeff " + coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + nL.x + " " + nL.y + " " + nL.z );
    } else {
      coeff[48] = (byte)( 0xff );
      coeff[49] = (byte)( 0xff );
      coeff[50] = (byte)( 0xff );
    }
    coeff[51] = (byte)( 0xff );
    return coeff;
  }

  // N.B. the string will have the length of the coeff array
  static String coeffToString( byte[] coeff )
  {
    int kk = (coeff == null)? 0 : coeff.length;
    StringBuilder cs = new StringBuilder( kk );
    for ( int k=0; k<kk; ++k ) {
      cs.insert(k, (char)(coeff[k]) );
    }
    // Log.v( "DistoX", "coeff to string " + coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + coeff[51] );
    return cs.toString();
  }

  // static void logCoeff( byte[] coeff )  // DEBUG METHOD
  // {
  //   // Log.v( "DistoX", coeff[ 0] + " " + coeff[ 1] + " " + coeff[ 2] + " " + coeff[ 3] + " " + coeff[ 4] + " " + coeff[ 5] );
  //   // Log.v( "DistoX", coeff[ 6] + " " + coeff[ 7] + " " + coeff[ 8] + " " + coeff[ 9] + " " + coeff[10] + " " + coeff[11] );
  //   // Log.v( "DistoX", coeff[12] + " " + coeff[13] + " " + coeff[14] + " " + coeff[15] + " " + coeff[16] + " " + coeff[17] );
  //   // Log.v( "DistoX", coeff[18] + " " + coeff[19] + " " + coeff[20] + " " + coeff[15] + " " + coeff[16] + " " + coeff[17] );
  //   // Log.v( "DistoX", coeff[24] + " " + coeff[25] + " " + coeff[26] + " " + coeff[27] + " " + coeff[28] + " " + coeff[29] );
  //   // Log.v( "DistoX", coeff[30] + " " + coeff[31] + " " + coeff[32] + " " + coeff[33] + " " + coeff[34] + " " + coeff[35] );
  //   // Log.v( "DistoX", coeff[36] + " " + coeff[37] + " " + coeff[38] + " " + coeff[39] + " " + coeff[40] + " " + coeff[41] );
  //   // Log.v( "DistoX", coeff[42] + " " + coeff[43] + " " + coeff[44] + " " + coeff[45] + " " + coeff[46] + " " + coeff[47] );
  //   // Log.v( "DistoX", coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + coeff[51] );
  // }

  static byte[] stringToCoeff( String cs )
  {
    byte[] coeff = new byte[ 52 ]; // N.B. return 52 calib coeff
    coeff[48] = coeff[49] = coeff[50] = coeff[51] = (byte)(0xff); // default values
    if ( cs == null ) {
      for ( int k=0; k<48; ++k ) coeff[k] = (byte)(0);
    } else {
      int kk = cs.length();
      for ( int k=0; k<kk; ++k ) coeff[k] = (byte)( cs.charAt(k) );
      // Log.v( "DistoX", "string to coeff " + coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + coeff[51] );
    }
    return coeff;
  }

  private static void coeffToBA( byte[] coeff, TDVector b, TDMatrix a, int off )
  {
    long v;
    long c0 = (int)(coeff[off/*+ 0*/]); if ( c0 < 0 ) c0 = 256+c0;
    long c1 = (int)(coeff[off+ 1]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    b.x = v / TDUtil.FV;

    c0 = (int)(coeff[off+ 2]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 3]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    a.x.x = v / TDUtil.FM;

    c0 = (int)(coeff[off+ 4]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 5]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    a.x.y = v / TDUtil.FM;

    c0 = (int)(coeff[off+ 6]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 7]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    a.x.z = v / TDUtil.FM;

    // BY
    c0 = (int)(coeff[off+ 8]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 9]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    b.y = v / TDUtil.FV;

    c0 = (int)(coeff[off+10]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+11]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    a.y.x = v / TDUtil.FM;

    c0 = (int)(coeff[off+12]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+13]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    a.y.y = v / TDUtil.FM;

    c0 = (int)(coeff[off+14]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+15]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    a.y.z = v / TDUtil.FM;

    // BZ
    c0 = (int)(coeff[off+16]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+17]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    b.z = v / TDUtil.FV;

    c0 = (int)(coeff[off+18]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+19]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    a.z.x = v / TDUtil.FM;

    c0 = (int)(coeff[off+20]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+21]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    a.z.y = v / TDUtil.FM;

    c0 = (int)(coeff[off+22]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+23]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TDUtil.ZERO ) v = v - TDUtil.NEG;
    a.z.z = v / TDUtil.FM;
  }

  static void coeffToG( byte[] coeff, TDVector b, TDMatrix a )
  {
    coeffToBA( coeff, b, a, 0 );
    // Log.v("DistoX", "G " + b.x + " " + b.y + " " + b.z + " " + a.x.x + " " + a.x.y + " " + a.x.z );
  }

  static void coeffToM( byte[] coeff, TDVector b, TDMatrix a )
  {
    coeffToBA( coeff, b, a, 24 );
    // Log.v("DistoX", "M " + b.x + " " + b.y + " " + b.z + " " + a.x.x + " " + a.x.y + " " + a.x.z );
  }

  // N.B. map coeff <--> NL
  //          0          1
  //          1          2
  //          ...
  //          126        127
  //          127        n.a.
  //          128       -127
  //          ...
  //          254       -1
  //  0xff    255        0     
  //
  static private float byteToFloatNL( byte b )
  {
    int c0 = 1 + (int)(b);
    if ( c0 >= 128 ) c0 = c0 - 256;
    return c0 / TDUtil.FN;
  }

  static byte floatToByteNL( float x )
  {
    float xf = x * TDUtil.FN; 
    int v = Math.round( xf ) - 1;
    if ( v <= 0 ) v = 0x100 + v;
    return (byte)( v & 0xff );
  }

  // FIXME
  // protected static byte floatToByteV( float x )
  // {
  //   long v = (long)(x * TDUtil.FV); if ( v > TDUtil.ZERO ) v = TDUtil.NEG - v;
  //   return (byte)(v & 0xff);
  // }
  // 
  // protected static byte floatToByteM( float x )
  // {
  //   long v = (long)(x * TDUtil.FM); if ( v > TDUtil.ZERO ) v = TDUtil.NEG - v;
  //   return (byte)(v & 0xff);
  // }
  
  static void coeffToNL( byte[] coeff, TDVector nl )
  {
    if ( coeff != null && coeff.length >= 51 ) {
      nl.x = byteToFloatNL( coeff[48] );
      nl.y = byteToFloatNL( coeff[49] );
      nl.z = byteToFloatNL( coeff[50] );
      // Log.v("DistoX", "coeff to NL " + coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + nl.x + " " + nl.y + " " + nl.z );
    } else {
      nl.x = 0;
      nl.y = 0;
      nl.z = 0;
    }
  }

  void AddValues( CalibCBlock b )
  {
    // add also group-0 CBlocks to keep CBlock list and calib vectors aligned
    AddValues( b.gx, b.gy, b.gz, b.mx, b.my, b.mz, b.mGroup );
  }

  private void AddValues( long gx, long gy, long gz, long mx, long my, long mz, long group0 )
  {
    if ( idx >= num ) {
      return;
    }
    // g[idx] = new TDVector( gx/TDUtil.FV, gy/TDUtil.FV, gz/TDUtil.FV );
    // m[idx] = new TDVector( mx/TDUtil.FV, my/TDUtil.FV, mz/TDUtil.FV );
    g[idx] = scaledVector( gx, gy, gz );
    m[idx] = scaledVector( mx, my, mz );
    group[idx] = (group0 < 0)? 0 : group0;

    if ( TDLog.LOG_CALIB ) {
      TDLog.DoLog(
        String.format(Locale.US, "Add %d G %d %d %d M %d %d %d Grp %d", idx, gx, gy, gz, mx, my, mz, group0 ) );
    }
    idx ++;
  }

  public int Size() { return idx; }

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
    TDLog.Log( TDLog.LOG_CALIB, "Reset calibration " + N + " data");
  }
    
/* ============================================================ */

  protected void LogNumber( String msg, int it )
  {
    TDLog.Log( TDLog.LOG_CALIB, msg + " " + it );
  }

  protected void LogMatrixVector( String msg, TDMatrix m1, TDVector v1 ) 
  {
    if ( ! TDLog.LOG_CALIB ) return;
    TDLog.DoLog(
      msg + String.format(Locale.US,
       " M: %8.4f %8.4f %8.4f V: %8.4f\n    %8.4f %8.4f %8.4f   %8.4f\n    %8.4f %8.4f %8.4f   %8.4f",
       m1.x.x, m1.x.y, m1.x.z, v1.x, 
       m1.y.x, m1.y.y, m1.y.z, v1.y, 
       m1.z.x, m1.z.y, m1.z.z, v1.z ) );
  }

  protected void LogVectors( String msg, long group, TDVector v1, TDVector v2 )
  {
    if ( ! TDLog.LOG_CALIB ) return;
    TDLog.DoLog(
      msg + String.format(Locale.US,
      " %3d V1 %8.4f %8.4f %8.4f\n    V2 %8.4f %8.4f %8.4f", group, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z ) ); 
  }

  protected void LogSC( String msg, float s, float c )
  {
    if ( ! TDLog.LOG_CALIB ) return;
    TDLog.DoLog( msg + String.format(Locale.US, " S %8.4f C %8.4f", s, c ) );
  }

/* ============================================================ */

  protected void checkOverflow( TDVector v, TDMatrix m )
  {
    float mv = v.maxAbsValue() * TDUtil.FV;
    float mm = m.maxAbsValue() * TDUtil.FM;
    if ( mv > mm ) mm = mv;
    if ( mm > 32768 ) { // 32768 = 256*128 = 1<<15 = 0x010000
      mv = 32768 / mm;
      m.timesEqual( mv );
      v.timesEqual( mv );
    }
  }

  private float saturate( float x )
  {
    int ix = (int)(x * TDUtil.FN);
    if ( ix > 127 ) { ix = 127; } else if ( ix < -127 ) { ix = -127; }
    return ix / TDUtil.FN;
  }

  protected void saturate( TDVector nl )
  {
    nl.x = saturate( nl.x );
    nl.y = saturate( nl.y );
    nl.z = saturate( nl.z );
  }

  protected void computeBearingAndClinoRad( TDVector g0, TDVector m0 )
  {
    // TDVector g = g0.mult( 1.0f / TDUtil.FV );
    // TDVector m = m0.mult( 1.0f / TDUtil.FV );
    TDVector g = scaledVector( g0 );
    TDVector m = scaledVector( m0 );
    g.normalize();
    m.normalize();
    TDVector e = new TDVector( 1.0f, 0.0f, 0.0f );
    TDVector y = m.cross( g );
    TDVector x = g.cross( y );
    y.normalize();
    x.normalize();
    float ex = e.dot( x );
    float ey = e.dot( y );
    float ez = e.dot( g );
    b0 =   TDMath.atan2( -ey, ex );
    c0 = - TDMath.atan2( ez, (float)Math.sqrt(ex*ex+ey*ey) );
    // r0    = TDMath.atan2( g.y, g.z );
    if ( b0 < 0.0f ) b0 += TDMath.M_2PI;
    // if ( r0 < 0.0f ) r0 += TDMath.M_2PI;
  }

  // -----------------------------------------------------------------------
  // ERROR STATS

  /** compute the unit vector direction of sensor-data (g,m)
   */
  TDVector computeDirection( TDVector g1, TDVector m1 )
  {
    TDVector g = scaledVector( g1 );
    TDVector m = scaledVector( m1 );
    TDVector gr;
    TDVector mr = m;
    if ( mNonLinear ) {
      TDMatrix gs = new TDMatrix();
      gs.x.x = g.x * g.x - 0.5f;
      gs.y.y = g.y * g.y - 0.5f;
      gs.z.z = g.z * g.z - 0.5f;
      gr = bG.plus( aG.timesV( g.plus( gs.timesV( nL ) ) ) );
    } else {
      gr = bG.plus( aG.timesV( g ) );
    }
    computeBearingAndClinoRad( gr, mr );
    return new TDVector( b0, c0 );
    // return new TDVector( (float)Math.cos(c0) * (float)Math.cos(b0),
    //                    (float)Math.cos(c0) * (float)Math.sin(b0),
    //                    (float)Math.sin(c0) );
  }

  // error acumulators
  protected int    mSumCount;
  protected double mSumErrors;
  protected double mSumErrorSquared;

  int    getStatCount()   { return mSumCount; }
  double getStatError()   { return mSumErrors; }
  double getStatError2()  { return mSumErrorSquared; }

  void initErrorStats()
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
  
