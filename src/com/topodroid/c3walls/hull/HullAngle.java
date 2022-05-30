/** @file HullAngle.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D angle sectors around a leg (for the projections half-planes)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.hull;

import com.topodroid.TDX.Cave3DShot;
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.TDX.Vector3D;
import com.topodroid.TDX.TopoGL;

import com.topodroid.utils.TDLog;

class HullAngle
{
  Cave3DStation station; // DEBUG
  double ber;    // shot bearing ( direct at FROM, opposite at TO ) [radians]
  Vector3D vBer; // 3D vector along bearing (E, N, Up)
  double a1;     // bearing to the right of ber [radians]
  double a2;     // bearing to the left of ber [radians]
  // a2 < ber < a1
  Vector3D n1, n2;

  double right;

  void dump()
  {
    TDLog.v("ANGLE " + station.getShortName() + " Ber " + deg(ber) + " a1 " + deg(a1) + " a2 " + deg(a2) + " V-ber " + vBer.x + " " + vBer.y + " " + vBer.z );
  }
    

  // only initialized: need to update angles and makeNormals at the end
  HullAngle( Cave3DStation st, double b )
  {
    station = st;
    ber = b;
    a1 = ber + Math.PI;
    a2 = ber - Math.PI;
    vBer = new Vector3D( Math.sin(ber), Math.cos(ber), 0 ); 
  }

  // fully constructed
  HullAngle( Cave3DStation st, double b, double aa1, double aa2 )
  {
    station = st;
    ber = b;
    a1 = aa1;
    a2 = aa2;
    makeNormals();
    vBer = new Vector3D( Math.sin(ber), Math.cos(ber), 0 );
  }

  // normals to the bearings a1 and a2
  // (E,N,Up) = (cos, -sin, 0) because ber(a) = ( sin(a), cos(a), 0 )
  void makeNormals()
  {
    n1 = new Vector3D( Math.cos(a1), -Math.sin(a1), 0 ); 
    n2 = new Vector3D( Math.cos(a2), -Math.sin(a2), 0 ); 
  }

  // check if b0 in [b1, b2]
  // @pre b1 < b2 and | b2 - b1 | <= PI/2
  // b0, b1, b2 >= 0
  // 
  private boolean isInside( double b0, double b1, double b2 )
  {
    if ( b0 >= b1 && b0 <= b2 ) return true;
    b0 += 2*Math.PI;
    if ( b0 >= b1 && b0 <= b2 ) return true;
    b0 -= 4*Math.PI;
    if ( b0 >= b1 && b0 <= b2 ) return true;
    return false;
  }

  int isInside( Cave3DShot splay )
  { 
    if ( isInside( splay.ber, ber, a1 ) ) return  1;
    if ( isInside( splay.ber, a2, ber ) ) return -1;
    return 0;
  }

  // if the splay is right, project on A1-plane
  private boolean isRight( Cave3DShot splay ) 
  {
    double b = splay.ber;
    if ( ber <= Math.PI ) {
      return ( ber <= b && b <= ber+Math.PI );
    }
    return ! ( ber-Math.PI < b && b <= ber );
  }

  // @param v    splay vector
  // @param len  splay length
  private boolean isAlongside( Vector3D v, double len, double alpha )
  {
    double c = vBer.dotProduct( v );
    return Math.abs(c) > alpha * len;
  }

  // @param splay   splay shot
  // @param v       splay vector
  Vector3D projectSplay( Cave3DShot splay, Vector3D v )
  {
    if ( isAlongside( v, splay.len, TopoGL.mSplayThr ) ) {
      // TDLog.v("ANGLE alongside V " + v.x + " " + v.y + " " + v.z + " len " + splay.len );
      return null;
    }
    Vector3D ret = null;
    if ( isRight( splay ) ) {
      double vn = n1.dotProduct( v );
      ret = new Vector3D( v.x - n1.x*vn, v.y - n1.y*vn, v.z );
    } else {
      double vn = n2.dotProduct( v );
      ret = new Vector3D( v.x - n2.x*vn, v.y - n2.y*vn, v.z );
    }
    return ret;
  }
 
  double deg(double x) { return x*180/Math.PI; }

  private void updateA1( double a ) { if ( a < a1 ) a1 = a; } 
  private void updateA2( double a ) { if ( a > a2 ) a2 = a; } 

  void update( double b )
  {
    int choice = 0;
    if ( ber <= Math.PI ) {
      if ( ber < b  && b <= ber + Math.PI ) {  // 0 < ber < b < ber+PI < 2*PI
        choice = 11;
        double a = (b + ber)/2;
        updateA1( a );
        updateA2( a-Math.PI );
      } else if ( b <= ber ) {  // 0 < b < ber < PI 
        choice = 12;
        double a = (b + ber)/2;
        updateA2( a );
        updateA1( a+Math.PI );
      } else {                 // 0 < b < ber < PI
        choice = 13;
        double a = (b + ber)/2 - Math.PI;
        updateA2( a );
        updateA1( a+Math.PI );
      }
    } else { // ber > PI
      if ( ber-Math.PI < b && b <= ber ) {
        choice = 21;
        double a = ( b + ber )/2;
        updateA2( a );
        updateA1( a+Math.PI );
      } else if ( b <= ber-Math.PI ) {
        choice = 22;
        double a = (b + ber)/2 + Math.PI;
        updateA1( a );
        updateA2( a-Math.PI );
      } else {                   // PI < ber < b < 2*PI
        choice = 23;
        double a = (b + ber)/2;  
        updateA1( a );
        updateA2( a-Math.PI );
      }
    }
    // TDLog.v("ANGLE " + station.short_name + " update " + choice + " " + deg(ber) + " with " + deg(b) + " " + deg(a1) + " " + deg(a2) );
  }
    

}
