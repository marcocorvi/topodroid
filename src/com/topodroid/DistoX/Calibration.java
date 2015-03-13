/* @file Calibration.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration algorithm
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 * --------------------------------------------------------
 * CHANGES
 * 20120725 TopoDroidApp log
 * 20140408 added enforce max to 2.0
 */
package com.topodroid.DistoX;

import java.lang.Math;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Locale;

// used by logCoeff
// import android.util.Log;

public class Calibration
{
  private TopoDroidApp mApp;

  private Matrix aG = null;
  private Matrix aM = null;
  private Vector bG = null;
  private Vector bM = null;

  private Vector[] g = null;
  private Vector[] m = null;
  private long[] group = null;
  private float[] err = null;
  float mMaxError = 0.0f;

  private int idx;
  private int num;

  private boolean mNonLinear;
  private Vector nL;

  private Vector gxp; // opt vectors
  private Vector mxp;
  private Vector gxt; // turn vectors
  private Vector mxt;
  float b0=0.0f, c0=0.0f; // bearing and clino

  private float mDelta;

  // ==============================================================

  void EnforceMax2( Vector b, Matrix a )
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
    if ( max >= 2.0 ) {
      float m1 = (float)(1.9999/max);
      TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "EnforceMax2 scale by " + m1 );
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

  public float getDelta() { return mDelta; }

  public Calibration( int N, TopoDroidApp app, boolean nonLinear )
  {
    num = 0;
    if ( N > 0 ) Reset( N );
    mApp = app;
    mNonLinear = nonLinear;
  }

  void setAlgorith( boolean nonLinear ) { mNonLinear = nonLinear; }

  public float Delta() { return mDelta; }
  public float Error( int k ) { return err[k]; }
  public float[] Errors() { return err; }

  public Matrix GetAG() { return aG; }
  public Matrix GetAM() { return aM; }
  public Vector GetBG() { return bG; }
  public Vector GetBM() { return bM; }
  public Vector GetNL() { return nL; }

  // public int nrCoeff() { return mNonLinear ? 52 : 48; }

