/** @file Line3D.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid sketch 3D line
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130219 created 
 * 20130830 addPoint for Vector
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

/** 
 * Line3D is an array of 3D points (Vector)
 */
class Line3D
{
  ArrayList< Vector > points;

  Line3D()
  {
    points = new ArrayList< Vector >();
  }

  void addPoint( float x, float y, float z )
  {
    points.add( new Vector(x,y,z) );
  }

  void addPoint( Vector v )
  {
    points.add( new Vector( v.x, v.y, v.z ) );
  }

}
