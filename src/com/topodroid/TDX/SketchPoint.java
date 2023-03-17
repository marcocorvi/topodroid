/* @file SketchPoint.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketching: point of a line
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDMath;
// import com.topodroid.utils.TDLog;
import com.topodroid.math.TDVector;

import android.graphics.Canvas;
// import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;


public class SketchPoint extends TDVector // world coords
{
  SketchLinePath mLine;
  float mRadius = 0; // polar coords on the vert/horiz plane refrenced to the basepoint on the leg
  float mAlpha  = 0; // [radians]

  /** cstr
   * @param v     3D point
   * @param line  sketch line to which this point belongs
   */
  public SketchPoint( TDVector v, SketchLinePath line )
  {
    super( v );
    mLine = line;
  }

  /** cstr
   * @param v     3D point
   * @note line is unspecified (null)
   */
  public SketchPoint( TDVector v ) { this(v, null); }

  // @param r  dot radius
  void drawPoint( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y, float r )
  {
    SketchPath.drawVector( this, canvas, mm, C, X, Y, r );
  }

  /** @return true if this point is eps-close to a specified vector
   * @param v    given vector
   * @param eps  epsilon
   */
  boolean isClose( TDVector v, double eps ) { return this.maxDiff( v ) < eps; }

}

