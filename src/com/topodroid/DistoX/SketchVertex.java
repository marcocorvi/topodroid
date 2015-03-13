/** @file SketchVertex.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid 3d sketch: 3D surface vertex (point with index)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130219 created 
 */
package com.topodroid.DistoX;

import java.io.PrintWriter;
import java.util.Locale;

class SketchVertex extends Vector
{
  SketchSurface surface;
  int index;   // index in the array of vertices
  // float angle; // scene angle with horizontal (work member used by SketchSurface)
  float dist;     // distance from border-3d used by stretch/extrude

  // x = east, y = south, z = vert(down)
  SketchVertex( SketchSurface parent, int idx, float x, float y, float z )
  {
    super( x, y, z );
    surface = parent;
    index   = idx;
  }

  // -------------------------------------------------------
  // THERION: index X Y Z
  // N.B. therion Y and Z coordinate are the negative of TopoDroid y,z coords

  void toTherion( PrintWriter pw )
  {
    pw.format(Locale.ENGLISH, "  %d %.4f %.4f %.4f\n", index, x, -y, -z );
  }
}

