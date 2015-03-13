/* @file SketchRefinement.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid sketch triangle refinement
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;


class SketchRefinement
{ 
  SketchTriangle t; // triangle to refine
  SketchVertex v;   // top-vertex
  Vector v2;        // intermediate new vertex between top and left
  Vector v3;        // intermediate new vertex between top and right

  SketchRefinement( SketchTriangle t0, SketchVertex v0, Vector v20, Vector v30 )
  {
    t = t0;
    v = v0;
    v2 = v20;
    v3 = v30;
  }
}


