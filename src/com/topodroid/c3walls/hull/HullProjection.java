/** @file HullProjection.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief a normal plane projection vector for the simple Hull
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.c3walls.hull;

import com.topodroid.DistoX.Cave3DShot;
import com.topodroid.DistoX.Cave3DStation;
import com.topodroid.DistoX.Vector3D;
import com.topodroid.DistoX.TopoGL;

import com.topodroid.utils.TDLog;

class HullProjection 
{
  Cave3DShot shot;    // splay shot (or null 
  Vector3D   vector;  // 3D vector (absolute coords)
  Vector3D   proj;    // plane projection (relative coords: origin at the station first, at the hull center later)
  double     angle;   // angle with the reference projection

  // copy of a projection with increased angle
  HullProjection( HullProjection p, double da )
  {
    shot   = p.shot;
    vector = p.vector;
    proj   = p.proj;
    angle  = p.angle + da;
  }

  /** 
   * @param s   splay shot
   * @param v   splay vector (not normalized)
   * @param pv  projection of v
   */
  HullProjection( Cave3DStation st, Cave3DShot s, Vector3D v, Vector3D pv )
  {
    shot   = s;
    angle  = 0.0f;
    if ( shot != null ) {
      vector = v;
      proj = pv;

      // make vector in absolute ref system
      if ( TopoGL.mSplayProj ) {
        vector.x = st.x + proj.x;  // V = Station + Projection
        vector.y = st.y + proj.y;
        vector.z = st.z + proj.z;
      } else {
        vector.x += st.x; // if using splay vectors: V = Station + Splay
        vector.y += st.y;
        vector.z += st.z;
      }
    } else {
      proj   = new Vector3D( 0, 0, 0 );
      vector = new Vector3D( st.x, st.y, st.z );
    }
  }

  void dump()
  {
    TDLog.v("PROJ " + proj.x + " " + proj.y + " " + proj.z + " angle " + angle );
  }
}
