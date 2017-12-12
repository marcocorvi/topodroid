/* @file DrawingUtilLandscape.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing landscape utility
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Paint;
// import android.graphics.Paint.FontMetrics;
// import android.graphics.PointF;
import android.graphics.Path;
// import android.graphics.Path.Direction;


class DrawingUtilLandscape extends DrawingUtil
{
  // private static final PointF mCenter = new PointF( CENTER_X, CENTER_Y );

  float toSceneX( float x, float y ) { return CENTER_Y + y * SCALE_FIX; }
  float toSceneY( float x, float y ) { return -(CENTER_X + x * SCALE_FIX); }

  float sceneToWorldX( float x, float y ) { return (-y - CENTER_Y)/SCALE_FIX; }
  float sceneToWorldY( float x, float y ) { return ( x - CENTER_X)/SCALE_FIX; }
    
}
