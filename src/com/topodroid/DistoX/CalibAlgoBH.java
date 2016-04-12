/* @file CalibAlgoBH.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX Beat Heeb's calibration algorithm
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 * This calibration algorithm is published in 
 *   B. Heeb 
 *   A general calibration algorithm for a 3-axis compass/clinometer devices
 *   CREG Journal 73
 * The C# source code for both the linear and the non-linear algorithms 
 * hev been provided courtesy of Beat Heeb.
 * This is a Java re-writing of the code.
 * It differs from the original code in the final error evaluation and reporting.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.Math;

import java.util.Locale;

// used by logCoeff
import android.util.Log;

public class CalibAlgoBH extends CalibAlgo
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
  CalibAlgoBH( byte[] coeff, boolean nonLinear )
  {
    super( coeff, nonLinear );
    // mNonLinear = nonLinear;
    // coeffToNL( coeff, nL );
  }


  public CalibAlgoBH( int N, boolean nonLinear )
  {
    super( N, nonLinear );
    // mNonLinear = nonLinear;
  }

  // void setAlgorith( boolean nonLinear ) { mNonLinear = nonLinear; }

  // public Vector GetNL() { return nL; }

  // public int nrCoeff() { return mNonLinear ? 52 : 48; }


  public int Calibrate()
  {
    mDelta = 0.0f;
    TDLog.Log( TDLog.LOG_CALIB, "Calibrate: data nr. " + idx 
                      + " algo " + (mNonLinear? "non-" : "") + "linear" );
    if ( idx < 16 ) return -1;
    return Optimize( idx, g, m );
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

  // compute (gxp, mxp)
  private void OptVectors( Vector gr, Vector mr, float s, float c )
  {
    Vector no = gr.cross( mr );
    no.Normalized();
    gxp = ( (mr.mult(c)).plus( (mr.cross(no)).mult(s) ) ).plus(gr);
    gxp.Normalized();
    mxp =   (gxp.mult(c)).plus( (no.cross(gxp)).mult(s) );
  }

  // compute (gxt, mxt)  
  private void TurnVectors( Vector gf, Vector mf, Vector gr, Vector mr )
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

  private int Optimize( int nn, Vector[] g, Vector [] m )
  {
    int max_it = TDSetting.mCalibMaxIt;
    float eps  = TDSetting.mCalibEps;

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
        ca += g[i].dot( m[i] );               // dot product
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

    // FIXME here and below was InverseT because Beat's code Calibration.cs used 
    // the inverse of the transposed (ok only for symmetric matrices).
    // Beat's NLCalibration.cs uses Matrix.Inverse( Matrix m ) which is
    //    m = Transposed(m);
    //    Matrix ad = new Matrix( m.y % m.z, m.z % m.x, m.x % m.y ); // Vector.operator% is the cross-product
    //    return ad * ( 1 / m.x * ad.x ); // adjugate * 1/determinant
    // which is InverseM 

    Vector avG = sumG.mult( invNum );  // average G
    Vector avM = sumM.mult( invNum );  // average M
    Matrix invG = (sumG2.minus( new Matrix(sumG, avG) ) ).InverseM();  // inverse of the transposed
    Matrix invM = (sumM2.minus( new Matrix(sumM, avM) ) ).InverseM();

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
        if ( group[i] <= 0 ) {
          ++i;
        } else if ( group[i] != group0 ) {
          group0 = group[i];
          Vector grp = new Vector();
          Vector mrp = new Vector();
          int first = i;
          while ( i < nn && (group[i] == 0 || group[i] == group0) ) {
            // group must be positive integer: group == 0 means to skip
            if ( group[i] > 0 ) {
              TurnVectors( gr[i], mr[i], gr[first], mr[first] ); // output ==> gxt, mxt
              grp.add( gxt );
              mrp.add( mxt );
            }
            ++ i;
          }
          OptVectors( grp, mrp, s, c ); // output ==> gxp, mxp

          sa += (mrp.cross(gxp)).Length();
          ca += mrp.dot(gxp);
          for (int j = first; j < i; ++j ) {
            if ( group[j] != 0 ) {
              TurnVectors( gxp, mxp, gr[j], mr[j] ); // output ==> gxt, mxt
              gx[j] = new Vector( gxt );
              mx[j] = new Vector( mxt );
            }
          }
        }
      }
      da = (float)Math.sqrt( ca*ca + sa*sa );
      s = sa / da;
      c = ca / da;
      // LogSC( "sin/cos", s, c );
      Vector avGx = new Vector();
      Vector avMx = new Vector();
      Matrix sumGxG = new Matrix();
      Matrix sumMxM = new Matrix();
      for (int i=0; i<nn; ++i ) {
        if ( group[i] > 0 ) {
          avGx.add( gx[i] );
          avMx.add( mx[i] );
          if ( mNonLinear ) {
            sumGxG.add( new Matrix( gx[i], gl[i] ) ); // NON_LINEAR gl instead of g
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
      // LogMatrixVector( "average G", sumGxG, avGx );
      // LogMatrixVector( "average M", sumMxM, avMx );

      aG = (sumGxG.minus( new Matrix(avGx, sumG) )).timesT( invG ); // multiplication by the transposed
      aM = (sumMxM.minus( new Matrix(avMx, sumM) )).timesT( invM );

      aG.z.y = (aG.y.z + aG.z.y) * 0.5f; // enforce symmetric aG(y,z)
      aG.y.z = aG.z.y;

      bG = avGx.minus( aG.timesV(avG) ); // get new bG and bM
      bM = avMx.minus( aM.timesV(avM) );
      // LogMatrixVector( "G", aG, bG );
      // LogMatrixVector( "M", aM, bM );

      float gmax = aG.MaxDiff(aG0);
      float mmax = aM.MaxDiff(aM0);
      if ( mNonLinear ) { // get new non-linearity coefficients
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
        nL = ( psum.InverseM()).timesV( qsum );
        saturate( nL );

        sumG  = new Vector(); // recalculate linearized g values
        sumG2 = new Matrix();
        for (int ii = 0; ii < nn; ii++) {
          if ( group[ii] > 0 ) {
            gl[ii] = g[ii].plus( gs[ii].timesV( nL ) );
            sumG.add( gl[ii] ); // sum up g and g^2
            sumG2.add( new Matrix(gl[ii], gl[ii]) ); // outer product
          }
        }
        avG  = sumG.mult( invNum ); // average g
        invG = (sumG2.minus( new Matrix(sumG, avG)) ).InverseM(); // inverse of the transposed
      }
      ++ it;
    } while ( it < max_it && ( aG.MaxDiff(aG0) > eps || aM.MaxDiff(aM0) > eps ) );

    // LogMatrixVector( "final G", aG, bG );
    // LogMatrixVector( "final M", aM, bM );
    checkOverflow( bG, aG );
    checkOverflow( bM, aM );

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
    // Log.v("DistoX", "compute errors...");
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
            TurnVectors( gr[i], mr[i], gr[first], mr[first] );
            grp.add( gxt );
            mrp.add( mxt );
          }
          ++ i;
        }
        OptVectors( grp, mrp, s, c );
        computeBearingAndClinoRad( gxp, mxp );
        Vector v0 = new Vector( b0, c0 );
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
            err[j] = v0.minus(v).Length(); // approx angle with 2*tan(alpha/2)
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
  public void addStatErrors( Vector g1[], Vector m1[], float[] errors )
  {
    int size = g1.length;
    Vector g[] = new Vector[ size ];
    Vector m[] = new Vector[ size ];
    Vector gl[] = new Vector[ size ];
    for ( int k=0; k<size; ++k ) {
      g[k] = scaledVector( g1[k] );
      m[k] = scaledVector( m1[k] );
    }
    Log.v("DistoX", "add stat errors: size " + size );
    if ( mNonLinear ) {
      Matrix gs = new Matrix();
      for ( int k=0; k<size; ++k ) {
        gs.x.x = g[k].x * g[k].x - 0.5f;
        gs.y.y = g[k].y * g[k].y - 0.5f;
        gs.z.z = g[k].z * g[k].z - 0.5f;
        gl[k] = g[k].plus( gs.timesV( nL ) );
      }
    } else {
      for ( int k=0; k<size; ++k ) gl[k] = g[k];
    }
    Vector grp = new Vector();
    Vector mrp = new Vector();
    Vector gr[] = new Vector[size];
    Vector mr[] = new Vector[size];
    for ( int i=0; i<size; ++i ) {
      if ( mNonLinear ) {
        gr[i] = bG.plus( aG.timesV(gl[i]) );
      } else {
        gr[i] = bG.plus( aG.timesV(g[i]) );
      }
      mr[i] = bM.plus( aM.timesV(m[i]) );
      TurnVectors( gr[i], mr[i], gr[0], mr[0] );
      grp.add( gxt );
      mrp.add( mxt );
    }
    computeBearingAndClinoRad( grp, mrp );
    Vector v0 = new Vector( b0, c0 );
    // Vector v0 = new Vector( (float)Math.cos(c0) * (float)Math.cos(b0),
    //                         (float)Math.cos(c0) * (float)Math.sin(b0),
    //                         (float)Math.sin(c0) );
    double err = 0.0;
    for ( int i=0; i<size; ++i ) {
      computeBearingAndClinoRad( gr[i], mr[i] );
      Vector v1 = new Vector( b0, c0 );
      // Vector v1 = new Vector( (float)Math.cos(c0) * (float)Math.cos(b0),
      //                         (float)Math.cos(c0) * (float)Math.sin(b0),
      //                         (float)Math.sin(c0) );
      double e = v1.minus(v0).Length();
      if ( errors != null ) errors[i] = (float)e;
      // Log.v("DistoX", e + " " + g[i].x + " " + g[i].y + " " + g[i].z );
      mSumCount += 1;
      mSumErrors += e;
      mSumErrorSquared += e*e;
    }
  }
}
  
