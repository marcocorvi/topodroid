/* @file CalibTransform.java
 *
 * @author marco corvi
 * @date oct 2023
 *
 * @brief TopoDroid DistoX calibration transform
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

public class CalibTransform
{
  protected TDMatrix aG = null;
  protected TDMatrix aM = null;
  protected TDVector bG = null;
  protected TDVector bM = null;
  protected TDVector nL = null;
  protected boolean  mNonLinear = false;

  protected float b0=0.0f, c0=0.0f; // bearing and clino


  /** construct a CalibTransform from the saved coefficients
   * @param coeff   coefficients array
   * @param nonLinear whether the transform is non-linear
   */
  public CalibTransform( byte[] coeff, boolean nonLinear )
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

  /** cstr
   * @param nonLinear whether to use non-linear algo
   */
  public CalibTransform( boolean nonLinear )
  {
    mNonLinear = nonLinear;
    bG = new TDVector();
    bM = new TDVector();
    aG = new TDMatrix();
    aM = new TDMatrix();
    nL = new TDVector();
  }

  /** copy cstr
   * @param calib   calib transform
   */
  public CalibTransform( CalibTransform calib ) 
  {
    mNonLinear = calib.mNonLinear;
    bG = new TDVector( calib.bG );
    bM = new TDVector( calib.bM );
    nL = new TDVector( calib.nL );
    aG = new TDMatrix( calib.aG );
    aM = new TDMatrix( calib.aM );
  }

  // void dump( )
  // {
  //   TDLog.v( String.format(Locale.US, "G %8.4f %8.4f %8.4f", bG.x, bG.y, bG.z ) );
  //   TDLog.v( "aG " + aG.x.x + " " + aG.x.y + " " + aG.x.z );
  //   TDLog.v( "   " + aG.y.x + " " + aG.y.y + " " + aG.y.z );
  //   TDLog.v( "   " + aG.z.x + " " + aG.z.y + " " + aG.z.z );
  //
  //   TDLog.v( String.format(Locale.US, "M %8.4f %8.4f %8.4f", bM.x, bM.y, bM.z ) );
  //   TDLog.v( "aM " + aM.x.x + " " + aM.x.y + " " + aM.x.z );
  //   TDLog.v( "   " + aM.y.x + " " + aM.y.y + " " + aM.y.z );
  //   TDLog.v( "   " + aM.z.x + " " + aM.z.y + " " + aM.z.z );
  //
  //   TDLog.v( String.format(Locale.US, "NL %8.4f %8.4f %8.4f", nL.x, nL.y, nL.z ) );
  // }

  // ==============================================================

  /** @return the vector scaled by the constant FV (24000.0)
   * @param v unscaled vector (raw DistoX values)
   */
  static protected TDVector scaledVector( TDVector v ) { return scaledVector( v.x, v.y, v.z ); }

  /** @return the vector scaled by the constant FV (24000.0)
   * @param x unscaled X component (raw DistoX value)
   * @param y unscaled Y component (raw DistoX value)
   * @param z unscaled Z component (raw DistoX value)
   */
  static public TDVector scaledVector( float x, float y, float z )
  {
    return new TDVector( x/TDUtil.FV, y/TDUtil.FV, z/TDUtil.FV );
  }

  /** maximum value of a tranform matrix entry
   */
  static final private double MAX_M_VALUE = 1.99993896;

  /** @return true if the AM matrix has an entry over the max M value (1.99993896) 
   * @note this should never happen with BH algo
   */
  public boolean hasSaturatedCoeff()
  {
    return ( ( Math.abs( aM.x.x ) >= MAX_M_VALUE ) 
          || ( Math.abs( aM.y.y ) >= MAX_M_VALUE )
          || ( Math.abs( aM.z.z ) >= MAX_M_VALUE ) );
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

  /** rescale the transformation coefficients so that none exceeds the max value
   * @param b     B vector
   * @param a     A matrix
   */
  public void EnforceMax2( TDVector b, TDMatrix a )
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
      // TDLog.v( "EnforceMax2 scale by " + m1 );
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

  // void setAlgorithm( boolean nonLinear ) { mNonLinear = nonLinear; }

  /** @return the A-matrix for the tranform of G
   */
  public TDMatrix GetAG() { return aG; }

  /** @return the A-matrix for the tranform of M
   */
  public TDMatrix GetAM() { return aM; }

  /** @return the B-vector for the tranform of G
   */
  public TDVector GetBG() { return bG; }

  /** @return the B-vector for the tranform of M
   */
  public TDVector GetBM() { return bM; }

  /** @return the non-linear vector for the tranform of G
   */
  public TDVector GetNL() { return nL; }

  // public int nrCoeff() { return mNonLinear ? 52 : 48; }

  /** @return the rounded value of a float as unsigned short (16 bits)
   * @param x     float value 
   * @note x in [0, 1.365333] is positive
   *       x in [1.365333, 2.730666] is negative (use complement)
   */
  private static long roundV( float x )
  {
    long v = Math.round(x * TDUtil.FV);
    if ( v > TDUtil.ZERO ) v = TDUtil.NEG - v;
    return v;
  }

  /** @return the rounded value of a float as unsigned short (16 bits)
   * @param x     float value
   * @note x in [0, 2] is positive
   *       x in [2, 4] is negative (use complement)
   */
  private static long roundM( float x )
  {
    long v = Math.round(x * TDUtil.FM);
    if ( v > TDUtil.ZERO ) v = TDUtil.NEG - v;
    return v;
  }

  /** @return the (byte) array of coefficients as for DistoX memory
   */
  public byte[] GetCoeff()
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
      // TDLog.v( "NL to coeff " + coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + nL.x + " " + nL.y + " " + nL.z );
    } else {
      coeff[48] = (byte)( 0xff );
      coeff[49] = (byte)( 0xff );
      coeff[50] = (byte)( 0xff );
    }
    coeff[51] = (byte)( 0xff );
    return coeff;
  }

  /** @return the string presenttaion of the coefficients
   * @param coeff  byte array of coefficients
   * @note the string will have the length of the coeff array
   */
  public static String coeffToString( byte[] coeff )
  {
    int kk = (coeff == null)? 0 : coeff.length;
    StringBuilder cs = new StringBuilder( kk );
    for ( int k=0; k<kk; ++k ) {
      cs.insert(k, (char)(coeff[k]) );
    }
    // TDLog.v( "coeff to string " + coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + coeff[51] );
    return cs.toString();
  }

  // static void logCoeff( byte[] coeff )  // DEBUG METHOD
  // {
  //   // TDLog.v( coeff[ 0] + " " + coeff[ 1] + " " + coeff[ 2] + " " + coeff[ 3] + " " + coeff[ 4] + " " + coeff[ 5] );
  //   // TDLog.v( coeff[ 6] + " " + coeff[ 7] + " " + coeff[ 8] + " " + coeff[ 9] + " " + coeff[10] + " " + coeff[11] );
  //   // TDLog.v( coeff[12] + " " + coeff[13] + " " + coeff[14] + " " + coeff[15] + " " + coeff[16] + " " + coeff[17] );
  //   // TDLog.v( coeff[18] + " " + coeff[19] + " " + coeff[20] + " " + coeff[15] + " " + coeff[16] + " " + coeff[17] );
  //   // TDLog.v( coeff[24] + " " + coeff[25] + " " + coeff[26] + " " + coeff[27] + " " + coeff[28] + " " + coeff[29] );
  //   // TDLog.v( coeff[30] + " " + coeff[31] + " " + coeff[32] + " " + coeff[33] + " " + coeff[34] + " " + coeff[35] );
  //   // TDLog.v( coeff[36] + " " + coeff[37] + " " + coeff[38] + " " + coeff[39] + " " + coeff[40] + " " + coeff[41] );
  //   // TDLog.v( coeff[42] + " " + coeff[43] + " " + coeff[44] + " " + coeff[45] + " " + coeff[46] + " " + coeff[47] );
  //   // TDLog.v( coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + coeff[51] );
  // }

  /** @return the byte array of coefficients for the string presentation
   * @param cs   string presentation of the coefficients
   */
  public static byte[] stringToCoeff( String cs )
  {
    byte[] coeff = new byte[ 52 ]; // N.B. return 52 calib coeff
    coeff[48] = coeff[49] = coeff[50] = coeff[51] = (byte)(0xff); // default values
    if ( cs == null ) {
      for ( int k=0; k<48; ++k ) coeff[k] = (byte)(0);
    } else {
      int kk = cs.length();
      for ( int k=0; k<kk; ++k ) coeff[k] = (byte)( cs.charAt(k) );
      // TDLog.v( "string to coeff " + coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + coeff[51] );
    }
    return coeff;
  }

  /** initialize the A-matrix and B-vector from the byte array of coefficients
   * @param coeff    coefficients
   * @param b        B vector
   * @param a        A matrix
   * @param off      offset in the array of coefficients
   */
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

  /** initialize the A-matrix and B-vector for G from the byte array of coefficients
   * @param coeff    coefficients
   * @param b        B vector
   * @param a        A matrix
   */
  public static void coeffToG( byte[] coeff, TDVector b, TDMatrix a )
  {
    coeffToBA( coeff, b, a, 0 );
    // TDLog.v( "G " + b.x + " " + b.y + " " + b.z + " " + a.x.x + " " + a.x.y + " " + a.x.z );
  }

  /** initialize the A-matrix and B-vector for M from the byte array of coefficients
   * @param coeff    coefficients
   * @param b        B vector
   * @param a        A matrix
   */
  public static void coeffToM( byte[] coeff, TDVector b, TDMatrix a )
  {
    coeffToBA( coeff, b, a, 24 );
    // TDLog.v( "M " + b.x + " " + b.y + " " + b.z + " " + a.x.x + " " + a.x.y + " " + a.x.z );
  }

  /** @return the float value of a non-linear entry from its byte presentation
   * @param b     non-linear byte
   * N.B. map coeff <--> NL
   *          0          1
   *          1          2
   *          ...
   *          126        127
   *          127        n.a.
   *          128       -127
   *          ...
   *          254       -1
   *  0xff    255        0     
   */
  private static float byteToFloatNL( byte b )
  {
    int c0 = 1 + (int)(b);
    if ( c0 >= 128 ) c0 = c0 - 256;
    return c0 / TDUtil.FN;
  }

  /** @return the byte value of a non-linear entry from its float value
   * @param x     non-linear entry value
   */
  public static byte floatToByteNL( float x )
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
  
  /** initialize the non-linear vector form the byte array of coefficients
   * @param coeff   coefficients
   * @param nl      non-linear vector
   */
  public static void coeffToNL( byte[] coeff, TDVector nl )
  {
    if ( coeff != null && coeff.length >= 51 ) {
      nl.x = byteToFloatNL( coeff[48] );
      nl.y = byteToFloatNL( coeff[49] );
      nl.z = byteToFloatNL( coeff[50] );
      // TDLog.v( "coeff to NL " + coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + nl.x + " " + nl.y + " " + nl.z );
    } else {
      nl.x = 0;
      nl.y = 0;
      nl.z = 0;
    }
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

  /** compute azimuth and clino from (g,m) in device frame
   * @param g0   G value (device-frame)
   * @param m0   M value
   */
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
   * @param g1    input G (sensor values)
   * @param m1    input M 
   */
  public TDVector computeDirection( TDVector g1, TDVector m1 )
  {
    TDVector g = scaledVector( g1 );
    TDVector m = scaledVector( m1 );
    TDVector gr;
    // TDVector mr = m; 
    TDVector mr = bM.plus( aM.timesV( m ) ); // FIXED 20230908
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

  /** compute the transformed M of a vector
   * @param m1    scaled input vector
   */
  public TDVector getTransformedM( TDVector m1 )
  {
    return bM.plus( aM.timesV( m1 ) );
  }

  /** compute the transformed G of a vector
   * @param g1    scaled input vector
   */
  public TDVector getTransformedG( TDVector g1 )
  {
    if ( mNonLinear ) {
      TDMatrix gs = new TDMatrix();
      gs.x.x = g1.x * g1.x - 0.5f;
      gs.y.y = g1.y * g1.y - 0.5f;
      gs.z.z = g1.z * g1.z - 0.5f;
      return bG.plus( aG.timesV( g1.plus( gs.timesV( nL ) ) ) );
    } else {
      return bG.plus( aG.timesV( g1 ) );
    }
  }

}
  
