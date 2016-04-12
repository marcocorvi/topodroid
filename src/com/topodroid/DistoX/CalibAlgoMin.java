/* @file CalibAlgoMin.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration algorithm Error minimization
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.Math;

import java.util.Locale;

// used by logCoeff
import android.util.Log;

public class CalibAlgoMin extends CalibAlgo
{
  // private boolean mNonLinear;
  // private Vector nL;

  private Vector gxp; // opt vectors
  private Vector mxp;
  private Vector gxt; // turn vectors
  private Vector mxt;
  // float b0=0.0f, c0=0.0f; // bearing and clino

  // ==============================================================
  /** construct a Calibration from the saved coefficients
   */
  CalibAlgoMin( byte[] coeff, boolean nonLinear )
  {
    super( coeff, nonLinear );
    // mNonLinear = nonLinear;
    // coeffToNL( coeff, nL );
  }


  public CalibAlgoMin( int idx, boolean nonLinear )
  {
    super( idx, nonLinear );
    // mNonLinear = nonLinear;
  }

  // void setAlgorith( boolean nonLinear ) { mNonLinear = nonLinear; }
  // public Vector GetNL() { return nL; }
  // public int nrCoeff() { return mNonLinear ? 52 : 48; }


  float clino( Vector gs, Vector ms )
  {
    Vector g = bG.plus( aG.timesV( gs ) );
    g.Normalized();
    return (float)Math.acos( g.x ) * TDMath.RAD2GRAD;
  }

  float azimuth( Vector gs, Vector ms )
  {
    Vector g = bG.plus( aG.timesV( gs ) );
    g.Normalized();
    float gx = g.x;

    Vector m = bM.plus( aM.timesV( ms ) );
    m.Normalized();
    float gm = g.dot( m );
 
    return (float)Math.acos( (gx*gm - m.x)/Math.sqrt((1-gx*gx)*(1-gm*gm)) ) * TDMath.RAD2GRAD;

    // Vector e = g ^ m;
    // Vector n = e ^ g;
    // n.Normalized();
    // Vector x(1,0,0);
    // Vector xh = x - g * g.x;
    // xh.Normalized();
    // return acos( xh * n ) * 180 / M_PI;
  }

  Vector G( int n ) { return bG.plus( aG.timesV( g[n] ) ); }
  Vector M( int n ) { return bM.plus( aM.timesV( m[n] ) ); }

  float mean_dip()
  {
    float sum = 0;
    for (int i=0; i<idx; ++i ) sum += G(i).dot( M(i) );
    return sum / idx;
  }

  Vector mean_g()
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.add( g[i] );
    sum.scaleBy( 1.0f/idx );
    return sum;
  }
  
  Vector mean_m()
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.add( m[i] );
    sum.scaleBy( 1.0f/idx );
    return sum;
  }
  
  Matrix mean_gg()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) sum.add( new Matrix( g[i], g[i] ) );
    sum.scaleBy( 1.0f/idx );
    return sum;
  }
  
  Matrix mean_mm()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) sum.add( new Matrix( m[i], m[i] ) );
    sum.scaleBy( 1.0f/idx );
    return sum;
  }
  
  float dgx2()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float x1 = G(i+0).x;
      float x2 = G(i+1).x;
      float x3 = G(i+2).x;
      float x4 = G(i+3).x;
      float x = ( x1 + x2 + x3 + x4 )/4;
      sum += (x-x1)*(x-x1) + (x-x2)*(x-x2) + (x-x3)*(x-x3) + (x-x4)*(x-x4);
    }
    return sum / idx;
  }
    
  float dmx2()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float x1 = M(i+0).x;
      float x2 = M(i+1).x;
      float x3 = M(i+2).x;
      float x4 = M(i+3).x;
      float x = ( x1 + x2 + x3 + x4 )/4;
      sum += (x-x1)*(x-x1) + (x-x2)*(x-x2) + (x-x3)*(x-x3) + (x-x4)*(x-x4);
    }
    return sum / idx;
  }
  
  float dgxx()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) { 
      float x1 = g[i+0].x;
      float x2 = g[i+1].x;
      float x3 = g[i+2].x;
      float x4 = g[i+3].x;
      float x = ( x1 + x2 + x3 + x4 )/4;
      sum += (x-x1)*(x-x1) + (x-x2)*(x-x2) + (x-x3)*(x-x3) + (x-x4)*(x-x4);
    }
    return sum / idx;
  }
  
  float dgyy()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float y1 = g[i+0].y;
      float y2 = g[i+1].y;
      float y3 = g[i+2].y;
      float y4 = g[i+3].y;
      float y = ( y1 + y2 + y3 + y4 )/4;
      sum += (y-y1)*(y-y1) + (y-y2)*(y-y2) + (y-y3)*(y-y3) + (y-y4)*(y-y4);
    }
    return sum / idx;
  }
  
  float dgzz()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float z1 = g[i+0].z;
      float z2 = g[i+1].z;
      float z3 = g[i+2].z;
      float z4 = g[i+3].z;
      float z = ( z1 + z2 + z3 + z4 )/4;
      sum += (z-z1)*(z-z1) + (z-z2)*(z-z2) + (z-z3)*(z-z3) + (z-z4)*(z-z4);
    }
    return sum / idx;
  }
  
  float dgxy()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float x1 = g[i+0].x;
      float x2 = g[i+1].x;
      float x3 = g[i+2].x;
      float x4 = g[i+3].x;
      float x = ( x1 + x2 + x3 + x4 )/4;
      float y1 = g[i+0].y;
      float y2 = g[i+1].y;
      float y3 = g[i+2].y;
      float y4 = g[i+3].y;
      float y = ( y1 + y2 + y3 + y4 )/4;
      sum += (x-x1)*(y-y1) + (x-x2)*(y-y2) + (x-x3)*(y-y3) + (x-x4)*(y-y4);
    }
    return sum / idx;
  }
  
  float dgxz()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float x1 = g[i+0].x;
      float x2 = g[i+1].x;
      float x3 = g[i+2].x;
      float x4 = g[i+3].x;
      float x = ( x1 + x2 + x3 + x4 )/4;
      float z1 = g[i+0].z;
      float z2 = g[i+1].z;
      float z3 = g[i+2].z;
      float z4 = g[i+3].z;
      float z = ( z1 + z2 + z3 + z4 )/4;
      sum += (x-x1)*(z-z1) + (x-x2)*(z-z2) + (x-x3)*(z-z3) + (x-x4)*(z-z4);
    }
    return sum / idx;
  }
  
  float dgyz()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float y1 = g[i+0].y;
      float y2 = g[i+1].y;
      float y3 = g[i+2].y;
      float y4 = g[i+3].y;
      float y = ( y1 + y2 + y3 + y4 )/4;
      float z1 = g[i+0].z;
      float z2 = g[i+1].z;
      float z3 = g[i+2].z;
      float z4 = g[i+3].z;
      float z = ( z1 + z2 + z3 + z4 )/4;
      sum += (y-y1)*(z-z1) + (y-y2)*(z-z2) + (y-y3)*(z-z3) + (y-y4)*(z-z4);
    }
    return sum / idx;
  }
  
  
  float dmxx()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float x1 = m[i+0].x;
      float x2 = m[i+1].x;
      float x3 = m[i+2].x;
      float x4 = m[i+3].x;
      float x = ( x1 + x2 + x3 + x4 )/4;
      sum += (x-x1)*(x-x1) + (x-x2)*(x-x2) + (x-x3)*(x-x3) + (x-x4)*(x-x4);
    }
    return sum / idx;
  }
  
  float dmyy()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float y1 = m[i+0].y;
      float y2 = m[i+1].y;
      float y3 = m[i+2].y;
      float y4 = m[i+3].y;
      float y = ( y1 + y2 + y3 + y4 )/4;
      sum += (y-y1)*(y-y1) + (y-y2)*(y-y2) + (y-y3)*(y-y3) + (y-y4)*(y-y4);
    }
    return sum / idx;
  }
  
  float dmzz()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float z1 = m[i+0].z;
      float z2 = m[i+1].z;
      float z3 = m[i+2].z;
      float z4 = m[i+3].z;
      float z = ( z1 + z2 + z3 + z4 )/4;
      sum += (z-z1)*(z-z1) + (z-z2)*(z-z2) + (z-z3)*(z-z3) + (z-z4)*(z-z4);
    }
    return sum / idx;
  }
  
  float dmxy()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float x1 = m[i+0].x;
      float x2 = m[i+1].x;
      float x3 = m[i+2].x;
      float x4 = m[i+3].x;
      float x = ( x1 + x2 + x3 + x4 )/4;
      float y1 = m[i+0].y;
      float y2 = m[i+1].y;
      float y3 = m[i+2].y;
      float y4 = m[i+3].y;
      float y = ( y1 + y2 + y3 + y4 )/4;
      sum += (x-x1)*(y-y1) + (x-x2)*(y-y2) + (x-x3)*(y-y3) + (x-x4)*(y-y4);
    }
    return sum / idx;
  }
  
  float dmxz()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float x1 = m[i+0].x;
      float x2 = m[i+1].x;
      float x3 = m[i+2].x;
      float x4 = m[i+3].x;
      float x = ( x1 + x2 + x3 + x4 )/4;
      float z1 = m[i+0].z;
      float z2 = m[i+1].z;
      float z3 = m[i+2].z;
      float z4 = m[i+3].z;
      float z = ( z1 + z2 + z3 + z4 )/4;
      sum += (x-x1)*(z-z1) + (x-x2)*(z-z2) + (x-x3)*(z-z3) + (x-x4)*(z-z4);
    }
    return sum / idx;
  }
  
  float dmyz()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float y1 = m[i+0].y;
      float y2 = m[i+1].y;
      float y3 = m[i+2].y;
      float y4 = m[i+3].y;
      float y = ( y1 + y2 + y3 + y4 )/4;
      float z1 = m[i+0].z;
      float z2 = m[i+1].z;
      float z3 = m[i+2].z;
      float z4 = m[i+3].z;
      float z = ( z1 + z2 + z3 + z4 )/4;
      sum += (y-y1)*(z-z1) + (y-y2)*(z-z2) + (y-y3)*(z-z3) + (y-y4)*(z-z4);
    }
    return sum / idx;
  }
  
  
  
  // ========================================================
  
  float r2g()
  {
    float sum = 0;
    for (int i=0; i<idx; ++i ) sum += G(i).LengthSquared();
    return sum / idx;
  }
  
  float r2m() // < (Bm+Am*ms) * (Bm+Am*ms) >
  {
    float sum = 0;
    for (int i=0; i<idx; ++i ) sum += M(i).LengthSquared();
    return sum / idx;
  }
  
  Matrix r2gg()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) {
      Matrix mu = new Matrix(g[i], g[i]);
      mu.scaleBy( G(i).LengthSquared() );
      sum.add( mu );
    }
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
  
  Matrix r2mm()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) {
      Matrix mu = new Matrix(m[i], m[i]);
      mu.scaleBy( M(i).LengthSquared() );
      sum.add( mu );
    }
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
  
  Vector g0() // <Bg + Ag * gs>
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.add( G(i) );
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
    
  Vector m0() // <Bm + Am * ms>
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.add( M(i) );
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
  
  Matrix gg()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) sum.add( new Matrix( G(i), g[i] ) );
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
  
  Matrix mm()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) sum.add( new Matrix( M(i), m[i] ) );
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
  
  Vector r2ag()
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.add( g[i].mult( G(i).LengthSquared() ) );
    sum.scaleBy( 1.0f / idx );
    return aG.timesV( sum );
  }
    
  Vector r2am()
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.add( m[i].mult( M(i).LengthSquared() ) );
    sum.scaleBy( 1.0f / idx );
    return aM.timesV( sum );
  }
  
  Vector gmm( float d ) // (G*M - d) M
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) {
      Vector v = M(i);
      sum.add( v.mult( G(i).dot(v) - d) );
    }
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
  
  Vector gmg( float d ) // (G*M - d) G
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) {
      Vector v = G(i);
      sum.add( v.mult( M(i).dot(v) - d ) );
    }
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
   
  Matrix r2bg()
  {
    Matrix sum = new Matrix(); 
    for (int i=0; i<idx; ++i ) {
      Matrix mu = new Matrix( bG, g[i]);
      mu.scaleBy( G(i).LengthSquared() );
      sum.add( mu );
    }
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
      
  Matrix r2bm()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) {
      Matrix mu = new Matrix( bM, m[i]);
      mu.scaleBy( M(i).LengthSquared() );
      sum.add( mu );
    }
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
  
  Matrix gmmg( float d )
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) {
      Vector v = M(i);
      Matrix mu = new Matrix(v, g[i]);
      mu.scaleBy( G(i).dot(v) - d );
      sum.add( mu );
    }
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
  
  Matrix gmgm( float d )
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) {
      Vector v = G(i);
      Matrix mu = new Matrix(v, m[i]);
      mu.scaleBy( M(i).dot(v) - d );
      sum.add( mu );
    }
    sum.scaleBy( 1.0f / idx );
    return sum;
  }
  
  // ---------------------------------------------------
  
  float error( int i, float dip, float beta, float gamma )
  {
    float sum = 0;
    float r2 = G(i).LengthSquared() - 1;
    sum += r2 * r2;
    r2 = M(i).LengthSquared() - 1;
    sum += r2 * r2;
    r2 = G(i).dot( M(i) ) - dip;
    sum += beta * r2 * r2;
    return sum / idx + gamma * ( dgx2() + dmx2() );
  }
  
  float error( float dip, float beta, float gamma )
  {
    float sum = 0;
    for ( int i=0; i<idx; ++i ) {
      float r2 = G(i).LengthSquared() - 1;
      sum += r2 * r2;
      r2 = M(i).LengthSquared() - 1;
      sum += r2 * r2;
      r2 = G(i).dot( M(i) ) - dip;
      sum += beta * r2 * r2;
    }
    return sum / idx + gamma * ( dgx2() + dmx2() );
  }

  public int Calibrate()
  {
    mDelta = 0.0f;
    TDLog.Log( TDLog.LOG_CALIB, "Calibrate: data nr. " + idx 
                      + " algo " + (mNonLinear? "non-" : "") + "linear" );
    if ( idx < 16 ) return -1;

    float alpha0 = TDSetting.mAlgoMinAlpha;
    float alpha1 = 1 - alpha0;
    float beta  = TDSetting.mAlgoMinBeta;
    float gamma = TDSetting.mAlgoMinGamma;

    float dip, e0, e1;
    int iter = 0;

    bG = mean_g(); bG.scaleBy(-1);
    bM = mean_m(); bM.scaleBy(-1);
    aG = mean_gg().InverseM();
    aM = mean_mm().InverseM();
    
    dip = mean_dip();
    e0 = error( dip, beta, gamma );
    // printf("E %.6f dip %.4f\n", e0, dip );

    do {
      Vector bg0 = bG.mult( alpha1 );
      Matrix ag0 = aG.mult( alpha1 );
      Vector bm0 = bM.mult( alpha1 );
      Matrix am0 = aM.mult( alpha1 );
      e1 = e0;
      {
        float r = 1 / r2g();
        Vector bg1 = ( g0().minus( r2ag().plus( gmm( dip ).mult( beta ) ) ) ).mult( r );
        Matrix rho = r2gg().InverseM();
        Vector dgx = new Vector( dgxx(), dgxy(), dgxz() );
        Vector dgy = new Vector( dgxy(), dgyy(), dgyz() );
        Vector dgz = new Vector( dgxz(), dgyz(), dgzz() );
        Matrix dag = new Matrix();
        dag.x.x = aG.x.dot( dgx );
        dag.x.y = aG.x.dot( dgy );
        dag.x.z = aG.x.dot( dgz );
        Matrix ag1 = ( gg().minus( r2bg().plus( gmmg( dip ).mult( beta ) ).plus( dag.mult( gamma ) ) ) ).timesM( rho );
        bG = bg0.plus( bg1.mult(alpha0) );
        aG = ag0.plus( ag1.mult(alpha0) );
      }
      {
        float r = 1 / r2m();
        Vector bm1 = ( m0().minus( r2am().plus( gmg( dip ).mult( beta ) ) ) ).mult( r );
        Matrix rho = r2mm().InverseM();
        Vector dmx = new Vector( dmxx(), dmxy(), dmxz() );
        Vector dmy = new Vector( dmxy(), dmyy(), dmyz() );
        Vector dmz = new Vector( dmxz(), dmyz(), dmzz() );
        Matrix dam = new Matrix();
        dam.x.x = aM.x.dot( dmx );
        dam.x.y = aM.x.dot( dmy );
        dam.x.z = aM.x.dot( dmz );
        Matrix am1 = ( mm().minus( r2bm().plus( gmgm( dip ).mult( beta ) ).plus( dam.mult( gamma ) ) ) ).timesM( rho );
        bM = bm0.plus( bm1.mult(alpha0) );
        aM = am0.plus( am1.mult(alpha0) );
      }
      dip = mean_dip();
      e0 = error( dip, beta, gamma );
      if ( e0 > e1 ) break;
      ++ iter;
    } while ( iter < TDSetting.mCalibMaxIt && e0 > TDSetting.mCalibEps );

    // LogMatrixVector( "final G", aG, bG );
    // LogMatrixVector( "final M", aM, bM );
    checkOverflow( bG, aG );
    checkOverflow( bM, aM );

    int nn = idx;
    Vector[] gr = new Vector[nn];
    Vector[] mr = new Vector[nn];
    for ( int i=0; i<nn; ++i ) {
      if ( group[i] > 0 ) {
        gr[i] = bG.plus( aG.timesV(g[i]) );
        mr[i] = bM.plus( aM.timesV(m[i]) );
      }
    }
    long group0 = -1;
    long cnt  = 0;
    mDelta    = 0.0f;
    mDelta2   = 0.0f;
    mMaxError = 0.0f;
    // Log.v("DistoX", "compute errors...");
    Vector vx;
    for ( int i=0; i<nn; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        vx = new Vector();
        int cx = 0;
        while ( i < nn && (group[i] == 0 || group[i] == group0) ) {
          if ( group[i] != 0 ) {
            computeBearingAndClinoRad( gr[i], mr[i] );
            vx.add( new Vector( b0, c0 ) ); 
            ++cx;
          }
          ++ i;
        }
        vx.scaleBy( 1.0f/cx );
        // Vector v0 = new Vector( (float)Math.cos(c0) * (float)Math.cos(b0),
        //                         (float)Math.cos(c0) * (float)Math.sin(b0),
        //                         (float)Math.sin(c0) );
        for (int j=first; j<i; ++j ) {
          if ( group[j] == 0 ) {
            err[j] = 0.0f;
          } else {
            computeBearingAndClinoRad( gr[j], mr[j] );
            Vector v = new Vector( b0, c0 );
            // Vector v = new Vector( (float)Math.cos(c0) * (float)Math.cos(b0),
            //                        (float)Math.cos(c0) * (float)Math.sin(b0),
            //                        (float)Math.sin(c0) );
            err[j] = vx.minus(v).Length(); // approx angle with 2*tan(alpha/2)
            mDelta  += err[j];
            mDelta2 += err[j] * err[j];
            if ( err[j] > mMaxError ) mMaxError = err[j];
            ++ cnt;
          }
        }
      }
    }
    mDelta  = mDelta / cnt;
    mDelta2 = (float)Math.sqrt(mDelta2/cnt - mDelta*mDelta);
    mDelta    *= TDMath.RAD2GRAD; // convert avg and std0-dev from radians to degrees
    mDelta2   *= TDMath.RAD2GRAD;
    mMaxError *= TDMath.RAD2GRAD;
    // Log.v("DistoX", "Delta " + mDelta + " " + mDelta2 + " cnt " + cnt + " max " + mMaxError );

    EnforceMax2( bG, aG );
    EnforceMax2( bM, aM );

    // for (int i=0; i<nn; ++i ) {
    //   if ( group[i] > 0 ) {
    //     Vector dg = gx[i].minus( gr[i] );
    //     Vector dm = mx[i].minus( mr[i] );
    //     err[i] = dg.dot(dg) + dm.dot(dm);
    //     mDelta  += err[i];
    //     mDelta2 += err[i] * err[i];
    //     err[i] = (float)Math.sqrt( err[i] );
    //   } else {
    //     err[i] = 0.0f;
    //   }
    // }
    // mDelta = 100 * (float)Math.sqrt( mDelta*invNum );
    return iter;
  }

  // -----------------------------------------------------------------------

  /** add the errors for a group of sensor-data to the stats
   * each error is the length of the vector-difference between the unit-vector directions.
   * this approximates the angle between the two directions:
   *   error = 2 tan(alpha/2) 
   * @param errors output vector to fill with errors (if not null)
   *               must have size as g1, m1
   */
  @Override
  public void addStatErrors( Vector g1[], Vector m1[], float[] errors )
  {
    int size = g1.length;
    Vector g2[] = new Vector[ size ];
    Vector m2[] = new Vector[ size ];
    for ( int k=0; k<size; ++k ) {
      g2[k] = scaledVector( g1[k] );
      m2[k] = scaledVector( m1[k] );
    }
    // Log.v("DistoX", "add stat errors: size " + size );

    Vector gr[] = new Vector[size];
    Vector mr[] = new Vector[size];
    Vector vx = new Vector();
    for ( int i=0; i<size; ++i ) {
      gr[i] = bG.plus( aG.timesV(g2[i]) );
      mr[i] = bM.plus( aM.timesV(m2[i]) );
      computeBearingAndClinoRad( gr[i], mr[i] );
      vx.add( new Vector( b0, c0 ) );
    }
    vx.scaleBy( 1.0f/size );
    float err = 0.0f;
    for ( int i=0; i<size; ++i ) {
      computeBearingAndClinoRad( gr[i], mr[i] );
      Vector v1 = new Vector( b0, c0 );
      float e = v1.minus(vx).Length();
      if ( errors != null ) errors[i] = (float)e;
      // Log.v("DistoX", e + " " + g[i].x + " " + g[i].y + " " + g[i].z );
      mSumCount += 1;
      mSumErrors += e;
      mSumErrorSquared += e*e;
    }
  }
}
  
