/* @file CalibAlgoMin.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration algorithm Error minimization
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.Math;

// import java.util.Locale;

// used by logCoeff
// import android.util.Log;

class CalibAlgoMin extends CalibAlgo
{
  // private boolean mNonLinear;
  // private Vector nL;

  private Vector gxp; // opt vectors
  private Vector mxp;
  private Vector gxt; // turn vectors
  private Vector mxt;

  private float invNN;
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


  CalibAlgoMin( int idx, boolean nonLinear )
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
    g.normalize();
    return (float)Math.acos( g.x ) * TDMath.RAD2DEG;
  }

  float azimuth( Vector gs, Vector ms )
  {
    Vector g = bG.plus( aG.timesV( gs ) );
    g.normalize();
    float gx = g.x;

    Vector m = bM.plus( aM.timesV( ms ) );
    m.normalize();
    float gm = g.dot( m );
 
    return (float)Math.acos( (gx*gm - m.x)/Math.sqrt((1-gx*gx)*(1-gm*gm)) ) * TDMath.RAD2DEG;

    // Vector e = g ^ m;
    // Vector n = e ^ g;
    // n.normalize();
    // Vector x(1,0,0);
    // Vector xh = x - g * g.x;
    // xh.normalize();
    // return acos( xh * n ) * 180 / M_PI;
  }

  private Vector G( int n ) { return bG.plus( aG.timesV( g[n] ) ); }
  private Vector M( int n ) { return bM.plus( aM.timesV( m[n] ) ); }

  private float mean_dip()
  {
    float sum = 0;
    for (int i=0; i<idx; ++i ) sum += G(i).dot( M(i) );
    return sum / idx;
  }

  private Vector mean_g()
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( g[i] );
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Vector mean_m()
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( m[i] );
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Matrix mean_gg()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) sum.plusEqual( new Matrix( g[i], g[i] ) );
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Matrix mean_mm()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) sum.plusEqual( new Matrix( m[i], m[i] ) );
    sum.timesEqual( invNN );
    return sum;
  }
  
  private float dgx2()
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
    
  private float dmx2()
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
  

  private Matrix dgxyz()
  {
    Matrix sum = new Matrix();
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        float xx = 0;
        float yy = 0;
        float zz = 0;
        while ( i < idx && (group[i] == 0 || group[i] == group0) ) {
          if ( group[i] != 0 ) {
            Vector v = g[i];
            xx += v.x;
            yy += v.y;
            zz += v.z;
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          xx /= c;
          yy /= c;
          zz /= c;
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              Vector v = g[j];
              sum.x.x += (xx-v.x) * (xx-v.x);
              sum.x.y += (xx-v.x) * (yy-v.y);
              sum.x.z += (xx-v.x) * (zz-v.z);
              sum.y.y += (yy-v.y) * (yy-v.y);
              sum.y.z += (yy-v.y) * (zz-v.z);
              sum.z.z += (zz-v.z) * (zz-v.z);
            }
          }
        }
      }
    }
    sum.timesEqual( invNN );
    sum.y.x = sum.x.y;
    sum.z.x = sum.x.z;
    sum.z.y = sum.y.z;
    return sum;
  }

  private Matrix dmxyz()
  {
    Matrix sum = new Matrix();
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        float xx = 0;
        float yy = 0;
        float zz = 0;
        while ( i < idx && (group[i] == 0 || group[i] == group0) ) {
          if ( group[i] != 0 ) {
            Vector v = m[i];
            xx += v.x;
            yy += v.y;
            zz += v.z;
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          xx /= c;
          yy /= c;
          zz /= c;
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              Vector v = m[j];
              sum.x.x += (xx-v.x) * (xx-v.x);
              sum.x.y += (xx-v.x) * (yy-v.y);
              sum.x.z += (xx-v.x) * (zz-v.z);
              sum.y.y += (yy-v.y) * (yy-v.y);
              sum.y.z += (yy-v.y) * (zz-v.z);
              sum.z.z += (zz-v.z) * (zz-v.z);
            }
          }
        }
      }
    }
    sum.timesEqual( invNN );
    sum.y.x = sum.x.y;
    sum.z.x = sum.x.z;
    sum.z.y = sum.y.z;
    return sum;
  }

  private Vector dbgGM()
  {
    Vector sum = new Vector();
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        Vector xx = new Vector();
        float gmx = 0;
        while ( i < idx && (group[i] == 0 || group[i] == group0) ) {
          if ( group[i] != 0 ) {
            Vector gg = G(i);
            Vector mm = M(i);
            gmx += gg.cross( mm ).x;
            xx.plusEqual( mm.crossX() );
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          xx.timesEqual( 1/c );
          gmx /= c;
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              Vector gg = G(j);
              Vector mm = M(j);
              sum.plusEqual( ( xx.minus( mm.crossX() ) ).times( gmx - gg.cross( mm ).x ) );
            }
          }
        }
      }
    }
    sum.timesEqual( invNN );
    return sum;
  }

  private Matrix dagGM()
  {
    Matrix sum = new Matrix();
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        Matrix xx = new Matrix();
        float gmx = 0;
        while ( i < idx && (group[i] == 0 || group[i] == group0) ) {
          if ( group[i] != 0 ) {
            Vector gg = G(i);
            Vector mm = M(i);
            gmx += gg.cross( mm ).x;
            xx.plusEqual( new Matrix( gg, mm.crossX() ) );
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          xx.timesEqual( 1/c );
          gmx /= c;
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              Vector gg = G(j);
              Vector mm = M(j);
              Matrix zz = xx.minus( new Matrix( gg, mm.crossX() ) );
              zz.timesEqual( gmx - gg.cross( mm ).x );
              sum.plusEqual( zz );
            }
          }
        }
      }
    }
    sum.timesEqual( invNN );
    return sum;
  }

  private Vector dbmGM()
  {
    Vector sum = new Vector();
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        Vector xx = new Vector();
        float gmx = 0;
        while ( i < idx && (group[i] == 0 || group[i] == group0) ) {
          if ( group[i] != 0 ) {
            Vector gg = G(i);
            Vector mm = M(i);
            gmx += mm.cross( gg ).x;
            xx.plusEqual( gg.crossX() );
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          xx.timesEqual( 1/c );
          gmx /= c;
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              Vector gg = G(j);
              Vector mm = M(j);
              sum.plusEqual( ( xx.minus( gg.crossX() ) ).times( gmx - mm.cross( gg ).x ) );
            }
          }
        }
      }
    }
    sum.timesEqual( invNN );
    return sum;
  }

  private Matrix damGM()
  {
    Matrix sum = new Matrix();
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        Matrix xx = new Matrix();
        float gmx = 0;
        while ( i < idx && (group[i] == 0 || group[i] == group0) ) {
          if ( group[i] != 0 ) {
            Vector gg = G(i);
            Vector mm = M(i);
            gmx += mm.cross( gg ).x;
            xx.plusEqual( new Matrix( mm, gg.crossX() ) );
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          xx.timesEqual( 1/c );
          gmx /= c;
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              Vector gg = G(j);
              Vector mm = M(j);
              Matrix zz = xx.minus( new Matrix( mm, gg.crossX() ) );
              zz.timesEqual( gmx - mm.cross( gg ).x );
              sum.plusEqual( zz );
            }
          }
        }
      }
    }
    sum.timesEqual( invNN );
    return sum;
  }

  // ========================================================
  
  private float r2g()
  {
    float sum = 0;
    for (int i=0; i<idx; ++i ) sum += G(i).LengthSquared();
    return sum / idx;
  }
  
  private float r2m() // < (Bm+Am*ms) * (Bm+Am*ms) >
  {
    float sum = 0;
    for (int i=0; i<idx; ++i ) sum += M(i).LengthSquared();
    return sum / idx;
  }
  
  private Matrix r2gg()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) {
      Matrix mu = new Matrix(g[i], g[i]);
      mu.timesEqual( G(i).LengthSquared() );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Matrix r2mm()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) {
      Matrix mu = new Matrix(m[i], m[i]);
      mu.timesEqual( M(i).LengthSquared() );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Vector g0() // <Bg + Ag * gs>
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( G(i) );
    sum.timesEqual( invNN );
    return sum;
  }
    
  private Vector m0() // <Bm + Am * ms>
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( M(i) );
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Matrix gg()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) sum.plusEqual( new Matrix( G(i), g[i] ) );
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Matrix mm()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) sum.plusEqual( new Matrix( M(i), m[i] ) );
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Vector r2ag()
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( g[i].times( G(i).LengthSquared() ) );
    sum.timesEqual( invNN );
    return aG.timesV( sum );
  }
    
  private Vector r2am()
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( m[i].times( M(i).LengthSquared() ) );
    sum.timesEqual( invNN );
    return aM.timesV( sum );
  }
  
  private Vector gmm( float d ) // (G*M - d) M
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) {
      Vector v = M(i);
      sum.plusEqual( v.times( G(i).dot(v) - d) );
    }
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Vector gmg( float d ) // (G*M - d) G
  {
    Vector sum = new Vector();
    for (int i=0; i<idx; ++i ) {
      Vector v = G(i);
      sum.plusEqual( v.times( M(i).dot(v) - d ) );
    }
    sum.timesEqual( invNN );
    return sum;
  }
   
  private Matrix r2bg()
  {
    Matrix sum = new Matrix(); 
    for (int i=0; i<idx; ++i ) {
      Matrix mu = new Matrix( bG, g[i]);
      mu.timesEqual( G(i).LengthSquared() );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
    return sum;
  }
      
  private Matrix r2bm()
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) {
      Matrix mu = new Matrix( bM, m[i]);
      mu.timesEqual( M(i).LengthSquared() );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Matrix gmmg( float d )
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) {
      Vector v = M(i);
      Matrix mu = new Matrix(v, g[i]);
      mu.timesEqual( G(i).dot(v) - d );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
    return sum;
  }
  
  private Matrix gmgm( float d )
  {
    Matrix sum = new Matrix();
    for (int i=0; i<idx; ++i ) {
      Vector v = G(i);
      Matrix mu = new Matrix(v, m[i]);
      mu.timesEqual( M(i).dot(v) - d );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
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
  
  private float error( float dip, float beta, float gamma )
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
    TDLog.Log( TDLog.LOG_CALIB, "Calibrate: data nr. " + idx + " Algo: min" );
    if ( idx < 16 ) return -1; // too few data
    invNN = 1.0f / idx;

    float alpha0 = TDSetting.mAlgoMinAlpha;
    float alpha1 = 1 - alpha0;
    float beta  = TDSetting.mAlgoMinBeta;
    float gamma = TDSetting.mAlgoMinGamma;
    float delta = TDSetting.mAlgoMinDelta;

    Vector zero = new Vector();

    float dip, e0, e1;
    int iter = 0;

    bG = mean_g(); bG.reverse();
    bM = mean_m(); bM.reverse();
    aG = mean_gg().InverseM();
    aM = mean_mm().InverseM();
    
    dip = mean_dip();
    e0 = error( dip, beta, gamma );
    // printf("E %.6f dip %.4f\n", e0, dip );

    do {
      Vector bg0 = bG.times( alpha1 );
      Matrix ag0 = aG.timesF( alpha1 );
      Vector bm0 = bM.times( alpha1 );
      Matrix am0 = aM.timesF( alpha1 );
      e1 = e0;
      {
        float r = 1 / r2g();
        Vector bd1 = dbgGM().times( delta );
        Vector bb1 = gmm( dip ).times( beta );
        Vector bg1 = ( g0().minus( r2ag().plus( bb1 ).plus( bd1 ) ) ).times( r );
        Matrix rho = r2gg().InverseM();
        Matrix dg  = dgxyz();
        Matrix dag = new Matrix();
        dag.x.x = aG.x.dot( dg.x );
        dag.x.y = aG.x.dot( dg.y );
        dag.x.z = aG.x.dot( dg.z );
        Matrix ad1 = dagGM().timesF( delta );
        Matrix ac1 = dag.timesF( gamma );
        Matrix ab1 = gmmg( dip ).timesF( beta );
        Matrix ag1 = ( gg().minus( r2bg().plus( ab1 ).plus( ac1 ).plus( ad1 ) ) ).timesM( rho );
        bG = bg0.plus( bg1.times(alpha0) );
        aG = ag0.plus( ag1.timesF(alpha0) );
      }
      {
        float r = 1 / r2m();
        Vector bd1 = dbmGM().times( delta );
        Vector bb1 = gmg( dip ).times( beta );
        Vector bm1 = ( m0().minus( r2am().plus( bb1 ).plus( bd1 ) ) ).times( r );
        Matrix rho = r2mm().InverseM();
        Matrix dm  = dmxyz();
        Matrix dam = new Matrix();
        dam.x.x = aM.x.dot( dm.x );
        dam.x.y = aM.x.dot( dm.y );
        dam.x.z = aM.x.dot( dm.z );
        Matrix ad1 = damGM().timesF( delta );
        Matrix ac1 = dam.timesF( gamma );
        Matrix ab1 = gmgm( dip ).timesF( beta );
        Matrix am1 = ( mm().minus( r2bm().plus( ab1 ).plus( ac1 ).plus( ad1 ) ) ).timesM( rho );
        bM = bm0.plus( bm1.times(alpha0) );
        aM = am0.plus( am1.timesF(alpha0) );
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
            vx.plusEqual( new Vector( b0, c0 ) ); 
            ++cx;
          }
          ++ i;
        }
        if ( cx > 0 ) vx.timesEqual( 1.0f/cx );
        // Log.v("DistoX", "group V " + vx.x + " " + vx.y + " " + vx.z );
        for (int j=first; j<i; ++j ) {
          if ( group[j] == 0 ) {
            err[j] = 0.0f;
          } else {
            computeBearingAndClinoRad( gr[j], mr[j] );
            Vector v = new Vector( b0, c0 );
            err[j] = vx.minus(v).Length(); // approx angle with 2*tan(alpha/2)
            // Log.v("DistoX", "Err " + err[j] + " V " + vx.x + " " + vx.y + " " + vx.z );
            mDelta  += err[j];
            mDelta2 += err[j] * err[j];
            if ( err[j] > mMaxError ) mMaxError = err[j];
            ++ cnt;
          }
        }
      }
    }
    if ( cnt > 0 ) {
      mDelta  = mDelta / cnt;
      mDelta2 = (float)Math.sqrt(mDelta2/cnt - mDelta*mDelta);
    }
    mDelta    *= TDMath.RAD2DEG; // convert avg and std0-dev from radians to degrees
    mDelta2   *= TDMath.RAD2DEG;
    mMaxError *= TDMath.RAD2DEG;
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
      vx.plusEqual( new Vector( b0, c0 ) );
    }
    vx.timesEqual( 1.0f/size );
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
  
