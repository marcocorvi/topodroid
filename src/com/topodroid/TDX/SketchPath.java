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
import java.io.IOException;

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
  public static final int SKETCH_PATH_NGHB    = 3; // neighbor leg
  public static final int SKETCH_PATH_WALL    = 4;
  public static final int SKETCH_PATH_STATION = 5;
  public static final int SKETCH_PATH_SECTION = 6;
  public static final int SKETCH_PATH_LINE    = 7;
  // public static final int SKETCH_PATH_POINT   = 8; // drawing point
  // public static final int SKETCH_PATH_AREA    = 9;

  protected int mPathType;      // path type
  protected boolean  mVisible = true; 
  protected Paint mPaint;

  /** cstr
   * @param type   path type: POINT (1), LINE (2), AREA (3) etc.
   */
  protected SketchPath( int type, Paint paint )
  {
    mPathType  = type;
    mPaint = paint;
  }

  // void log()
  // {
  //   // TDLog.v("PATH " + "Path " + x1 + " " + y1 + "   " + x2 + " " + y2 );
  // }


  /** write the path to a data stream - it does nothing by default
   * @param dos   output stream
   */
  public void toDataStream( DataOutputStream dos ) throws IOException { TDLog.Error( "ERROR Sketch Path toDataStream"); }

  /** read from a stream
   * @param cmd  command manager (unused)
   * @param dis  input stream
   * @param version file version
   * @return 0 by default
   */
  public int fromDataStream( SketchCommandManager cmd, DataInputStream dis, int version ) throws IOException
  {
    TDLog.Error( "ERROR Sketch Path fromDataStream");
    return 0;
  }

  protected static void toDataStream( DataOutputStream dos, TDVector v ) throws IOException
  {
    dos.writeFloat( v.x );
    dos.writeFloat( v.y );
    dos.writeFloat( v.z );
  }

  protected static TDVector tdVectorFromDataStream( DataInputStream dis ) throws IOException
  {
    float x = dis.readFloat();
    float y = dis.readFloat();
    float z = dis.readFloat();
    return new TDVector( x, y, z );
  }

  /** @return the type of the path
   */
  public int getPathType() { return mPathType; }

  /** set visibility
   * @param visible   visibility
   */
  public void setVisibility( boolean visible ) { mVisible = visible; }

  public void setPaint( Paint paint ) { mPaint = paint; }

  public int size() { return 0; }


  /** draw the path on a canvas
   * @param canvas   canvas - N.B. canvas is guaranteed not null
   * @param matrix   transform matrix
   * @param C        center (E,N,Up)
   * @param X        horizontal direction (E,N,Up)
   * @param Y        downward direction in (E,N,Up)
   */
  public void draw( Canvas canvas, Matrix matrix, TDVector C, TDVector X, TDVector Y )
  {
    if ( ! mVisible ) return;
    Path path = makeProjectedPath( C, X, Y );
    if ( path != null ) {
      path.transform( matrix );
      // path.offset( off_x, off_y );
      drawPath( path, canvas );
    }
  }

  /** to be overridden
   * @param C        center (E,N,Up)
   * @param X        horizontal direction (E,N,Up)
   * @param Y        downward direction in (E,N,Up)
   */
  Path makeProjectedPath( TDVector C, TDVector X, TDVector Y ) { return null; }


  /** draw the path on the canvas with the class paint
   * @param path    path
   * @param canvas  canvas
   */
  private void drawPath( Path path, Canvas canvas )
  {
    if ( mPaint == null ) return;
    canvas.drawPath( path, mPaint );
  }


  static void dataCheck( String msg, boolean test )
  {
    if ( ! test ) TDLog.Error("ERROR failed " + msg );
  }

  /** draw a pink circle
   * @note used by SketchPoint, SketchStationPath and SketchSection
   * @param vv  circle center
   * @param canvas    canvas
   * @param mm        transform matrix
   * @param C         projection center
   * @param X         projection X-axis 
   * @param Y         projection Y-axis 
   * @param r         circle radius
   */
  static void drawVector( TDVector vv, Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y, float r )
  {
    Path path = new Path();
    TDVector v = vv.minus( C );
    float x = X.dot( v ); // (world coord)
    float y = Y.dot( v ); // downward 
    path.addCircle( x, y, r, Path.Direction.CCW );
    path.transform( mm );
    canvas.drawPath( path, BrushManager.errorPaint );
  }

  // debug
  static void drawSegment( TDVector v1, TDVector v2, Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y, Paint paint )
  {
    Path path = new Path();
    TDVector w = v1.minus( C );
    float x = X.dot( w ); // (world coord)
    float y = Y.dot( w ); // downward 
    path.moveTo( x, y );
    w = v2.minus( C );
    x = X.dot( w ); // (world coord)
    y = Y.dot( w ); // downward 
    path.lineTo( x, y );
    path.transform( mm );
    canvas.drawPath( path, paint );
  }

}
