/* @file CalibCoverage.java
 *
 * @author marco corvi
 * @date may 2019
 *
 * @brief TopoDroid calibration data distribution evaluation
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.utils.TDMath;
import com.topodroid.math.TDVector;
import com.topodroid.DistoX.R;
import com.topodroid.DistoX.TDUtil;

import android.util.Log;

import java.util.List;

public class CalibCoverage
{
  class Direction
  {
    final float mCompass;
    final float mClino;
    float mValue;

    Direction( float cm, float cl, float v )
    {
      mCompass = cm;
      mClino = cl;
      mValue = v;
    }
  }

  static final int DIM_Y       =  37;
  static final int DIM_Y2      =  18; // (DIM_Y-1)/2
  static final int DELTA_Y     =   5;
  static final int AZIMUTH_BIT =  32;

  private final int[] clino_angles;
  private final int[] t_size;
  private final int[] t_offset;
  private int t_dim;

  private Direction[] angles;
  private float mCoverage;

  public CalibCoverage( )
  {
    clino_angles = new int[ DIM_Y ];
    t_size       = new int[ DIM_Y ];
    t_offset     = new int[ DIM_Y ];
    setup();
    // mCoverage = evalCoverage( list,  null );
    mCoverage = 0; // do not compute immediately
  }

  float getCoverage() { return mCoverage; }
  Direction[] getDirections() { return angles; }
  int[] getTSize() { return t_size; }
  int[] getTOffset() { return t_offset; }

  private void setup()
  {
    int i;
    for ( i=0; i<DIM_Y; ++i ) { // clino angles: from +90 to -90
      clino_angles[i] = 90 - DELTA_Y*i;
    }
    t_size[ 0 ] = t_size[DIM_Y-1] = 1;
    for ( i=1; i<DIM_Y2; ++i ) {
      t_size[i] = t_size[DIM_Y-1-i] = AZIMUTH_BIT * i;
    }
    t_size[ DIM_Y2 ] = AZIMUTH_BIT * DIM_Y2; // max azimuth steps 54 at clino 0

    t_offset[0] = 0;
    for ( i=1; i<DIM_Y; ++i ) {
      t_offset[i] = t_offset[i-1] + t_size[i-1];
      // Log.v("DistoX", "J " + i + " off " + t_offset[i] + " size " + t_size[i] );
    }
    t_dim = t_offset[DIM_Y-1] + t_size[DIM_Y-1];
    // Log.v("DistoX", "dim " + t_dim );

    angles = new Direction [ t_dim ];
    for (int k = 0; k<DIM_Y; ++k ){
      float clino = clino_angles[k] * TDMath.DEG2RAD;
      for (int j=t_offset[k]; j<t_offset[k]+t_size[k]; ++j ) {
        angles[j] = new Direction(
                      TDMath.M_PI + ( TDMath.M_2PI * (j - t_offset[k]) ) / t_size[k],
                      clino, 
                      1.0f );
      }
    }
  }

  // compass and clino in radians
  private float cosine( float compass1, float clino1, float compass2, float clino2 )
  {
    double h1 = Math.cos( clino1 );
    double z1 = Math.sin( clino1 );
    double x1 = h1 * Math.cos( compass1 );
    double y1 = h1 * Math.sin( compass1 );
    double h2 = Math.cos( clino2 );
    double z2 = Math.sin( clino2 );
    double x2 = h2 * Math.cos( compass2 );
    double y2 = h2 * Math.sin( compass2 );
    return (float)(x1*x2 + y1*y2 + z1*z2); // cosine of the angle
  }

  private void updateDirections( float compass, float clino, int cnt )
  {
    for (int j=0; j<t_dim; ++j ) {
      float c = cosine( compass, clino, angles[j].mCompass, angles[j].mClino );
      if ( c > 0.0 ) {
        c = c * c;
        angles[j].mValue -= (cnt >= 4)? c*c : c*c * cnt * 0.25f;
        if ( angles[j].mValue < 0.0f ) angles[j].mValue = 0.0f;
      }
    }
  }

  // @param clist    list of calib shots
  // @pre shots bearing and clino must have been precomputed
  // @return array distribution of between shots errors
  // static float[] evalDeviations( List< CBlock > clist )
  // {
  //   float[] error = new float[ 181 ]; // angle deviations among calib shots of a group
  //   for ( int k = 0; k < 181; ++ k ) error[k] = 0;
  //   int sz = clist.size();
  //   int cnt = 0;
  //   for ( int i1 = 0; i1 < sz; ++ i1 ) {
  //     CBlock b1 = clist.get( i1 );
  //     if ( b1.mGroup == 0 ) continue;
  //     float compass = b1.mBearing * TDMath.DEG2RAD;
  //     float clino   = b1.mClino   * TDMath.DEG2RAD;
  //     float h1 = TDMath.cos( clino );
  //     float z1 = TDMath.sin( clino );
  //     float n1 = h1 * TDMath.cos( compass );
  //     float e1 = h1 * TDMath.sin( compass );
  //     for ( int i2 = i1+1; i2 < sz; ++i2 ) {
  //       CBlock b2 = clist.get( i2 );
  //       if ( b2.mGroup == 0 ) continue;
  //       if ( b2.mGroup != b1.mGroup ) break;
  //       // compute difference
  //       compass = b2.mBearing * TDMath.DEG2RAD;
  //       clino   = b2.mClino   * TDMath.DEG2RAD;
  //       h1 = TDMath.cos( clino );
  //       int a = (int)(TDMath.acosd( z1 * TDMath.sin( clino ) + n1 * h1 * TDMath.cos( compass ) + e1 * h1 * TDMath.sin( compass ) ) );
  //       if ( a > 180 ) a = 180;
  //       ++ error[a];
  //       ++ cnt;
  //     }
  //   }
  //   Log.v("DistoX-COVER", "size " + sz + " count " + cnt );
  //   return error;
  // }

  // @param clist    list of calib shots
  // @param thr      deviation threshold
  // @return number of shots above threshold
  // static int evalShotDeviations( List< CBlock > clist, float thr )
  // {
  //   int sz = clist.size();
  //   int ret = 0;
  //   long old_grp = 0;
  //   int i0 = 0;
  //   for ( int i1 = 0; i1 < sz; ++ i1 ) {
  //     CBlock b1 = clist.get( i1 );
  //     if ( b1.mGroup == 0 ) continue;
  //     if ( old_grp != b1.mGroup ) {
  //       i0 = i1;
  //       old_grp = b1.mGroup;
  //     }
  //     float compass = b1.mBearing * TDMath.DEG2RAD;
  //     float clino   = b1.mClino   * TDMath.DEG2RAD;
  //     float h1 = TDMath.cos( clino );
  //     float z1 = TDMath.sin( clino );
  //     float n1 = h1 * TDMath.cos( compass );
  //     float e1 = h1 * TDMath.sin( compass );
  //     int cnt = 0;
  //     float dev = 0;
  //     for ( int i2 = i0; i2 < sz; ++i2 ) {
  //       if ( i2 == i1 ) continue;
  //       CBlock b2 = clist.get( i2 );
  //       if ( b2.mGroup == 0 ) continue;
  //       if ( b2.mGroup != b1.mGroup ) break;
  //       // compute difference
  //       compass = b2.mBearing * TDMath.DEG2RAD;
  //       clino   = b2.mClino   * TDMath.DEG2RAD;
  //       h1 = TDMath.cos( clino );
  //       dev += TDMath.acosd( z1 * TDMath.sin( clino ) + n1 * h1 * TDMath.cos( compass ) + e1 * h1 * TDMath.sin( compass ) );
  //       ++ cnt;
  //     }
  //     b1.setOffGroup( ( cnt > 0 && dev/cnt > thr ) );
  //     if ( b1.isOffGroup() ) ++ ret;
  //   }
  //   Log.v("DistoX-COVER", "off-group " + ret );
  //   return ret;
  // }

  public float evalCoverage( List< CBlock > clist, CalibAlgo transform )
  {

    for (int j=0; j<t_dim; ++j ) angles[j].mValue = 1.0f;

    long old_grp = 0;
    float compass_avg = 0.0f;
    float clino_avg   = 0.0f;
    int cnt_avg = 0;
    for ( CBlock b : clist ) {
      if ( b.mGroup == 0 ) continue;
      if ( transform == null ) {
        b.computeBearingAndClino( );
      } else {
        b.computeBearingAndClino( transform );
      }
      float compass = b.mBearing * TDMath.DEG2RAD;
      float clino   = b.mClino   * TDMath.DEG2RAD;
      if ( b.mGroup == old_grp ) { // calib shot adds to the group
        if ( cnt_avg > 0 && Math.abs( compass - compass_avg / cnt_avg ) > 1.5f * TDMath.M_PI ) {
          if ( compass > TDMath.M_PI ) {
            compass -= TDMath.M_2PI; // average around 0
          } else {
            compass += TDMath.M_2PI; // average around 360
          }
        }
        clino_avg   += clino;
        compass_avg += compass;
        cnt_avg     ++;
      } else { // new group: update directions
        if ( cnt_avg > 0 ) {
          compass_avg /= cnt_avg;
          clino_avg   /= cnt_avg;
          updateDirections( compass_avg, clino_avg, cnt_avg );
        }
        clino_avg   = clino;
        compass_avg = compass;
        cnt_avg     = 1;
        old_grp     = b.mGroup;
      }
    }
    if ( cnt_avg > 0 ) {
      compass_avg /= cnt_avg;
      clino_avg   /= cnt_avg;
      updateDirections( compass_avg, clino_avg, cnt_avg );
    }

    mCoverage = 0.0f;
    for (int j=0; j<t_dim; ++j ) {
      mCoverage += angles[j].mValue;
    }
    mCoverage = 100.0f * ( 1.0f - mCoverage/t_dim );
    return mCoverage;
  }

  // @param clist  list of CBlocks
  // @param mode   0: G,  1: M
  public float evalCoverageGM( List< CBlock > clist, int mode ) 
  {
    for (int j=0; j<t_dim; ++j ) angles[j].mValue = 1.0f;

    long old_grp = 0;
    float compass_avg = 0.0f;
    float clino_avg   = 0.0f;
    int cnt_avg = 0;

    float f = TDUtil.FV;
    for ( CBlock b : clist ) {
      if ( b.mGroup <= 0 ) continue;
      TDVector v = ( mode == 0 )? new TDVector( b.gx/f, b.gy/f, b.gz/f ) : new TDVector( b.mx/f, b.my/f, b.mz/f );
      float compass = TDMath.atan2( v.x, v.y ); if ( compass < 0 ) compass += TDMath.M_2PI;
      float clino   = TDMath.atan2( v.z, TDMath.sqrt( v.x * v.x + v.y * v.y ) );
      updateDirections( compass, clino, 1 );
    }
    mCoverage = 0.0f;
    for (int j=0; j<t_dim; ++j ) {
      mCoverage += angles[j].mValue;
    }
    mCoverage = 100.0f * ( 1.0f - mCoverage/t_dim );
    return mCoverage;
  }

}
