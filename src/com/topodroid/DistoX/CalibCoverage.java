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
package com.topodroid.DistoX;

import java.util.List;

// import android.util.Log;

class CalibCoverage
{
  class Direction
  {
    float mCompass;
    float mClino;
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

  private int[] clino_angles;
  private int[] t_size;
  private int[] t_offset;
  private int t_dim;

  private Direction[] angles;
  private float mCoverage;

  CalibCoverage( List< CalibCBlock > list )
  {
    clino_angles = new int[ DIM_Y ];
    t_size       = new int[ DIM_Y ];
    t_offset     = new int[ DIM_Y ];
    setup();
    mCoverage = evalCoverage( list,  null );
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

  float evalCoverage( List<CalibCBlock> clist, CalibAlgo transform )
  {
    for (int j=0; j<t_dim; ++j ) angles[j].mValue = 1.0f;

    long old_grp = 0;
    float compass_avg = 0.0f;
    float clino_avg   = 0.0f;
    int cnt_avg = 0;
    for ( CalibCBlock b : clist ) {
      if ( b.mGroup == 0 ) continue;
      if ( transform == null ) {
        b.computeBearingAndClino( );
      } else {
        b.computeBearingAndClino( transform );
      }
      float compass = b.mBearing * TDMath.DEG2RAD;
      float clino   = b.mClino   * TDMath.DEG2RAD;
      if ( b.mGroup == old_grp ) {
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
      } else {
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
  float evalCoverageGM( List<CalibCBlock> clist, int mode ) 
  {
    for (int j=0; j<t_dim; ++j ) angles[j].mValue = 1.0f;

    long old_grp = 0;
    float compass_avg = 0.0f;
    float clino_avg   = 0.0f;
    int cnt_avg = 0;

    float f = TDUtil.FV;
    for ( CalibCBlock b : clist ) {
      if ( b.mGroup <= 0 ) continue;
      Vector v = ( mode == 0 )? new Vector( b.gx/f, b.gy/f, b.gz/f ) : new Vector( b.mx/f, b.my/f, b.mz/f );
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
