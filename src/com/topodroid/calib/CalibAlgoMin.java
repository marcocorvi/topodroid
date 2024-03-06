/* @file CalibAlgoMin.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration algorithm Error minimization
 *
 * compiled against v. 6.2.14 but not tested
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.math.TDVector;
import com.topodroid.math.TDMatrix;
import com.topodroid.prefs.TDSetting;

import java.lang.Math;

// import java.util.Locale;

// used by logCoeff
// import android.util.Log;

// upper case denotes variables components in device frame
// lower case denotes variables components in sensor frame

class CalibAlgoMin extends CalibAlgo
{
  // private boolean mNonLinear;
  // private TDVector nL;

  private TDVector gxp; // opt vectors
  private TDVector mxp;
  private TDVector gxt; // turn vectors
  private TDVector mxt;

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
  // public TDVector GetNL() { return nL; }
  // public int nrCoeff() { return mNonLinear ? 52 : 48; }


  // /** @return clino + 90 degrees
  //  * @param gs   g (sensor)
  //  * @param ms   m (sensor)
  //  */
  // float clino( TDVector gs, TDVector ms )
  // {
  //   TDVector g = bG.plus( aG.timesV( gs ) );
  //   g.normalize();
  //   return (float)Math.acos( g.x ) * TDMath.RAD2DEG;
  // }

  // /** @return azimuth if in [0,180], and 360-azimuth if in [180,360]
  //  * @param gs   g (sensor)
  //  * @param ms   m (sensor)
  //  */
  // float azimuth( TDVector gs, TDVector ms )
  // {
  //   TDVector g = bG.plus( aG.timesV( gs ) );
  //   g.normalize();
  //   float gx = g.x;
  //
  //   TDVector m = bM.plus( aM.timesV( ms ) );
  //   m.normalize();
  //   float gm = g.dot( m );
  // 
  //   return (float)Math.acos( (gx*gm - m.x)/Math.sqrt((1-gx*gx)*(1-gm*gm)) ) * TDMath.RAD2DEG;
  //
  //   // TDVector e = g ^ m;
  //   // TDVector n = e ^ g;
  //   // n.normalize();
  //   // TDVector x(1,0,0);
  //   // TDVector xh = x - g * g.x;
  //   // xh.normalize();
  //   // return acos( xh * n ) * 180 / M_PI;
  // }

  /** @return calibrated G (device frame)
   * @param n  data index
   */
  private TDVector G( int n ) { return bG.plus( aG.timesV( g[n] ) ); }

  /** @return calibrated M (device frame)
   * @param n  data index
   */
  private TDVector M( int n ) { return bM.plus( aM.timesV( m[n] ) ); }

  /** @return mean dip (using calibrated G, M) [radians]
   * @note this is the complementary to the M dip-angle
   */
  private float mean_dip()
  {
    float sum = 0;
    for (int i=0; i<idx; ++i ) sum += G(i).dot( M(i) );
    return sum / idx;
  }

  /** @return mean g (sensor)
   */
  private TDVector mean_g()
  {
    TDVector sum = new TDVector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( g[i] );
    sum.timesEqual( invNN ); // invNN = 1/idx
    return sum;
  }
  
  /** @return mean m (sensor)
   */
  private TDVector mean_m()
  {
    TDVector sum = new TDVector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( m[i] );
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** return mean (g x g) matrix (sensor)
   * @note the m[h,k] = Sum_(i,j) g{i]_h g[j]_k where h,k are x,y,z  
   */
  private TDMatrix mean_gg()
  {
    TDMatrix sum = new TDMatrix();
    for (int i=0; i<idx; ++i ) sum.plusEqual( new TDMatrix( g[i], g[i] ) );
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** return mean (m x m) matrix (sensor)
   */
  private TDMatrix mean_mm()
  {
    TDMatrix sum = new TDMatrix();
    for (int i=0; i<idx; ++i ) sum.plusEqual( new TDMatrix( m[i], m[i] ) );
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** @return average sum of squared X-differences between calibrated G(i) and calibrated group G
   */
  private float dGx2()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float x1 = G(i/*+0*/).x;
      float x2 = G(i+1).x;
      float x3 = G(i+2).x;
      float x4 = G(i+3).x;
      float x = ( x1 + x2 + x3 + x4 )/4;
      sum += (x-x1)*(x-x1) + (x-x2)*(x-x2) + (x-x3)*(x-x3) + (x-x4)*(x-x4);
    }
    return sum / idx; // FIXME this should be multiplied by 4
  }
    
  /** @return average sum of squared X-differences between calibrated M(i) and calibrated group M
   */
  private float dMx2()
  {
    float sum = 0;
    for (int i=0; i<idx; i+=4 ) {
      float x1 = M(i/*+0*/).x;
      float x2 = M(i+1).x;
      float x3 = M(i+2).x;
      float x4 = M(i+3).x;
      float x = ( x1 + x2 + x3 + x4 )/4;
      sum += (x-x1)*(x-x1) + (x-x2)*(x-x2) + (x-x3)*(x-x3) + (x-x4)*(x-x4);
    }
    return sum / idx; // FIXME this should be multiplied by 4 (alternatively gamma can be set 4 times bigger)
  }
  
  /** @return mean matrix of differences between group-average (sensor) g and (sensor) g 
   * ie, 
   *      (<g>x - g_x)*(<g>x - g_x)     (<g>x - g_x)*(<g>y - g_y)    (<g>x - g_x)*(<g>z - g_z)
   *      (<g>x - g_x)*(<g>y - g_y)     (<g>y - g_y)*(<g>y - g_y)    ...
   *      (<g>x - g_x)*(<g>z - g_z)     ...
   */
  private TDMatrix dgxyz()
  {
    TDMatrix sum = new TDMatrix();
    int csum = 0;
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        TDVector vv = new TDVector(); // group average (sensor) g
        while ( i < idx && (group[i] <= 0 || group[i] == group0) ) {
          if ( group[i] > 0 ) {
            vv.plusEqual( g[i] );
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          vv.timesEqual( 1/(float)c );
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              TDVector v = g[j];
              sum.x.x += (vv.x - v.x) * (vv.x - v.x);
              sum.x.y += (vv.x - v.x) * (vv.y - v.y);
              sum.x.z += (vv.x - v.x) * (vv.z - v.z);
              sum.y.y += (vv.y - v.y) * (vv.y - v.y);
              sum.y.z += (vv.y - v.y) * (vv.z - v.z);
              sum.z.z += (vv.z - v.z) * (vv.z - v.z);
              ++ csum;
            }
          }
        }
      }
    }
    if ( csum > 1 ) {
      sum.timesEqual( 1.0f/csum ); // ( invNN );
    }
    sum.y.x = sum.x.y;
    sum.z.x = sum.x.z;
    sum.z.y = sum.y.z;
    return sum;
  }

  /** @return mean matrix of differences between group-average (sensor) m and (sensor) m 
   */
  private TDMatrix dmxyz()
  {
    TDMatrix sum = new TDMatrix();
    int csum = 0;
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        TDVector vv = new TDVector();
        while ( i < idx && (group[i] <= 0 || group[i] == group0) ) {
          if ( group[i] > 0 ) {
            vv.plusEqual( m[i] );
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          vv.timesEqual( 1/(float)c );
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              TDVector v = m[j];
              sum.x.x += (vv.x - v.x) * (vv.x - v.x);
              sum.x.y += (vv.x - v.x) * (vv.y - v.y);
              sum.x.z += (vv.x - v.x) * (vv.z - v.z);
              sum.y.y += (vv.y - v.y) * (vv.y - v.y);
              sum.y.z += (vv.y - v.y) * (vv.z - v.z);
              sum.z.z += (vv.z - v.z) * (vv.z - v.z);
              ++ csum;
            }
          }
        }
      }
    }
    if ( csum > 1 ) {
      sum.timesEqual( 1.0f/csum ); // ( invNN );
    }
    sum.y.x = sum.x.y;
    sum.z.x = sum.x.z;
    sum.z.y = sum.y.z;
    return sum;
  }

  /** vector sum of the vector differences of the average M^X of the group with the M^X in the group
   *                    weighted by the differences of the average (G^M)x and (G^M)x
   */
  private TDVector dbGGM()
  {
    TDVector sum = new TDVector();
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        TDVector XX = new TDVector();
        float GMx = 0;
        while ( i < idx && (group[i] <= 0 || group[i] == group0) ) {
          if ( group[i] > 0 ) {
            TDVector MM = M(i);
            GMx += cross_x( G(i), MM );     // average [ G(i) ^ M(i) ]_x
            XX.plusEqual( MM.crossX() );    // average vector M(i) ^ X   maybe
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          XX.timesEqual( 1/(float)c );
          GMx /= c;
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              TDVector MM = M(j);
              sum.plusEqual( ( XX.minus( MM.crossX() ) ).times( GMx - cross_x( G(j), MM ) ) );
            }
          }
        }
      }
    }
    sum.timesEqual( invNN );
    return sum;
  }

  /** matrix sum of the matrix differences of the average [G % M^X] of the group
   *                    with the [G % M^X] in the group
   *                    weighted by the differences of the average (G^M)x and (G^M)x
   */
  private TDMatrix daGGM()
  {
    TDMatrix sum = new TDMatrix();
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        TDMatrix XX = new TDMatrix();
        float GMx = 0;
        while ( i < idx && (group[i] <= 0 || group[i] == group0) ) {
          if ( group[i] > 0 ) {
            TDVector GG = G(i);
            TDVector MM = M(i);
            GMx += cross_x( GG, MM );                       // average (G ^ M)x
            XX.plusEqual( new TDMatrix( GG, MM.crossX() ) );  // average G ^ ( M ^ X )
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          XX.timesEqual( 1/(float)c );
          GMx /= c;
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              TDVector GG = G(j);
              TDVector MM = M(j);
              TDMatrix ZZ = XX.minus( new TDMatrix( GG, MM.crossX() ) ); // average minus G ^ ( M ^ X ) 
              ZZ.timesEqual( GMx - cross_x( GG, MM ) );
              sum.plusEqual( ZZ );
            }
          }
        }
      }
    }
    sum.timesEqual( invNN );
    return sum;
  }

  /** vector sum of the vector differences of the average G^X of the group with the G^X in the group
   *                    weighted by the differences of the average (G^M)x and (G^M)x
   */
  private TDVector dbMGM()
  {
    TDVector sum = new TDVector();
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        TDVector XX = new TDVector();
        float GMx = 0;
        while ( i < idx && (group[i] <= 0 || group[i] == group0) ) {
          if ( group[i] > 0 ) {
            TDVector GG = G(i);
            GMx += cross_x( M(i), GG );
            XX.plusEqual( GG.crossX() );
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          XX.timesEqual( 1/(float)c );
          GMx /= c;
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              TDVector GG = G(j);
              sum.plusEqual( ( XX.minus( GG.crossX() ) ).times( GMx - cross_x( M(j), GG ) ) );
            }
          }
        }
      }
    }
    sum.timesEqual( invNN );
    return sum;
  }

  /** matrix sum of the matrix differences of the average [M % G^X] of the group
   *                    with the [M % G^X] in the group
   *                    weighted by the differences of the average (G^M)x and (G^M)x
   */
  private TDMatrix daMGM()
  {
    TDMatrix sum = new TDMatrix();
    long group0 = -1;
    for ( int i=0; i<idx; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        int c = 0;
        TDMatrix XX = new TDMatrix();
        float GMx = 0;
        while ( i < idx && (group[i] <= 0 || group[i] == group0) ) {
          if ( group[i] > 0 ) {
            TDVector GG = G(i);
            TDVector MM = M(i);
            GMx += cross_x( MM, GG );
            XX.plusEqual( new TDMatrix( MM, GG.crossX() ) );
            ++c;
          }
          ++ i;
        }
        if ( c > 0 ) {
          XX.timesEqual( 1/(float)c );
          GMx /= c;
          for (int j=first; j<i; ++j ) {
            if ( group[j] > 0 ) {
              TDVector GG = G(j);
              TDVector MM = M(j);
              TDMatrix ZZ = XX.minus( new TDMatrix( MM, GG.crossX() ) );
              ZZ.timesEqual( GMx - cross_x( MM, GG ) );
              sum.plusEqual( ZZ );
            }
          }
        }
      }
    }
    sum.timesEqual( invNN );
    return sum;
  }

  // ========================================================
  
  /** @return average G(i)^2
   */
  private float r2G()
  {
    float sum = 0;
    for (int i=0; i<idx; ++i ) sum += G(i).lengthSquared();
    return sum / idx;
  }
  
  /** @return average M(i)^2
   */
  private float r2M() // < (Bm+Am*ms) * (Bm+Am*ms) >
  {
    float sum = 0;
    for (int i=0; i<idx; ++i ) sum += M(i).lengthSquared();
    return sum / idx;
  }
  
  /** @return average matrix  |G(i)|^2 [ g(i) X g(i) ]
   */
  private TDMatrix r2gg()
  {
    TDMatrix sum = new TDMatrix();
    for (int i=0; i<idx; ++i ) {
      TDMatrix mu = new TDMatrix(g[i], g[i]);
      mu.timesEqual( G(i).lengthSquared() );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** @return average matrix  |M(i)|^2 [ m(i) X m(i) ]
   */
  private TDMatrix r2mm()
  {
    TDMatrix sum = new TDMatrix();
    for (int i=0; i<idx; ++i ) {
      TDMatrix mu = new TDMatrix(m[i], m[i]);
      mu.timesEqual( M(i).lengthSquared() );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** @return average G(i)
   */
  private TDVector G0() // <Bg + Ag * gs>
  {
    TDVector sum = new TDVector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( G(i) );
    sum.timesEqual( invNN );
    return sum;
  }
    
  /** @return average M(i)
   */
  private TDVector M0() // <Bm + Am * ms>
  {
    TDVector sum = new TDVector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( M(i) );
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** @return average matrix G(i) X g(i)
   */
  private TDMatrix Gg()
  {
    TDMatrix sum = new TDMatrix();
    for (int i=0; i<idx; ++i ) sum.plusEqual( new TDMatrix( G(i), g[i] ) );
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** @return average matrix M(i) X m(i)
   */
  private TDMatrix Mm()
  {
    TDMatrix sum = new TDMatrix();
    for (int i=0; i<idx; ++i ) sum.plusEqual( new TDMatrix( M(i), m[i] ) );
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** @return average |G(i)|^2 g(i)
   */
  private TDVector r2ag()
  {
    TDVector sum = new TDVector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( g[i].times( G(i).lengthSquared() ) );
    sum.timesEqual( invNN );
    return aG.timesV( sum );
  }
    
  /** @return average |M(i)|^2 m(i)
   */
  private TDVector r2am()
  {
    TDVector sum = new TDVector();
    for (int i=0; i<idx; ++i ) sum.plusEqual( m[i].times( M(i).lengthSquared() ) );
    sum.timesEqual( invNN );
    return aM.timesV( sum );
  }
  
  /** @return average (G(i) * M(i) - dip ) M(i) 
   */
  private TDVector GMM( float d ) 
  {
    TDVector sum = new TDVector();
    for (int i=0; i<idx; ++i ) {
      TDVector v = M(i);
      sum.plusEqual( v.times( G(i).dot(v) - d) );
    }
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** @return average (G(i) * M(i) - dip ) G(i)
   */
  private TDVector GMG( float d ) 
  {
    TDVector sum = new TDVector();
    for (int i=0; i<idx; ++i ) {
      TDVector v = G(i);
      sum.plusEqual( v.times( M(i).dot(v) - d ) );
    }
    sum.timesEqual( invNN );
    return sum;
  }
   
  /** @return average matrix  |G(i)|^2 Bg X g(i)
   */
  private TDMatrix r2bg()
  {
    TDMatrix sum = new TDMatrix(); 
    for (int i=0; i<idx; ++i ) {
      TDMatrix mu = new TDMatrix( bG, g[i]);
      mu.timesEqual( G(i).lengthSquared() );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
    return sum;
  }
      
  /** @return average matrix  |M(i)|^2 Bm X m(i)
   */
  private TDMatrix r2bm()
  {
    TDMatrix sum = new TDMatrix();
    for (int i=0; i<idx; ++i ) {
      TDMatrix mu = new TDMatrix( bM, m[i]);
      mu.timesEqual( M(i).lengthSquared() );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** @return average matrix (G(i) * M(i) - dip ) M(i) X g(i)
   */
  private TDMatrix GMMg( float d )
  {
    TDMatrix sum = new TDMatrix();
    for (int i=0; i<idx; ++i ) {
      TDVector v = M(i);
      TDMatrix mu = new TDMatrix(v, g[i]);
      mu.timesEqual( G(i).dot(v) - d );
      sum.plusEqual( mu );
    }
    sum.timesEqual( invNN );
    return sum;
  }
  
  /** @return average matrix (G(i) * M(i) - dip ) G(i) X m(i)
   */
  private TDMatrix GMGm( float d )
  {
    TDMatrix sum = new TDMatrix();
    for (int i=0; i<idx; ++i ) {
      TDVector v = G(i);
      TDMatrix mu = new TDMatrix(v, m[i]);
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
    float r2 = G(i).lengthSquared() - 1;
    sum += r2 * r2;
    r2 = M(i).lengthSquared() - 1;
    sum += r2 * r2;
    r2 = G(i).dot( M(i) ) - dip;
    sum += beta * r2 * r2;
    return sum / idx + gamma * ( dGx2() + dMx2() );
  }
  
  private float error( float dip, float beta, float gamma )
  {
    float sum = 0;
    for ( int i=0; i<idx; ++i ) {
      float r2 = G(i).lengthSquared() - 1;
      sum += r2 * r2;
      r2 = M(i).lengthSquared() - 1;
      sum += r2 * r2;
      r2 = G(i).dot( M(i) ) - dip;
      sum += beta * r2 * r2;
    }
    return sum / idx + gamma * ( dGx2() + dMx2() );
  }

  public int Calibrate()
  {
    mDelta = 0.0f;
    TDLog.v( "MIN_ALGO calibrate: data nr. " + idx );
    if ( idx < 16 ) return -1; // too few data
    invNN = 1.0f / idx;

    float alpha0 = TDSetting.mAlgoMinAlpha;
    float alpha1 = 1 - alpha0;
    float beta  = TDSetting.mAlgoMinBeta;
    float gamma = TDSetting.mAlgoMinGamma;
    float delta = TDSetting.mAlgoMinDelta;

    TDVector zero = new TDVector();

    float dip, e0, e1;
    int iter = 0;

    bG = mean_g(); bG.reverse();
    bM = mean_m(); bM.reverse();
    aG = mean_gg().inverseMatrix();
    aM = mean_mm().inverseMatrix();
    
    dip = mean_dip();
    e0 = error( dip, beta, gamma );
    // printf("E %.6f dip %.4f\n", e0, dip );

    do {
      TDVector bg0 = bG.times( alpha1 );
      TDMatrix ag0 = aG.timesF( alpha1 );
      TDVector bm0 = bM.times( alpha1 );
      TDMatrix am0 = aM.timesF( alpha1 );
      e1 = e0;
      {
        float r = 1 / r2G();
        TDVector bd1 = dbGGM().times( delta );
        TDVector bb1 = GMM( dip ).times( beta );
        TDVector bg1 = ( G0().minus( r2ag().plus( bb1 ).plus( bd1 ) ) ).times( r );
        TDMatrix rho = r2gg().inverseMatrix();
        TDMatrix dg  = dgxyz();
        TDMatrix dag = new TDMatrix();
        dag.x.x = aG.x.dot( dg.x );
        dag.x.y = aG.x.dot( dg.y );
        dag.x.z = aG.x.dot( dg.z );
        TDMatrix ad1 = daGGM().timesF( delta );
        TDMatrix ac1 = dag.timesF( gamma );
        TDMatrix ab1 = GMMg( dip ).timesF( beta );
        TDMatrix ag1 = ( Gg().minus( r2bg().plus( ab1 ).plus( ac1 ).plus( ad1 ) ) ).timesM( rho );
        bG = bg0.plus( bg1.times(alpha0) );
        aG = ag0.plus( ag1.timesF(alpha0) );
      }
      {
        float r = 1 / r2M();
        TDVector bd1 = dbMGM().times( delta );
        TDVector bb1 = GMG( dip ).times( beta );
        TDVector bm1 = ( M0().minus( r2am().plus( bb1 ).plus( bd1 ) ) ).times( r );
        TDMatrix rho = r2mm().inverseMatrix();
        TDMatrix dm  = dmxyz();
        TDMatrix dam = new TDMatrix();
        dam.x.x = aM.x.dot( dm.x );
        dam.x.y = aM.x.dot( dm.y );
        dam.x.z = aM.x.dot( dm.z );
        TDMatrix ad1 = daMGM().timesF( delta );
        TDMatrix ac1 = dam.timesF( gamma );
        TDMatrix ab1 = GMGm( dip ).timesF( beta );
        TDMatrix am1 = ( Mm().minus( r2bm().plus( ab1 ).plus( ac1 ).plus( ad1 ) ) ).timesM( rho );
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
    TDVector[] gr = new TDVector[nn];
    TDVector[] mr = new TDVector[nn];
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
    TDVector vx;
    for ( int i=0; i<nn; ) {
      if ( group[i] <= 0 ) {
        ++i;
      } else if ( group[i] != group0 ) {
        group0 = group[i];
        int first = i;
        vx = new TDVector();
        int cx = 0;
        while ( i < nn && (group[i] <= 0 || group[i] == group0) ) {
          if ( group[i] > 0 ) {
            computeBearingAndClinoRad( gr[i], mr[i] );
            vx.plusEqual( new TDVector( b0, c0 ) ); 
            ++cx;
          }
          ++ i;
        }
        if ( cx > 0 ) vx.timesEqual( 1.0f/cx );
        // Log.v("DistoX", "group V " + vx.x + " " + vx.y + " " + vx.z );
        for (int j=first; j<i; ++j ) {
          if ( group[j] <= 0 ) {
            err[j] = 0.0f;
          } else {
            computeBearingAndClinoRad( gr[j], mr[j] );
            TDVector v = new TDVector( b0, c0 );
            err[j] = vx.minus(v).length(); // approx angle with 2*tan(alpha/2)
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
  public void addStatErrors( TDVector[] g1, TDVector[] m1, float[] errors )
  {
    int size = g1.length;
    TDVector[] g2 = new TDVector[ size ];
    TDVector[] m2 = new TDVector[ size ];
    for ( int k=0; k<size; ++k ) {
      g2[k] = scaledVector( g1[k] );
      m2[k] = scaledVector( m1[k] );
    }
    // Log.v("DistoX", "add stat errors: size " + size );

    TDVector[] gr = new TDVector[size];
    TDVector[] mr = new TDVector[size];
    TDVector vx = new TDVector();
    for ( int i=0; i<size; ++i ) {
      gr[i] = bG.plus( aG.timesV(g2[i]) );
      mr[i] = bM.plus( aM.timesV(m2[i]) );
      computeBearingAndClinoRad( gr[i], mr[i] );
      vx.plusEqual( new TDVector( b0, c0 ) );
    }
    vx.timesEqual( 1.0f/size );
    float err = 0.0f;
    for ( int i=0; i<size; ++i ) {
      computeBearingAndClinoRad( gr[i], mr[i] );
      TDVector v1 = new TDVector( b0, c0 );
      float e = v1.minus(vx).length();
      if ( errors != null ) errors[i] = e;
      // Log.v("DistoX", e + " " + g[i].x + " " + g[i].y + " " + g[i].z );
      mSumCount += 1;
      mSumErrors += e;
      mSumErrorSquared += e*e;
    }
  }

  /** @return X component of cross product of two vectors
   * @param g   first vector
   * @param m   second vector
   */
  private float cross_x( TDVector g, TDVector m ) 
  {
    return g.y * m.z - g.z * m.y;
  }
}
  
