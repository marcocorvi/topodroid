/* @file CalibAlgoBH.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX Beat Heeb calibration algorithm
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 * This calibration algorithm is published in 
 *   B. Heeb 
 *   A general calibration algorithm for a 3-axis compass/clino devices
 *   CREG Journal 73
 * The C# source code for both the linear and the non-linear algorithms 
 * hev been provided courtesy of Beat Heeb.
 * This is a Java re-writing of the code.
 * It differs from the original code in the final error evaluation and reporting.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;

import java.lang.Math;

// import java.util.Locale;

// used by logCoeff

public class CalibAlgoBH extends CalibAlgo
{
  // private boolean mNonLinear;
  // private TDVector nL;

  private TDVector gxp; // opt vectors
  private TDVector mxp;
  private TDVector gxt; // turn vectors
  private TDVector mxt;
  // float b0=0.0f, c0=0.0f; // bearing and clino

  // ==============================================================
  /** construct a Calibration from the saved coefficients
   */
  public CalibAlgoBH( byte[] coeff, boolean nonLinear )
  {
    super( coeff, nonLinear );
    // mNonLinear = nonLinear;
    // coeffToNL( coeff, nL );
  }


  // @param N         number of calib data
  // @param nonLinear  whether to use non-linear algo
  public CalibAlgoBH( int N, boolean nonLinear )
  {
    super( N, nonLinear );
    // mNonLinear = nonLinear;
  }

  // void setAlgorithm( boolean nonLinear ) { mNonLinear = nonLinear; }

  // public TDVector GetNL() { return nL; }

  // public int nrCoeff() { return mNonLinear ? 52 : 48; }


  public int Calibrate()
  {
    mDelta = 0.0f;
    TDLog.Log( TDLog.LOG_CALIB, "Calibrate: data nr. " + idx + " algo " + (mNonLinear? "non-" : "") + "linear" );
    if ( idx < 16 ) return -1;
    return Optimize( idx, g, m );
  }

  // ------------------------------------------------------------
  // private methods

  private void InitializeAB( TDVector avG, TDVector avM )
  {
    aG = new TDMatrix( TDMatrix.one );
    aM = new TDMatrix( TDMatrix.one );
// FIXME NL
    if ( mNonLinear ) {
      bG = new TDVector( -avG.x, -avG.y, -avG.z );
      bM = new TDVector( -avM.x, -avM.y, -avM.z );
    } else {
      bG = new TDVector();
      bM = new TDVector();
    }
    nL = new TDVector();   // initialize to zero vector
  }

  // compute (gxp, mxp)
  private void OptVectors( TDVector gr, TDVector mr, float s, float c )
  {
    TDVector no = gr.cross( mr );
    no.normalize();
    gxp = ( (mr.times(c)).plus( (mr.cross(no)).times(s) ) ).plus(gr);
    gxp.normalize();
    mxp =   (gxp.times(c)).plus( (no.cross(gxp)).times(s) );
  }

  // compute (gxt, mxt)  
  private void TurnVectors( TDVector gf, TDVector mf, TDVector gr, TDVector mr )
  {
    float s1 = gr.z * gf.y - gr.y * gf.z + mr.z * mf.y - mr.y * mf.z;
    float c1 = gr.y * gf.y + gr.z * gf.z + mr.y * mf.y + mr.z * mf.z;
    float d1 = (float)Math.sqrt( c1*c1 + s1*s1 );
    s1 /= d1;
    c1 /= d1;
    gxt = gf.TurnX( s1, c1 );
    mxt = mf.TurnX( s1, c1 );
  }

