/** @file PCSite.java
 *
 *e @author marco corvi
 * @date may 2017 
 *
 * @brief Cave3D powercrust 3D wall site
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.pcrust;

import com.topodroid.DistoX.Vector3D;

import com.topodroid.utils.TDLog;

class PCSite extends Vector3D
{
  Angle angle; // head of list of disjoint angles at the site
  PCPolygon poly;

  class Angle // angle (between two sites) at a site
  {
    PCSite v1; // angle side vertices
    PCSite v2;
    Angle next; // links in the list of site's angles
    Angle prev;

    Angle( PCSite w1, PCSite w2 ) // create an angle w1-w2 (not inserted in the list of the site's angles)
    {
      v1 = w1;
      v2 = w2;
      next = prev = null; 
    }
  }

  PCSite( double x, double y, double z ) // create a site with empty list
  {
    super( x, y, z );
    angle = null;
    poly  = null;
  }

  // double distance2D( PCSite s ) 
  // {
  //   return Math.sqrt( (x - s.x)*(x - s.x) + (y - s.y)*(y - s.y) );
  // }

  private void unlink( Angle b ) 
  {
    if ( b.next != null ) b.next.prev = b.prev; // a.prev;
    if ( b.prev != null ) b.prev.next = b.next; else angle = b.next;
    b.next = b.prev = null;
  }

  // insert angle W1--Site--W2:
  // if there is already an angle with vertex V2 equals to W1
  //    change the angle vertex V2 to W2
  //    next if there is another angle with vertex V1 equals to W2
  //      move the angle vertex V2 to the other angle V2
  void insertAngle( PCSite w1, PCSite w2 )
  {
    boolean done = false;
    for ( Angle a = angle; a != null; a = a.next ) {
      if ( a.v2 == w1 ) { // ... -p- v1 -a- w1 -n- ...
        done = true;
        a.v2 = w2;        // ... -p- v1 -a- w2  ||    w1 -n- ...
        for ( Angle b = angle; b != null; b = b.next ) {
          if ( b == a ) continue;
          if ( a.v2 == b.v1 ) { // ... -p- v1 -a- w2 -b- v2 -N- ...
            a.v2 = b.v2;        // ... -p- v1 -a- v2 - ...
            unlink( b );
            break; // because all angles are disjoint
          }
        }
        break;
      }
      if ( a.v1 == w2 ) {
        done = true;
        a.v1 = w1;
        for ( Angle b = angle; b != null; b = b.next ) {
          if ( b == a ) continue;
          if ( a.v1 == b.v2 ) {
            a.v1 = b.v1;
            unlink( b );
            break; // because all angles are disjoint
          }
        }
        break;
      }
    }
    if ( ! done ) { // put A at the head of the list:
                    //                   angle <--> A1 ...
                    //    angle = A <--> ..... <--> A1 ...
      Angle a = new Angle( w1, w2 );
      a.next = angle;
      if ( angle != null ) angle.prev = a;
      angle = a;
    }
  }

  // a PCSite is NOT open if
  //   - either the list of angles is empty
  //   - or head.v1 == tail.v2
  boolean isOpen( ) 
  {
    if ( angle == null ) return false;
    if ( angle.next != null ) {
      // TDLog.Error( "SITE site with more than one angle");
      Angle n = angle.next;  // get the tail of the list of angles
      while ( n.next != null ) n = n.next;
      return n.v2 != angle.v1;
    }
    return ( angle.v1 != angle.v2 );
  }

}
