/** @file Cave3DTube.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief tube walls between two XSections
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import java.util.ArrayList;

class Cave3DTube
{
  Cave3DShot     shot;    
  Cave3DXSection xs1;
  Cave3DXSection xs2;
  ArrayList< Triangle3D > triangles;
  int color; // DEBUG

  /** get the size of the projections
   * @param k    proj index: 0 at FROM, 1 at TO
   */
  // int projSize( int k ) { return (k==0)? projs1.size() : projs2.size(); }

  int size() { return triangles == null ? 0 : triangles.size(); }

  Cave3DTube( Cave3DShot sh,                   // shot
              Cave3DXSection sf,                // shot FROM station
              Cave3DXSection st )               // shot TO station
  {
    // TDLog.v("Hull at station " + st.name + " shot " + sh.from_station.name + "-" + sh.to_station.name );
    shot = sh;
    xs1  = sf;
    xs2  = st;

    triangles = new ArrayList< Triangle3D >();
    computeTube();
  }

  // void dumpTube()
  // {
  //   TDLog.v("Tube " + xs1.name() + "--" + xs2.name() + " triangles " + triangles.size() );
  // }

  private void addTriangle( Vector3D p1, Vector3D p2, Vector3D p3 )
  {
    triangles.add( new Triangle3D( p1, p2, p3, color ) );
  }


  private void computeTube()
  {
    // TODO
    int k1 = 0;
    int k2 = 0;
    int k1size = xs1.size();
    int k2size = xs2.size();
    boolean reverse1 = shot.dotProduct( xs1.normal ) < 0;
    boolean reverse2 = shot.dotProduct( xs2.normal ) < 0;
    // TDLog.v("Tube " + xs1.name() + "--" + xs2.name() + " compute tube " + xs1.size() + " " + xs2.size() + " R " + reverse1 + " " + reverse2 );
    // xs1.dump();
    // xs2.dump();
    double a1 = xs1.angle( k1, reverse1 );
    double a2 = xs2.angle( k2, reverse2 );
    double a10 = 0;
    double a20 = 0;
    if ( a1 < a2 ) { a20 = a2 - a1; } else { a10 = a1 - a2; }

    Vector3D p10 = xs1.point(k1,reverse1);
    Vector3D p20 = xs2.point(k2,reverse2);
    boolean done1 = false;
    boolean done2 = false;
    boolean inc1 = false;
    // TDLog.v("start " + k1 + " " + k2 );
    do {
      if ( done1 ) {
        int k2p = (k2+1) % k2size;
        done2 = k2p == 0;
        // TDLog.v("2-insert " + k2 + " -> " + k2p );
        Vector3D p2 = xs2.point( k2p, reverse2 );
        addTriangle( p10, p2, p20 );
        p20 = p2;
        k2 = k2p;
        double a = xs2.angle( k2, reverse2 );
        a20 += a - a2;
        a2 = a;
      } else if ( done2 ) {
        int k1p = (k1+1) % k1size;
        done1 = k1p == 0;
        // TDLog.v("1-insert " + k1 + " -> " + k1p );
        Vector3D p1 = xs1.point( k1p, reverse1 );
        addTriangle(p10, p1, p20 );
        p10 = p1;
        k1 = k1p;
        double a = xs1.angle( k1, reverse1 );
        a10 += a - a1;
        a1 = a;
      } else if ( a10 == a20 ) {
        if ( inc1 ) {
          int k2p = (k2+1) % k2size;
          done2 = k2p == 0;
          inc1 = false;
          // TDLog.v("2-insert " + k2 + " -> " + k2p + " a1 = a2 " + a10 );
          Vector3D p2 = xs2.point( k2p, reverse2 );
          addTriangle( p10, p2, p20 );
          p20 = p2;
          k2 = k2p;
          double a = xs2.angle( k2, reverse2 );
          a20 += a - a2;
          a2 = a;
        } else {
          int k1p = (k1+1) % k1size;
          done1 = k1p == 0;
          inc1 = true;
          // TDLog.v("1-insert " + k1 + " -> " + k1p + " a1 = a2 " + a10 );
          Vector3D p1 = xs1.point( k1p, reverse1 );
          addTriangle(p10, p1, p20 );
          p10 = p1;
          k1 = k1p;
          double a = xs1.angle( k1, reverse1 );
          a10 += a - a1;
          a1 = a;
        }
      } else if ( a10 < a20 ) {
        int k1p = (k1+1) % k1size;
        done1 = k1p == 0;
        inc1 = true;
        // TDLog.v("1-insert " + k1 + " -> " + k1p + " a1 < a2 " + a10 + " < " + a20 );
        Vector3D p1 = xs1.point( k1p, reverse1 );
        addTriangle(p10, p1, p20 );
        p10 = p1;
        k1 = k1p;
        double a = xs1.angle( k1, reverse1 );
        a10 += a - a1;
        a1 = a;
      } else {
        int k2p = (k2+1) % k2size;
        done2 = k2p == 0;
        inc1 = false;
        // TDLog.v("2-insert " + k2 + " -> " + k2p + " a1 > a2 " + a10 + " > " + a20 );
        Vector3D p2 = xs2.point( k2p, reverse2 );
        addTriangle( p10, p2, p20 );
        p20 = p2;
        k2 = k2p;
        double a = xs2.angle( k2, reverse2 );
        a20 += a - a2;
        a2 = a;
      }
    } while ( ! ( done1 && done2 ) );
  }

}