/* ============================================================ */

  private int Optimize( int nn, TDVector[] g, TDVector [] m )
  {
    int max_it = TDSetting.mCalibMaxIt;
    float eps  = TDSetting.mCalibEps;
    // TDLog.v( "Calib Algo BH eps " + eps + " iter " + max_it );

    // int num = g.Length();
    TDVector[] gr = new TDVector[nn];
    TDVector[] mr = new TDVector[nn];
    TDVector[] gx = new TDVector[nn];
    TDVector[] mx = new TDVector[nn];
    TDVector[] gl = null;
    TDMatrix[] gs = null;
    if ( mNonLinear ) {
      gl = new TDVector[nn]; // linearized g values
      gs = new TDMatrix[nn]; // Diagonal(g^2 - 1/2)
    }

    TDMatrix aG0;
    TDMatrix aM0;

    TDVector sumG = new TDVector();
    TDVector sumM = new TDVector();
    TDMatrix sumG2 = new TDMatrix();
    TDMatrix sumM2 = new TDMatrix();

    float sa = 0.0f;
    float ca = 0.0f;
    float invNum = 0.0f;
    for (int i=0; i<nn; ++i ) {
      if ( group[i] > 0 ) {
        invNum += 1.0f;
        sa += ( g[i].cross( m[i] )).Length(); // cross product
        ca += g[i].dot( m[i] );               // dot product
        sumG.plusEqual( g[i] );
        sumM.plusEqual( m[i] );
        sumG2.plusEqual( new TDMatrix(g[i],g[i]) );   // outer product
        sumM2.plusEqual( new TDMatrix(m[i],m[i]) );
        if ( gl != null /* mNonLinear */ ) {
          gl[i] = new TDVector(g[i]);
        }
        if ( gs != null /* mNonLinear */ ) {
          gs[i] = new TDMatrix();               // zero matrix
          gs[i].x.x = g[i].x * g[i].x - 0.5f; // diagonal elements
          gs[i].y.y = g[i].y * g[i].y - 0.5f;
          gs[i].z.z = g[i].z * g[i].z - 0.5f;
        }
      }
    }
    if ( invNum < 0.5f ) return 0;

    invNum = 1.0f / invNum;

    // FIXME here and below was InverseT because Beat's code Calibration.cs used 
    // the inverse of the transposed (ok only for symmetric matrices).
    // Beat's NLCalibration.cs uses TDMatrix.Inverse( TDMatrix m ) which is
    //    m = Transposed(m);
    //    TDMatrix ad = new TDMatrix( m.y % m.z, m.z % m.x, m.x % m.y ); // Vector.operator% is the cross-product
    //    return ad * ( 1 / m.x * ad.x ); // adjugate * 1/determinant
    // which is InverseM 

    TDVector avG = sumG.times( invNum );  // average G
    TDVector avM = sumM.times( invNum );  // average M
    TDMatrix invG = (sumG2.minus( new TDMatrix(sumG, avG) ) ).InverseM();  // inverse of the transposed
    TDMatrix invM = (sumM2.minus( new TDMatrix(sumM, avM) ) ).InverseM();

    // TDLog.Log( TDLog.LOG_CALIB, "Number", nn );
    // TDLog.Log( TDLog.LOG_CALIB, "invG", invG, avG );
    // TDLog.Log( TDLog.LOG_CALIB, "invM", invM, avM ); // this is OK
    // LogMatrixVector( "initial inverse|average G", invG, avG );
    // LogMatrixVector( "initial inverse|average M", invM, avM );

    InitializeAB( avG, avM ); // nL is also initialized to Vector_Zero
    // LogAB( 0, aG, bG, aM, bM ); // this is OK

    int it = 0;
    float da = (float)Math.sqrt( ca*ca + sa*sa );
    float s = sa / da;
    float c = ca / da;
    // LogSC( "sin/cos", s, c ); // this is OK
// FIXME NL
    // float alpha = TDMath.atan2( sa, ca );


    do {
      for ( int i=0; i<nn; ++i ) {
        if ( group[i] > 0 ) {
          if ( mNonLinear ) {
            gr[i] = bG.plus( aG.timesV(gl[i]) ); // NON_LINEAR: gl instead of g
          } else {
            gr[i] = bG.plus( aG.timesV(g[i]) );
          }
          mr[i] = bM.plus( aM.timesV(m[i]) );
        }
      }
      sa = 0.0f;
      ca = 0.0f;
      long group0 = -1;
      for ( int i=0; i<nn; ) {
        if ( group[i] <= 0 ) { // N.B. group[] is >= 0 by CalibAlgo class
          ++i;
        } else if ( group[i] != group0 ) {
          group0 = group[i];
          TDVector grp = new TDVector();
          TDVector mrp = new TDVector();
          int first = i;
          while ( i < nn && (group[i] <= 0 || group[i] == group0) ) {
            // group must be positive integer: group <= 0 means to skip
            if ( group[i] > 0 ) {
              TurnVectors( gr[i], mr[i], gr[first], mr[first] ); // output ==> gxt, mxt
              grp.plusEqual( gxt );
              mrp.plusEqual( mxt );
            }
            ++ i;
          }
          OptVectors( grp, mrp, s, c ); // output ==> gxp, mxp

          sa += (mrp.cross(gxp)).Length();
          ca += mrp.dot(gxp);
          for (int j = first; j < i; ++j ) {
            if ( group[j] > 0 ) {
              TurnVectors( gxp, mxp, gr[j], mr[j] ); // output ==> gxt, mxt
              gx[j] = new TDVector( gxt );
              mx[j] = new TDVector( mxt );
            }
          }
        }
      }
      da = (float)Math.sqrt( ca*ca + sa*sa );
      s = sa / da;
      c = ca / da;
      // LogSC( "sin/cos", s, c );
      TDVector avGx = new TDVector();
      TDVector avMx = new TDVector();
      TDMatrix sumGxG = new TDMatrix();
      TDMatrix sumMxM = new TDMatrix();
      for (int i=0; i<nn; ++i ) {
        if ( group[i] > 0 ) {
          avGx.plusEqual( gx[i] );
          avMx.plusEqual( mx[i] );
          if ( mNonLinear ) {
            sumGxG.plusEqual( new TDMatrix( gx[i], gl[i] ) ); // NON_LINEAR gl instead of g
          } else {
            sumGxG.plusEqual( new TDMatrix( gx[i], g[i] ) );
          }
          sumMxM.plusEqual( new TDMatrix( mx[i], m[i] ) );
        } 
      }
      aG0 = new TDMatrix( aG );
      aM0 = new TDMatrix( aM );
      avGx.timesEqual( invNum );
      avMx.timesEqual( invNum );
      // LogMatrixVector( "average G", sumGxG, avGx );
      // LogMatrixVector( "average M", sumMxM, avMx );

      aG = (sumGxG.minus( new TDMatrix(avGx, sumG) )).timesT( invG ); // multiplication by the transposed
      aM = (sumMxM.minus( new TDMatrix(avMx, sumM) )).timesT( invM );

      aG.z.y = (aG.y.z + aG.z.y) * 0.5f; // enforce symmetric aG(y,z)
      aG.y.z = aG.z.y;

      bG = avGx.minus( aG.timesV(avG) ); // get new bG and bM
      bM = avMx.minus( aM.timesV(avM) );
      // LogMatrixVector( "G", aG, bG );
      // LogMatrixVector( "M", aM, bM );

      float gmax = aG.MaxDiff(aG0);
      float mmax = aM.MaxDiff(aM0);
      if ( mNonLinear ) { // get new non-linearity coefficients
        TDMatrix psum = new TDMatrix();
        TDVector qsum = new TDVector();
        for (int ii = 0; ii < nn; ii++) {
          if ( group[ii] > 0 ) {
            TDMatrix p = aG.timesM( gs[ii] );
            TDVector q = ( gx[ii].minus( aG.timesV( g[ii] ) ) ).minus( bG );
            TDMatrix pt = p.Transposed();

            // psum = (P^t * P) N.B. psum^t = psum
            psum.plusEqual( pt.timesT( pt ) ); // psum.plusEqual( pt.timesM( p ) ); 
            qsum.plusEqual( pt.timesV( q ) );
          }
        }
        nL = ( psum.InverseM()).timesV( qsum );
        saturate( nL );

        sumG  = new TDVector(); // recalculate linearized g values
        sumG2 = new TDMatrix();
        for (int ii = 0; ii < nn; ii++) {
          if ( group[ii] > 0 ) {
            gl[ii] = g[ii].plus( gs[ii].timesV( nL ) );
            sumG.plusEqual( gl[ii] ); // sum up g and g^2
            sumG2.plusEqual( new TDMatrix(gl[ii], gl[ii]) ); // outer product
          }
        }
        avG  = sumG.times( invNum ); // average g
        invG = (sumG2.minus( new TDMatrix(sumG, avG)) ).InverseM(); // inverse of the transposed
      }
      ++ it;
    } while ( it < max_it && ( aG.MaxDiff(aG0) > eps || aM.MaxDiff(aM0) > eps ) );

    // LogMatrixVector( "final G", aG, bG );
    // LogMatrixVector( "final M", aM, bM );
    checkOverflow( bG, aG );
    checkOverflow( bM, aM );

    // this is beat's delta
    int cnt_bh = 0;
    mDeltaBH = 0.0f;
    for ( int i=0; i<nn; ++i ) {
      if ( group[i] > 0 ) {
        TDVector dg = gx[i].minus( gr[i] );
        TDVector dm = mx[i].minus( mr[i] );
        mDeltaBH += dg.LengthSquared() + dm.LengthSquared();
        ++ cnt_bh;
      }
    }
    if ( cnt_bh > 0 ) {
      mDeltaBH = TDMath.sqrt( mDeltaBH / cnt_bh ) * 100;
      // TDLog.v("delta BH " + mDeltaBH + " cnt " + cnt_bh );
    }

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
    long cnt  = 0;
    mDelta    = 0.0f;
    mDelta2   = 0.0f;
    mMaxError = 0.0f;
    // TDLog.v( "compute errors...");
    for ( int i=0; i<nn; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        TDVector grp = new TDVector();
        TDVector mrp = new TDVector();
        int first = i;
        while ( i < nn && (group[i] <= 0 || group[i] == group0) ) {
          if ( group[i] > 0 ) {
            TurnVectors( gr[i], mr[i], gr[first], mr[first] );
            grp.plusEqual( gxt );
            mrp.plusEqual( mxt );
          }
          ++ i;
        }
        OptVectors( grp, mrp, s, c );
        computeBearingAndClinoRad( gxp, mxp );
        TDVector v0 = new TDVector( b0, c0 );
        // TDLog.v( "group V " + v0.x + " " + v0.y + " " + v0.z );
        int cnt_gr = 0;
        float delta_gr = 0.0f;
        float delta2_gr = 0.0f;
        for (int j=first; j<i; ++j ) {
          if ( group[j] <= 0 ) {
            err[j] = 0.0f;
          } else {
            computeBearingAndClinoRad( gr[j], mr[j] );
            TDVector v = new TDVector( b0, c0 );
            err[j] = v0.minus(v).Length(); // approx angle with 2*tan(alpha/2)
            // TDLog.v( "Err" + err[j] + " V " + v.x + " " + v.y + " " + v.z );
            if ( err[j] > mMaxError ) mMaxError = err[j];
            delta_gr  += err[j];
            delta2_gr += err[j] * err[j];
            ++ cnt_gr;
          }
        }
        if ( cnt_gr > 1 ) {
          mDelta  += delta_gr;
          mDelta2 += delta2_gr;
          cnt += cnt_gr;
        }
      }
    }
    mDelta  = mDelta / cnt;
    mDelta2 = (float)Math.sqrt(mDelta2/cnt - mDelta*mDelta);
    mDelta    *= TDMath.RAD2DEG; // convert avg and std0-dev from radians to degrees
    mDelta2   *= TDMath.RAD2DEG;
    mMaxError *= TDMath.RAD2DEG;
    // TDLog.v("nn " + nn + " Delta " + mDelta + " " + mDelta2 + " cnt " + cnt + " max " + mMaxError );

    EnforceMax2( bG, aG );
    EnforceMax2( bM, aM );

    // for (int i=0; i<nn; ++i ) {
    //   if ( group[i] > 0 ) {
    //     TDVector dg = gx[i].minus( gr[i] );
    //     TDVector dm = mx[i].minus( mr[i] );
    //     err[i] = dg.dot(dg) + dm.dot(dm);
    //     mDelta  += err[i];
    //     mDelta2 += err[i] * err[i];
    //     err[i] = (float)Math.sqrt( err[i] );
    //   } else {
    //     err[i] = 0.0f;
    //   }
    // }
    // mDelta = 100 * (float)Math.sqrt( mDelta*invNum );
    return it;
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
  public void addStatErrors( TDVector[] g1, TDVector[] m1, float[] errors )
  {
    int size = g1.length;
    TDVector[] g = new TDVector[ size ];
    TDVector[] m = new TDVector[ size ];
    TDVector[] gl = new TDVector[ size ];
    for ( int k=0; k<size; ++k ) {
      g[k] = scaledVector( g1[k] );
      m[k] = scaledVector( m1[k] );
    }
    // TDLog.v( "add stat errors: size " + size );
    if ( mNonLinear ) {
      TDMatrix gs = new TDMatrix();
      for ( int k=0; k<size; ++k ) {
        gs.x.x = g[k].x * g[k].x - 0.5f;
        gs.y.y = g[k].y * g[k].y - 0.5f;
        gs.z.z = g[k].z * g[k].z - 0.5f;
        gl[k] = g[k].plus( gs.timesV( nL ) );
      }
    } else {
	  // for ( int k=0; k<size; ++k ) gl[k] = g[k];
      System.arraycopy(g, 0, gl, 0, size);
    }
    TDVector grp = new TDVector();
    TDVector mrp = new TDVector();
    TDVector[] gr = new TDVector[size];
    TDVector[] mr = new TDVector[size];
    for ( int i=0; i<size; ++i ) {
      if ( mNonLinear ) {
        gr[i] = bG.plus( aG.timesV(gl[i]) );
      } else {
        gr[i] = bG.plus( aG.timesV(g[i]) );
      }
      mr[i] = bM.plus( aM.timesV(m[i]) );
      TurnVectors( gr[i], mr[i], gr[0], mr[0] );
      grp.plusEqual( gxt );
      mrp.plusEqual( mxt );
    }
    computeBearingAndClinoRad( grp, mrp );
    TDVector v0 = new TDVector( b0, c0 );
    // TDVector v0 = new TDVector( (float)Math.cos(c0) * (float)Math.cos(b0),
    //                         (float)Math.cos(c0) * (float)Math.sin(b0),
    //                         (float)Math.sin(c0) );
    double err = 0.0;
    for ( int i=0; i<size; ++i ) {
      computeBearingAndClinoRad( gr[i], mr[i] );
      TDVector v1 = new TDVector( b0, c0 );
      // TDVector v1 = new TDVector( (float)Math.cos(c0) * (float)Math.cos(b0),
      //                         (float)Math.cos(c0) * (float)Math.sin(b0),
      //                         (float)Math.sin(c0) );
      double e = v1.minus(v0).Length();
      if ( errors != null ) errors[i] = (float)e;
      // TDLog.v( e + " " + g[i].x + " " + g[i].y + " " + g[i].z );
      mSumCount += 1;
      mSumErrors += e;
      mSumErrorSquared += e*e;
    }
  }
}
  
