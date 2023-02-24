/* @file SketchLinePath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: paths (points, lines, and areas)
 * 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;
// import com.topodroid.math.Point2D; // float X-Y

import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

/**
 * direct/indirect subclasses:
 *   - SketchPointLinePath
 *      - SketchLinePath
 *      - DrawingAreaPath
 *   - DrawingPointPath
 *   - DrawingStationUser
 */

public class SketchLinePath extends SketchPath 
{
  ArrayList< SketchPoint > mPts;

  /** cstr
   * @param type   path type: POINT (1), LINE (2), AREA (3) etc.
   */
  public SketchLinePath( Paint paint )
  {
    super( SketchPath.SKETCH_PATH_LINE, paint );
    mPts = new ArrayList< SketchPoint >();
  }

  void appendPoint( TDVector v ) { mPts.add( new SketchPoint( v, this ) ); }

  @Override
  public int size() { return mPts.size(); }

  /** clear the path
   */
  void clear() { mPts.clear(); }

  /** write the path to a data stream - it does nothing by default
   * @param dos   output stream
   */
  @Override
  public void toDataStream( DataOutputStream dos ) { TDLog.Error( "ERROR Sketch Line Path toDataStream "); }

  @Override
  public void fromDataStream( DataInputStream dis ) { TDLog.Error( "ERROR Sketch Line Path fromDataStream "); }


  /** make projected path
   */
  @Override
  Path makeProjectedPath( TDVector C, TDVector X, TDVector Y, float zoom )
  {
    int sz = mPts.size();
    if ( sz > 2 ) {
      TDVector v = mPts.get(0).minus( C );
      Path path = new Path();
      path.moveTo( zoom * X.dot( v ), zoom * Y.dot( v ) );
      for ( int k=1; k<sz; ++k ) {
        v = mPts.get(k).minus( C );
        path.lineTo( zoom * X.dot( v ), zoom * Y.dot( v ) );
      }
      return path;
    }
    return null;
  }

  /** @return the distance to a given 3D line
   * @param line   3D line
   * @param pt     point of this line-path closest to the given line
   */
  float findClosestPoint( SketchLine line, SketchPoint pt )
  {
    pt = null;
    float ret = 10000000;
    for ( SketchPoint p : mPts ) {
      float d = line.distanceSquared( p );
      if ( d < ret ) { ret = d; pt = p; }
    }
   return TDMath.sqrt( ret );
  }

  // @param r  dot radius
  void drawPoints( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y, float zoom, float off_x, float off_y, float r )
  {
    if ( ! mVisible ) return;
    Path path = new Path();
    for ( SketchPoint p : mPts ) {
      TDVector v = p.minus( C );
      float x = zoom * X.dot( v );
      float y = zoom * Y.dot( v ); // downaward
      path.addCircle( x, y, r, Path.Direction.CCW );
    }
    path.transform( mm );
    path.offset( off_x, off_y );
    canvas.drawPath( path, BrushManager.fixedOrangePaint );
  }

}
