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
 * CHANGES
 * 20130108 created fron DistoXNum
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

// import android.util.Log;

public class NumBranch 
{
  private static final float grad2rad = TopoDroidUtil.GRAD2RAD;

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
  //   TopoDroidLog.Log( TopoDroidLog.LOG_NUM, sb.toString() );
  // }

  NumBranch( int t, NumNode n )
  {
    type = t;
    n1 = n;
    n2 = null;
    shots = new ArrayList<NumShot>();
    use = 0;
    e = 0.0f;
    s = 0.0f;
    v = 0.0f;
    len = 0.0f;
  }

  void addShot( NumShot shot )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_NUM, "Br add shot(" + shot.from.name + "-" + shot.to.name + ") bdir " + shot.mBranchDir + " sdir " + shot.mDirection );
    shots.add( shot );
    // float d = shot.mLength;
    // len += d;
  }

  void computeError()
  {
    e = 0.0f;
    s = 0.0f;
    v = 0.0f;
    len = 0.0f;
    for ( NumShot sh : shots ) {
      float d = sh.mLength;
      float b = sh.mBearing * grad2rad;
      float c = sh.mClino * grad2rad;
      len += d;
      d *= sh.mDirection * sh.mBranchDir;
      v -= d * (float)Math.sin(c);
      float h0 = d * (float)Math.abs( Math.cos(c) );
      s -= h0 * (float)Math.cos(b);
      e += h0 * (float)Math.sin(b);
      // TopoDroidLog.Log( TopoDroidLog.LOG_NUM, "Br sh " + sh.from.name + "-" + sh.to.name + " Br Err " + e + " " + s );
    }
    
    // NumStation st = ( n1 == null )? shots.get(0).from : n1.station;
    // NumStation sf = ( n2 == null )? st : n2.station;     // Log only
    // dump();
    // TopoDroidLog.Log( TopoDroidLog.LOG_NUM, "Br " + st.name + "=" + sf.name + " delta " + e + " " + s + " " + v );
  }

  void compensateError( float e0, float s0, float v0 )
  {
    // NumStation st = ( n1 == null )? shots.get(0).from : n1.station;
    // NumStation sf = ( n2 == null )? st : n2.station;     // Log only
    // dump();
    // TopoDroidLog.Log( TopoDroidLog.LOG_NUM, "Br " + st.name + "=" + sf.name + " compensate error " + e0 + " " + s0 + " " + v0 );

    e0 /= len;
    s0 /= len;
    v0 /= len;
    for ( NumShot sh : shots ) {
      // block displacement vector (absolute, not in the branch)
      float d = sh.mLength;
      float b = sh.mBearing * grad2rad;
      float c = sh.mClino * grad2rad;
      float v1 = -d * (float)Math.sin(c);
      float h1 =  d * (float)Math.abs( Math.cos(c) );
      float s1 = -h1 * (float)Math.cos(b);
      float e1 =  h1 * (float)Math.sin(b);
      float l = d * sh.mDirection * sh.mBranchDir;
      e1 += e0*l;
      s1 += s0*l;
      v1 += v0*l;
     
      h1 = (float)(Math.sqrt( e1*e1 + s1*s1 ));
      sh.mBearing = (float)(Math.atan2( e1, -s1 ) / grad2rad); // + 90.0f * (1 - sh.mDirection);
      if ( sh.mBearing >= 360.0f) sh.mBearing -= 360.0f;
      sh.mClino   = (float)(Math.atan2( -v1, h1 ) / grad2rad); // * sh.mDirection;
      sh.mLength  = (float)(Math.sqrt( h1*h1 + v1*v1 ));
      // TopoDroidLog.Log( TopoDroidLog.LOG_NUM, "shift shot " + sh.from.name + "-" + sh.to.name + " shift " + e1 + " " + s1 + "  " + v1 );
      // TopoDroidLog.Log( TopoDroidLog.LOG_NUM, "corrected block " + sh.from.name + "-" + sh.to.name + " " + sh.mLength + " " + sh.mBearing + " " + sh.mClino );
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

