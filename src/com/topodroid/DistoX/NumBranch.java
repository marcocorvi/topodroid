/** @file NumBranch.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction branch of shots
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

// import android.util.Log;

public class NumBranch 
{
  static final int BRANCH_UNKNOWN = 0; // branch types
  static final int BRANCH_END_END = 1;
  static final int BRANCH_CROSS_END = 2;
  static final int BRANCH_CROSS_CROSS = 3;
  static final int BRANCH_LOOP = 4;
 
  public int type; // branch type
  public int use;  // tag for loop identification
  public NumNode n1;
  public NumNode n2;
  ArrayList< NumShot > shots;
  float e, s, v; // east, south, vert closure-error
  float len;     // branch length

  // void dump()
  // {
  //   StringBuilder sb = new StringBuilder();
  //   sb.append("Branch ");
  //   for ( NumShot sh : shots ) sb.append( sh.from.name ).append("-").append( sh.to.name ).append(" ");
  //   TDLog.Log( TDLog.LOG_NUM, sb.toString() );
  // }

  NumBranch( int t, NumNode n )
  {
    type = t;
    n1 = n;
    n2 = null;
    shots = new ArrayList<>();
    use = 0;
    e = 0.0f;
    s = 0.0f;
    v = 0.0f;
    len = 0.0f;
  }

  void addShot( NumShot shot )
  {
    shots.add( shot );
    // float d = shot.length();
    // len += d;
  }

  void computeError()
  {
    e = 0.0f;
    s = 0.0f;
    v = 0.0f;
    len = 0.0f;
    for ( NumShot sh : shots ) {
      float d = sh.length();
      float b = sh.bearing(); // degrees
      float c = sh.clino(); // degrees
      len += d;
      // d *= sh.mDirection * sh.mBranchDir; // FIXME DIRCETION
      d *= sh.mBranchDir;
      v -= d * TDMath.sind(c);
      float h0 = d * TDMath.abs( TDMath.cosd(c) );
      s -= h0 * TDMath.cosd(b);
      e += h0 * TDMath.sind(b);
      // TDLog.Log( TDLog.LOG_NUM, "Br sh " + sh.from.name + "-" + sh.to.name + " Br Err " + e + " " + s );
    }
  }

  void compensateError( float e0, float s0, float v0 )
  {
    e0 /= len;
    s0 /= len;
    v0 /= len;
    for ( NumShot sh : shots ) {
      // block displacement vector (absolute, not in the branch)
      float d = sh.length();
      float b = sh.bearing(); // degrees
      float c = sh.clino();
      float v1 = -d * TDMath.sind(c);
      float h1 =  d * TDMath.abs( TDMath.cosd(c) );
      float s1 = -h1 * TDMath.cosd(b);
      float e1 =  h1 * TDMath.sind(b);
      // float l = d * sh.mDirection * sh.mBranchDir; // FIXME DIRECTION
      float l = d * sh.mBranchDir;
      e1 += e0*l;
      s1 += s0*l;
      v1 += v0*l;
     
      h1 = TDMath.sqrt( e1*e1 + s1*s1 );
      b = TDMath.atan2d( e1, -s1 ); // + 90.0f * (1 - sh.mDirection); // FIXME PRE-DIRECTION
      if ( b < 0 ) b += 360;
      c = TDMath.atan2d( -v1, h1 ); // * sh.mDirection; // FIXME PRE-DIRECTION
      d = TDMath.sqrt( h1*h1 + v1*v1 );
      sh.reset( d, b, c );
    }
  }

  void setLastNode( NumNode n ) { n2 = n; }

  NumNode otherNode( NumNode n )
  {
    if ( n == n1 ) return n2;
    if ( n == n2 ) return n1;
    return null;
  }
  
}

