/* @file SketchPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: paths (points, lines, and areas)
 * 
 * FixedPath path is a straight line between the two endpoints
 * GridPath paths are also straight lines
 * PreviewPath path is a line with "many" points
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.math.TDVector;

import java.io.DataOutputStream;
import java.io.DataInputStream;

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

public class SketchPath 
{
  public static final int SKETCH_PATH_LEG     = 0; // leg
  public static final int SKETCH_PATH_SPLAY   = 1; // splay
  public static final int SKETCH_PATH_GRID    = 2; // grid
  public static final int SKETCH_PATH_WALL    = 3;
  public static final int SKETCH_PATH_SECTION = 4;
  public static final int SKETCH_PATH_LINE    = 5;
  // public static final int SKETCH_PATH_POINT   = 4; // drawing point
  // public static final int SKETCH_PATH_AREA    = 6;

  protected int mType;      // path type
  protected boolean  mVisible = true; 
  protected Paint mPaint;

  /** cstr
   * @param type   path type: POINT (1), LINE (2), AREA (3) etc.
   */
  protected SketchPath( int type, Paint paint )
  {
    mType  = type;
    mPaint = paint;
  }

  // void log()
  // {
  //   // TDLog.v("PATH " + "Path " + x1 + " " + y1 + "   " + x2 + " " + y2 );
  // }


  /** write the path to a data stream - it does nothing by default
   * @param dos   output stream
   */
  public void toDataStream( DataOutputStream dos ) { TDLog.Error( "ERROR Sketch Path toDataStream"); }

  public void fromDataStream( DataInputStream dos ) { TDLog.Error( "ERROR Sketch Path fromDataStream"); }

  /** @return the type of the command, namely 0
   */
  public int type() { return mType; }

  /** set visibility
   * @param visible   visibility
   */
  public void setVisibility( boolean visible ) { mVisible = visible; }

  public void setPaint( Paint paint ) { mPaint = paint; }

  public int size() { return 0; }

  /** draw the path on a canvas
   * @param canvas   canvas - N.B. canvas is guaranteed not null
   * @param C        center (E,N,Up)
   * @param X        horizontal direction (E,N,Up)
   * @param Y        downward direction in (E,N,Up)
   * @param zoom     zoom
   * @param off_x    X offset
   * @param off_y    Y offset
   */
  public void draw( Canvas canvas, TDVector C, TDVector X, TDVector Y, float zoom, float off_x, float off_y )
  {
    if ( ! mVisible ) return;
    Path path = makeProjectedPath( C, X, Y, zoom );
    if ( path != null ) {
      path.offset( off_x, off_y );
      drawPath( path, canvas );
    }
  }


  /** draw the path on a canvas
   * @param canvas   canvas - N.B. canvas is guaranteed not null
   * @param matrix   transform matrix
   * @param C        center (E,N,Up)
   * @param X        horizontal direction (E,N,Up)
   * @param Y        downward direction in (E,N,Up)
   * @param zoom     zoom
   * @param off_x    X offset
   * @param off_y    Y offset
   */
  public void draw( Canvas canvas, Matrix matrix, TDVector C, TDVector X, TDVector Y, float zoom, float off_x, float off_y )
  {
    if ( ! mVisible ) return;
    Path path = makeProjectedPath( C, X, Y, zoom );
    if ( path != null ) {
      path.transform( matrix );
      path.offset( off_x, off_y );
      drawPath( path, canvas );
    }
  }

  /** to be overridden
   * @param C        center (E,N,Up)
   * @param X        horizontal direction (E,N,Up)
   * @param Y        downward direction in (E,N,Up)
   * @param zoom     zoom
   */
  Path makeProjectedPath( TDVector C, TDVector X, TDVector Y, float zoom ) { return null; }


  /** draw the path on the canvas with the class paint
   * @param path    path
   * @param canvas  canvas
   */
  private void drawPath( Path path, Canvas canvas )
  {
    if ( mPaint == null ) return;
    canvas.drawPath( path, mPaint );
  }

}
