/* @file SketchPath.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid 3d sketch: path types (points, lines, and areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * 20130220 created
 */
package com.topodroid.DistoX;

import android.graphics.Paint;

// import android.util.Log;

/**
 * The SketchPath add common metainfo at a "path", namely
 *   - the two stations between which the path is placed
 *   - the type (POINT, LINE, AREA)
 *   - the view type (top, side, 3d)
 *   - the therion type
 *   - the paint
 *   - the surface of this path
 */
public class SketchPath extends SketchShot
{
  int mType;       // DrawingBrushPaths.DRAWING_TYPE_POINT LINE AREA
  int mThType;     // item therion type
  Paint mPaint;
  SketchSurface mSurface;

  /** 
   * @param type    path type
   * @param s1      first station name
   * @param s2      second station name
   */
  SketchPath( int type, String s1, String s2 )
  {
    super( s1, s2 );
    mType     = type;
    mThType   = -1;
    mPaint    = null;
    mSurface  = null;
  }

  public String toTherion() { return new String("FIXME SketchPath::toTherion()"); }

}
