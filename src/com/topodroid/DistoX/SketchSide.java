/** @file SketchSide.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid 3d sketch: surface vertex (point with index)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130219 created 
 */
package com.topodroid.DistoX;

import java.io.PrintWriter;

/** side in the triangulated surafce
 *
 * the order of the vertices and that of the triangles are not related
 */
class SketchSide
{
  SketchSurface surface;
  int index;         // index in the array of sides
  boolean highlight; // whether this side is highlighted
  int v1;            // index of first vertex
  int v2;            // index of second vertex
  SketchTriangle t1;
  SketchTriangle t2;
  float length;      // side length (used to compute refineToMaxSide )

  SketchSide( SketchSurface parent, int idx, int vertex1, int vertex2 )
  {
    surface = parent;
    index   = idx;
    highlight = false;
    v1 = vertex1;
    v2 = vertex2;
    t1 = null;
    t2 = null;
    length = 0f;
  }

  void addTriangle( SketchTriangle t )
  {
    if ( t1 == null ) {
      t1 = t;
    } else if ( t2 == null ) {
      t2 = t;
    }
  }

  boolean hasVertices( int i, int j )
  {
    return ( ( v1 == i) && (v2 == j) ) || ( ( v1 == j) && (v2 == i) );
  }

  SketchTriangle otherTriangle( SketchTriangle t ) 
  {
    if ( t == t1 ) return t2;
    if ( t == t2 ) return t1;
    return null;
  }

  // remove a triangle from this side
  boolean removeTriangle( SketchTriangle t )
  {
    if ( t == t2 ) {
      t2 = null;
      return true;
    }
    if ( t == t1 ) {
      t1 = t2;
      t2 = null;
      return true;
    }
    return false;
  }

  // -------------------------------------------------------
  // THERION: index and two vertex indices

  void toTherion( PrintWriter pw )
  {
    // FIXME triangles ?
    pw.format("  %d %d %d\n", index, v1, v2 );
  }
}


