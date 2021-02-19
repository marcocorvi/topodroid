/* @file NumBranch.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction branch of shots
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;


import java.util.ArrayList;

// import android.util.Log;

public class NumBranch
{
  public static final int BRANCH_UNKNOWN = 0; // branch types
  public static final int BRANCH_END_END = 1;
  public static final int BRANCH_CROSS_END = 2;
  public static final int BRANCH_CROSS_CROSS = 3;
  public static final int BRANCH_LOOP = 4;
 
  int type; // branch type
  int use;  // tag for loop identification
  NumNode n1;
  NumNode n2;
  public ArrayList< NumShot > shots;
  public double e, s, v; // east, south, vert closure-error
  public double len;     // branch length

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
    e = 0;
    s = 0;
    v = 0;
    len = 0;
  }

  void addShot( NumShot shot )
  {
    shots.add( shot );
    // double d = shot.length();
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
      v -= d * TDMath.sinDd( c );
      double h0 = d * Math.abs( TDMath.cosDd( c ) );
      s -= h0 * TDMath.cosDd( b );
      e += h0 * TDMath.sinDd( b );
      // TDLog.Log( TDLog.LOG_NUM, "Br sh " + sh.from.name + "-" + sh.to.name + " Br Err " + e + " " + s );
    }
  }

  void compensateError( double e0, double s0, double v0 )
  {
    e0 /= len;
    s0 /= len;
    v0 /= len;
    for ( NumShot sh : shots ) {
      // block displacement vector (absolute, not in the branch)
      float d = sh.length();
      float b = sh.bearing(); // degrees
      float c = sh.clino();
      double v1 =  -d * TDMath.sinDd( c );
      double h1 =   d * Math.abs( TDMath.cosDd( c ) );
      double s1 = -h1 * TDMath.cosDd( b );
      double e1 =  h1 * TDMath.sinDd( b );
      // double l = d * sh.mDirection * sh.mBranchDir; // FIXME DIRECTION
      double l = d * sh.mBranchDir;
      e1 += e0*l;
      s1 += s0*l;
      v1 += v0*l;
     
      h1 = Math.sqrt( e1*e1 + s1*s1 );
      b = TDMath.atan2Fd( e1, -s1 ); // + 90.0f * (1 - sh.mDirection); // FIXME PRE-DIRECTION
      if ( b < 0 ) b += 360;
      c = TDMath.atan2Fd( -v1, h1 ); // * sh.mDirection; // FIXME PRE-DIRECTION
      d = TDMath.sqrtF( h1*h1 + v1*v1 );
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

