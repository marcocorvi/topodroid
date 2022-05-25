/** @file SketchPoint.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief TopoDroid sketch point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

import com.topodroid.TDX.Vector3D;
import com.topodroid.TDX.GlSketch;

// import com.topodroid.utils.TDLog;

public class SketchPoint extends Vector3D
{
  public String thname;
  public int    idx;  // symbol index

  public SketchPoint( double x, double y, double z, String th )
  {
    super( x, y, z );
    thname = th;
    idx = GlSketch.getPointIndex( thname );
  }
}
