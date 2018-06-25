/* @file Line3D.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid sketch 3D line
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
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
    points = new ArrayList<>();
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