  public byte[] GetCoeff()
  {
    if ( aG == null ) return null;
    byte[] coeff = new byte[ 52 ]; // FIXME nrCoeff()
    long v;
    v  = (long)(bG.x * TopoDroidUtil.FV); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[ 0] = (byte)(v & 0xff);
    coeff[ 1] = (byte)((v>>8) & 0xff);
    v = (long)(aG.x.x * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[ 2] = (byte)(v & 0xff);
    coeff[ 3] = (byte)((v>>8) & 0xff);
    v = (long)(aG.x.y * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[ 4] = (byte)(v & 0xff);
    coeff[ 5] = (byte)((v>>8) & 0xff);
    v = (long)(aG.x.z * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[ 6] = (byte)(v & 0xff);
    coeff[ 7] = (byte)((v>>8) & 0xff);

    v = (long)(bG.y * TopoDroidUtil.FV); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[ 8] = (byte)(v & 0xff);
    coeff[ 9] = (byte)((v>>8) & 0xff);
    v = (long)(aG.y.x * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[10] = (byte)(v & 0xff);
    coeff[11] = (byte)((v>>8) & 0xff);
    v = (long)(aG.y.y * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[12] = (byte)(v & 0xff);
    coeff[13] = (byte)((v>>8) & 0xff);
    v = (long)(aG.y.z * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[14] = (byte)(v & 0xff);
    coeff[15] = (byte)((v>>8) & 0xff);

    v = (long)(bG.z * TopoDroidUtil.FV); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[16] = (byte)(v & 0xff);
    coeff[17] = (byte)((v>>8) & 0xff);
    v = (long)(aG.z.x * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[18] = (byte)(v & 0xff);
    coeff[19] = (byte)((v>>8) & 0xff);
    v = (long)(aG.z.y * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[20] = (byte)(v & 0xff);
    coeff[21] = (byte)((v>>8) & 0xff);
    v = (long)(aG.z.z * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[22] = (byte)(v & 0xff);
    coeff[23] = (byte)((v>>8) & 0xff);
    
    v = (long)(bM.x * TopoDroidUtil.FV); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[24] = (byte)(v & 0xff);
    coeff[25] = (byte)((v>>8) & 0xff);
    v = (long)(aM.x.x * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[26] = (byte)(v & 0xff);
    coeff[27] = (byte)((v>>8) & 0xff);
    v = (long)(aM.x.y * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[28] = (byte)(v & 0xff);
    coeff[29] = (byte)((v>>8) & 0xff);
    v = (long)(aM.x.z * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[30] = (byte)(v & 0xff);
    coeff[31] = (byte)((v>>8) & 0xff);

    v = (long)(bM.y * TopoDroidUtil.FV); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[32] = (byte)(v & 0xff);
    coeff[33] = (byte)((v>>8) & 0xff);
    v = (long)(aM.y.x * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[34] = (byte)(v & 0xff);
    coeff[35] = (byte)((v>>8) & 0xff);
    v = (long)(aM.y.y * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[36] = (byte)(v & 0xff);
    coeff[37] = (byte)((v>>8) & 0xff);
    v = (long)(aM.y.z * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[38] = (byte)(v & 0xff);
    coeff[39] = (byte)((v>>8) & 0xff);

    v = (long)(bM.z * TopoDroidUtil.FV); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[40] = (byte)(v & 0xff);
    coeff[41] = (byte)((v>>8) & 0xff);
    v = (long)(aM.z.x * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[42] = (byte)(v & 0xff);
    coeff[43] = (byte)((v>>8) & 0xff);
    v = (long)(aM.z.y * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[44] = (byte)(v & 0xff);
    coeff[45] = (byte)((v>>8) & 0xff);
    v = (long)(aM.z.z * TopoDroidUtil.FM); if ( v > TopoDroidUtil.ZERO ) v = TopoDroidUtil.NEG - v;
    coeff[46] = (byte)(v & 0xff);
    coeff[47] = (byte)((v>>8) & 0xff);

    if ( mNonLinear ) {
      v = (long)(nL.x * TopoDroidUtil.FN); if ( v <= 0 ) v = 0xff + v;
      coeff[48] = (byte)( (v-1) & 0xff );
      v = (long)(nL.y * TopoDroidUtil.FN); if ( v <= 0 ) v = 0xff + v;
      coeff[49] = (byte)( (v-1) & 0xff );
      v = (long)(nL.z * TopoDroidUtil.FN); if ( v <= 0 ) v = 0xff + v;
      coeff[50] = (byte)( (v-1) & 0xff );
      coeff[51] = (byte)( 0xff );
    } else {
      coeff[48] = (byte)( 0xff );
      coeff[49] = (byte)( 0xff );
      coeff[50] = (byte)( 0xff );
      coeff[51] = (byte)( 0xff );
    }
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
    return cs.toString();
  }

  // static void logCoeff( byte[] coeff )  // DEBUG METHOD
  // {
  //   Log.v( "DistoX", coeff[ 0] + " " + coeff[ 1] + " " + coeff[ 2] + " " + coeff[ 3] + " " + coeff[ 4] + " " + coeff[ 5] );
  //   Log.v( "DistoX", coeff[ 6] + " " + coeff[ 7] + " " + coeff[ 8] + " " + coeff[ 9] + " " + coeff[10] + " " + coeff[11] );
  //   Log.v( "DistoX", coeff[12] + " " + coeff[13] + " " + coeff[14] + " " + coeff[15] + " " + coeff[16] + " " + coeff[17] );
  //   Log.v( "DistoX", coeff[18] + " " + coeff[19] + " " + coeff[20] + " " + coeff[15] + " " + coeff[16] + " " + coeff[17] );
  //   Log.v( "DistoX", coeff[24] + " " + coeff[25] + " " + coeff[26] + " " + coeff[27] + " " + coeff[28] + " " + coeff[29] );
  //   Log.v( "DistoX", coeff[30] + " " + coeff[31] + " " + coeff[32] + " " + coeff[33] + " " + coeff[34] + " " + coeff[35] );
  //   Log.v( "DistoX", coeff[36] + " " + coeff[37] + " " + coeff[38] + " " + coeff[39] + " " + coeff[40] + " " + coeff[41] );
  //   Log.v( "DistoX", coeff[42] + " " + coeff[43] + " " + coeff[44] + " " + coeff[45] + " " + coeff[46] + " " + coeff[47] );
  //   Log.v( "DistoX", coeff[48] + " " + coeff[49] + " " + coeff[50] + " " + coeff[51] );
  // }

  static byte[] stringToCoeff( String cs )
  {
    byte[] coeff = new byte[ 52 ]; // N.B. return 52 calib coeff
    if ( cs == null ) {
      for ( int k=0; k<52; ++k ) coeff[k] = (byte)(0);
    } else {
      int kk = cs.length();
      coeff[48] = (byte)( 0xff ); // default values if the string is only 48 bytes
      coeff[49] = (byte)( 0xff );
      coeff[50] = (byte)( 0xff );
      coeff[51] = (byte)( 0xff );
      for ( int k=0; k<kk; ++k ) coeff[k] = (byte)( cs.charAt(k) );
    }
    return coeff;
  }

  private static void coeffToBA( byte[] coeff, Vector b, Matrix a, int off )
  {
    long v;
    long c0 = (int)(coeff[off+ 0]); if ( c0 < 0 ) c0 = 256+c0;
    long c1 = (int)(coeff[off+ 1]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    b.x = v / TopoDroidUtil.FV;
    c0 = (int)(coeff[off+ 2]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 3]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    a.x.x = v / TopoDroidUtil.FM;
    c0 = (int)(coeff[off+ 4]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 5]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    a.x.y = v / TopoDroidUtil.FM;
    c0 = (int)(coeff[off+ 6]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 7]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    a.x.z = v / TopoDroidUtil.FM;

    // BY
    c0 = (int)(coeff[off+ 8]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+ 9]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    b.y = v / TopoDroidUtil.FV;

    c0 = (int)(coeff[off+10]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+11]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    a.y.x = v / TopoDroidUtil.FM;

    c0 = (int)(coeff[off+12]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+13]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    a.y.y = v / TopoDroidUtil.FM;

    c0 = (int)(coeff[off+14]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+15]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    a.y.z = v / TopoDroidUtil.FM;

    // BZ
    c0 = (int)(coeff[off+16]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+17]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    b.z = v / TopoDroidUtil.FV;
    c0 = (int)(coeff[off+18]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+19]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    a.z.x = v / TopoDroidUtil.FM;
    c0 = (int)(coeff[off+20]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+21]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    a.z.y = v / TopoDroidUtil.FM;
    c0 = (int)(coeff[off+22]); if ( c0 < 0 ) c0 = 256+c0;
    c1 = (int)(coeff[off+23]); if ( c1 < 0 ) c1 = 256+c1;
    v = c0 + (c1<<8 );
    if ( v > TopoDroidUtil.ZERO ) v = v - TopoDroidUtil.NEG;
    a.z.z = v / TopoDroidUtil.FM;
  }

  public static void coeffToG( byte[] coeff, Vector b, Matrix a )
  {
    coeffToBA( coeff, b, a, 0 );
  }

  public static void coeffToM( byte[] coeff, Vector b, Matrix a )
  {
    coeffToBA( coeff, b, a, 24 );
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
  public static void coeffToNL( byte[] coeff, Vector nl )
  {
    if ( coeff != null && coeff.length >= 51 ) {
      int c0 = (int)(coeff[48]) + 1; if ( c0 >= 128 ) c0 = c0 - 256;
      nl.x = c0;
      c0 = (int)(coeff[49]) + 1; if ( c0 >= 128 ) c0 = c0 - 256;
      nl.y = c0;
      c0 = (int)(coeff[50]) + 1; if ( c0 >= 128 ) c0 = c0 - 256;
      nl.z = c0;
    } else {
      nl.x = 0;
      nl.y = 0;
      nl.z = 0;
    }
  }

  public void AddValues( CalibCBlock b )
  {
    // add also group-0 CBlocks to keep CBlock list and calib vectors aligned
    AddValues( b.gx, b.gy, b.gz, b.mx, b.my, b.mz, b.mGroup );
  }

  public void AddValues( long gx, long gy, long gz, long mx, long my, long mz, long group0 )
  {
    if ( idx >= num ) {
      return;
    }
    g[idx] = new Vector( gx/TopoDroidUtil.FV, gy/TopoDroidUtil.FV, gz/TopoDroidUtil.FV );
    m[idx] = new Vector( mx/TopoDroidUtil.FV, my/TopoDroidUtil.FV, mz/TopoDroidUtil.FV );
    group[idx] = group0;

    if ( TopoDroidLog.LOG_CALIB ) {
      StringWriter sw = new StringWriter();
      PrintWriter  pw = new PrintWriter( sw );
      pw.format("Add %d G %d %d %d M %d %d %d Grp %d", idx, gx, gy, gz, mx, my, mz, group0 );
      TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, sw.getBuffer().toString() );
    }
    idx ++;
  }

  public int Size() { return idx; }

  public int Calibrate()
  {
    mDelta = 0.0f;
    TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "Calibrate: data nr. " + idx 
                      + " algo " + (mNonLinear? "non-" : "") + "linear" );
    if ( idx < 16 ) return -1;
    return Optimize( idx, g, m );
  }

  public void Reset( int N )
  {
    if ( N != num ) {
      num = N;
      g = new Vector[N];
      m = new Vector[N];
      group = new long[N];
      err   = new float[N];
    }
    idx = 0;
    aG = null;
    bG = null;
    aM = null;
    bM = null;
    TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "Reset calibration " + N + " data");
  }
    
  // ------------------------------------------------------------
  // private methods

  private void InitializeAB( Vector avG, Vector avM )
  {
    aG = new Matrix( Matrix.one );
    aM = new Matrix( Matrix.one );
// FIXME NL
    if ( mNonLinear ) {
      bG = new Vector( -avG.x, -avG.y, -avG.z );
      bM = new Vector( -avM.x, -avM.y, -avM.z );
    } else {
      bG = new Vector();
      bM = new Vector();
    }
    nL = new Vector();   // inittialize to zero vector
  }

  private void OptVectors( Vector gr, Vector mr, float s, float c )
  {
    Vector no = gr.cross( mr );
    no.Normalized();
    gxp = ( (mr.mult(c)).plus( (mr.cross(no)).mult(s) ) ).plus(gr);
    gxp.Normalized();
    mxp =   (gxp.mult(c)).plus( (no.cross(gxp)).mult(s) );
  }

  private void TurnVectors( Vector gf, Vector mf, Vector gr, Vector mr,
                            boolean print )
  {
    float s1 = gr.z * gf.y - gr.y * gf.z + mr.z * mf.y - mr.y * mf.z;
    float c1 = gr.y * gf.y + gr.z * gf.z + mr.y * mf.y + mr.z * mf.z;
    float d1 = (float)Math.sqrt( c1*c1 + s1*s1 );
    s1 /= d1;
    c1 /= d1;
    gxt = gf.TurnX( s1, c1 );
    mxt = mf.TurnX( s1, c1 );
    // if ( print ) {
    //   TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "TurnVectors", s1, c1 );
    //   TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "TurnVectors", -1, gf, gxt );
    //   TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "TurnVectors", -1, mf, mxt );
    // }
  }

/* ============================================================ */

  private void LogNumber( String msg, int it )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, msg + " " + it );
  }

  private void LogMatrixVector( String msg, Matrix m1, Vector v1 ) 
  {
    if ( ! TopoDroidLog.LOG_CALIB ) return;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, " M: %8.4f %8.4f %8.4f V: %8.4f\n    %8.4f %8.4f %8.4f   %8.4f\n    %8.4f %8.4f %8.4f   %8.4f",
       m1.x.x, m1.x.y, m1.x.z, v1.x, 
       m1.y.x, m1.y.y, m1.y.z, v1.y, 
       m1.z.x, m1.z.y, m1.z.z, v1.z );
    TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, msg + sw.getBuffer().toString() );
  }

  private void LogVectors( String msg, long group, Vector v1, Vector v2 )
  {
    if ( ! TopoDroidLog.LOG_CALIB ) return;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, " %3d V1 %8.4f %8.4f %8.4f\n    V2 %8.4f %8.4f %8.4f", group, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z ); 
    TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, msg + sw.getBuffer().toString() );
  }

  private void LogSC( String msg, float s, float c )
  {
    if ( ! TopoDroidLog.LOG_CALIB ) return;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, " S %8.4f C %8.4f", s, c ); 
    TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, msg + sw.getBuffer().toString() );
  }

/* ============================================================ */

  private void checkOverflow( Vector v, Matrix m )
  {
    float mv = v.maxAbsValue() * TopoDroidUtil.FV;
    float mm = m.maxAbsValue() * TopoDroidUtil.FV;
    if ( mv > mm ) mm = mv;
    if ( mm > 256*128 ) {
      mv = 256*128 / mm;
      m.scaleBy( mv );
      v.scaleBy( mv );
    }
  }

  private void saturate( Vector nl )
  {
    float max = 127.0f / TopoDroidUtil.FN;
    if ( nl.x >  max ) nl.x =  max;
    if ( nl.x < -max ) nl.x = -max;
    if ( nl.y >  max ) nl.y =  max;
    if ( nl.y < -max ) nl.y = -max;
    if ( nl.z >  max ) nl.z =  max;
    if ( nl.z < -max ) nl.z = -max;
  }

  private void computeBearingAndClinoRad( Vector g0, Vector m0 )
  {
    Vector g = g0.mult( 1.0f / TopoDroidUtil.FV );
    Vector m = m0.mult( 1.0f / TopoDroidUtil.FV );
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
    b0 = (float)Math.atan2( -ey, ex );
    c0   = - (float)Math.atan2( ez, (float)Math.sqrt(ex*ex+ey*ey) );
    // r0    = (float)Math.atan2( g.y, g.z );
    if ( b0 < 0.0f ) b0 += 2*TopoDroidUtil.M_PI;
    // if ( r0 < 0.0f ) r0 += 2*TopoDroidUtil.M_PI;
  }

// ----------------------------------------------------------------

  private int Optimize( int nn, Vector[] g, Vector [] m )
  {
    int max_it = TopoDroidSetting.mCalibMaxIt;
    float eps  = TopoDroidSetting.mCalibEps;

    // int num = g.Length();
    Vector[] gr = new Vector[nn];
    Vector[] mr = new Vector[nn];
    Vector[] gx = new Vector[nn];
    Vector[] mx = new Vector[nn];
    Vector[] gl = null;
    Matrix[] gs = null;
    if ( mNonLinear ) {
      gl = new Vector[nn]; // linearized g values
      gs = new Matrix[nn]; // Diag(g^2 - 1/2)
    }

    Matrix aG0;
    Matrix aM0;

    Vector sumG = new Vector();
    Vector sumM = new Vector();
    Matrix sumG2 = new Matrix();
    Matrix sumM2 = new Matrix();

    float sa = 0.0f;
    float ca = 0.0f;
    float invNum = 0.0f;
    for (int i=0; i<nn; ++i ) {
      if ( group[i] > 0 ) {
        invNum += 1.0f;
        sa += ( g[i].cross( m[i] )).Length(); // cross product
        ca += g[i].dot( m[i] );               // dor product
        sumG.add( g[i] );
        sumM.add( m[i] );
        sumG2.add( new Matrix(g[i],g[i]) );   // outer product
        sumM2.add( new Matrix(m[i],m[i]) );
        if ( mNonLinear ) {
          gl[i] = new Vector( g[i] );
          gs[i] = new Matrix();               // zero matrix
          gs[i].x.x = g[i].x * g[i].x - 0.5f; // diagonal elements
          gs[i].y.y = g[i].y * g[i].y - 0.5f;
          gs[i].z.z = g[i].z * g[i].z - 0.5f;
        }
      }
    }
    if ( invNum < 0.5f ) return 0;

    invNum = 1.0f / invNum;

    Vector avG = sumG.mult( invNum );  // average G
    Vector avM = sumM.mult( invNum );  // average M
    Matrix invG = (sumG2.minus( new Matrix(sumG, avG) ) ).InverseT();  // inverse of the transposed
    Matrix invM = (sumM2.minus( new Matrix(sumM, avM) ) ).InverseT();

    // TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "Number", nn );
    // TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "invG", invG, avG );
    // TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "invM", invM, avM ); // this is OK
    LogMatrixVector( "initial inverse|average G", invG, avG );
    LogMatrixVector( "initial inverse|average M", invM, avM );

    InitializeAB( avG, avM );
    // LogAB( 0, aG, bG, aM, bM ); // this is OK

    int it = 0;
    float da = (float)Math.sqrt( ca*ca + sa*sa );
    float s = sa / da;
    float c = ca / da;
    LogSC( "sin/cos", s, c ); // this is OK
// FIXME NL
    // float alpha = (float)Math.atan2( sa, ca );


    do {
      for ( int i=0; i<nn; ++i ) {
        if ( group[i] > 0 ) {
          if ( mNonLinear ) {
            gr[i] = bG.plus( aG.timesV(gl[i]) );
          } else {
            gr[i] = bG.plus( aG.timesV(g[i]) );
          }
          mr[i] = bM.plus( aM.timesV(m[i]) );
          // if ( i < 4 ) {
          //  LogVectors( "GR", i, gr[i], mr[i] ); // this is OK at the first iteration
          // }
        }
      }
      sa = 0.0f;
      ca = 0.0f;
      long group0 = -1;
      for ( int i=0; i<nn; ) {
        if ( group[i] <= 0 ) {
          ++i;
        } else if ( group[i] != group0 ) {
          group0 = group[i];
          Vector grp = new Vector();
          Vector mrp = new Vector();
          int first = i;
          while ( i < nn && (group[i] == 0 || group[i] == group0) ) {
            // group must be positive integer
            // group == 0 means to skip
            //
            if ( group[i] > 0 ) {
              TurnVectors( gr[i], mr[i], gr[first], mr[first], i<4 ); // output ==> gxt, mxt
              // if ( i < 4 ) {
              //   LogVectors( "GR ", i, gr[i], mr[i] );
              //   LogVectors( "GXT", i, gxt,   mxt );
              // }
              grp.add( gxt );
              mrp.add( mxt );
            }
            ++ i;
          }
          OptVectors( grp, mrp, s, c ); // output ==> gxp, mxp
          // if ( group0 < 2 ) {
          //   LogVectors( "grp", group0, grp, mrp );
          //   LogVectors( "gxp", group0, gxp, mxp );
          // }

          sa += (mrp.cross(gxp)).Length();
          ca += mrp.dot(gxp);
          for (int j = first; j < i; ++j ) {
            if ( group[j] != 0 ) {
              TurnVectors( gxp, mxp, gr[j], mr[j], j<4 ); // output ==> gxt, mxt
              gx[j] = new Vector( gxt );
              mx[j] = new Vector( mxt );
              if ( group0 == 1 ) {
                LogVectors( "gx", group0, gx[j], mx[j] );
              }
            }
          }
        }
      }
      da = (float)Math.sqrt( ca*ca + sa*sa );
      s = sa / da;
      c = ca / da;
      LogSC( "sin/cos", s, c );
      Vector avGx = new Vector();
      Vector avMx = new Vector();
      Matrix sumGxG = new Matrix();
      Matrix sumMxM = new Matrix();
      for (int i=0; i<nn; ++i ) {
        if ( group[i] > 0 ) {
          avGx.add( gx[i] );
          avMx.add( mx[i] );
          if ( mNonLinear ) {
            sumGxG.add( new Matrix( gx[i], gl[i] ) );
          } else {
            sumGxG.add( new Matrix( gx[i], g[i] ) );
          }
          sumMxM.add( new Matrix( mx[i], m[i] ) );
        } 
      }
      aG0 = new Matrix( aG );
      aM0 = new Matrix( aM );
      avGx.scaleBy( invNum );
      avMx.scaleBy( invNum );
      LogMatrixVector( "average G", sumGxG, avGx );
      LogMatrixVector( "average M", sumMxM, avMx );

      aG = (sumGxG.minus( new Matrix(avGx, sumG) )).timesT( invG ); // multiplication by the transposed
      aM = (sumMxM.minus( new Matrix(avMx, sumM) )).timesT( invM );
      aG.z.y = (aG.y.z + aG.z.y) / 2.0f;
      aG.y.z = aG.z.y;
      bG = avGx.minus( aG.timesV(avG) );
      bM = avMx.minus( aM.timesV(avM) );
      LogMatrixVector( "G", aG, bG );
      LogMatrixVector( "M", aM, bM );
      float gmax = aG.MaxDiff(aG0);
      float mmax = aM.MaxDiff(aM0);
      if ( mNonLinear ) {
        // get new non-linearity coefficients
        Matrix psum = new Matrix();
        Vector qsum = new Vector();
        for (int ii = 0; ii < nn; ii++) {
          if ( group[ii] > 0 ) {
            Matrix p = aG.timesM( gs[ii] );
            Vector q = ( gx[ii].minus( aG.timesV( g[ii] ) ) ).minus( bG );
            Matrix pt = p.Transposed();

            // psum = (P^t * P) N.B. psum^t = psum
            psum.add( pt.timesT( pt ) ); // psum.add( pt.timesM( p ) ); 
            qsum.add( pt.timesV( q ) );
          }
        }
        nL = ( psum.InverseT()).timesV( qsum );
        saturate( nL );
        // recalculate linearized g values
        sumG  = new Vector();
        sumG2 = new Matrix();
        for (int ii = 0; ii < nn; ii++) {
          if ( group[ii] > 0 ) {
            gl[ii] = g[ii].plus( gs[ii].timesV( nL ) );
            // sum up g and g^2
            sumG.add( gl[ii] );
            sumG2.add( new Matrix(gl[ii], gl[ii]) ); // outer product
          }
        }
        avG  = sumG.mult( invNum ); // average g
        invG = (sumG2.minus( new Matrix(sumG, avG)) ).InverseT(); // inverse of the transposed
      }
      ++ it;
    } while ( it < max_it && ( aG.MaxDiff(aG0) > eps || aM.MaxDiff(aM0) > eps ) );

    // LogMatrixVector( "final G", aG, bG );
    // LogMatrixVector( "final M", aM, bM );
    checkOverflow( bG, aG );
    checkOverflow( bM, aM );

    mDelta = 0.0f;
    for ( int i=0; i<nn; ++i ) {
      if ( group[i] > 0 ) {
        if ( mNonLinear ) { 
          gr[i] = bG.plus( aG.timesV(gl[i]) );
        } else {
          gr[i] = bG.plus( aG.timesV(g[i]) );
        }
        mr[i] = bM.plus( aM.timesV(m[i]) );
      }
    }
    long group0 = -1;
    long cnt = 0;
    for ( int i=0; i<nn; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        Vector grp = new Vector();
        Vector mrp = new Vector();
        int first = i;
        while ( i < nn && (group[i] == 0 || group[i] == group0) ) {
          if ( group[i] != 0 ) {
            TurnVectors( gr[i], mr[i], gr[first], mr[first], i<4 );
            grp.add( gxt );
            mrp.add( mxt );
          }
          ++ i;
        }
        OptVectors( grp, mrp, s, c );
        computeBearingAndClinoRad( gxp, mxp );
        Vector v0 = new Vector( (float)Math.cos(c0) * (float)Math.cos(b0),
                                (float)Math.cos(c0) * (float)Math.sin(b0),
                                (float)Math.sin(c0) );
        for (int j=first; j<i; ++j ) {
          if ( group[j] == 0 ) {
            err[j] = 0.0f;
          } else {
            computeBearingAndClinoRad( gr[j], mr[j] );
            Vector v = new Vector( (float)Math.cos(c0) * (float)Math.cos(b0),
                                   (float)Math.cos(c0) * (float)Math.sin(b0),
                                   (float)Math.sin(c0) );
            err[j] = v0.minus(v).Length(); // approx angle with sin/tan
            mDelta += err[j];
            ++ cnt;
          }
        }
      }
    }
    mDelta = mDelta * TopoDroidUtil.RAD2GRAD / cnt;

    EnforceMax2( bG, aG );
    EnforceMax2( bM, aM );

    // for (int i=0; i<nn; ++i ) {
    //   if ( group[i] > 0 ) {
    //     Vector dg = gx[i].minus( gr[i] );
    //     Vector dm = mx[i].minus( mr[i] );
    //     err[i] = dg.dot(dg) + dm.dot(dm);
    //     mDelta += err[i];
    //     err[i] = (float)Math.sqrt( err[i] );
    //   } else {
    //     err[i] = 0.0f;
    //   }
    // }
    // mDelta = 100 * (float)Math.sqrt( mDelta*invNum );
    return it;
  }
        
}
  
