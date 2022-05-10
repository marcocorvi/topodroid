/** @file PCSegment.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief 3D segment
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.pcrust;

import com.topodroid.TDX.Vector3D;

// import java.io.StringWriter;
// import java.io.PrintWriter;

public class PCSegment
{
  PCIntersection v1, v2;
  PCSegment next;
  double s;

  PCSegment( PCIntersection q1, PCIntersection q2 )
  {
    v1 = q1;
    v2 = q2;
    s = ( q1.s + q2.s )/2;
    next = null;
  }

/*
  int hasEndPoint( Vector3D v, double eps )
  {
    if ( v1.coincide( v, eps ) ) return 1;
    if ( v2.coincide( v, eps ) ) return 2;
    return 0;
  }

  boolean touches( PCSegment s, double eps ) 
  {
    return hasEndPoint( s.v1, eps ) != 0 || hasEndPoint( s.v2, eps  ) != 0;
  }
*/

  double s() { return s; }

  public Vector3D getV1() { return v1; }
  public Vector3D getV2() { return v2; }

}
