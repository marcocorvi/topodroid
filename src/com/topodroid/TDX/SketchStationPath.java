/* @file SketchStationPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: paths (points, lines, and areas)
 * 
 * StationPath path is a straight line between the two endpoints
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
// import com.topodroid.prefs.TDSetting;
// import com.topodroid.math.Point2D; // float X-Y

// import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
// import android.graphics.Paint.FontMetrics;
// import android.graphics.RectF;


public class SketchStationPath extends SketchPath 
{
  private TDVector mV;    // (E,N,Up)
  private String mName;
  private String mFullname;
  private String mFrom;   // fullname of the other leg station
  private boolean mForward; // true if this station is to TO of its leg (false if FROM is null)

  /** cstr
   * @param paint  path paint
   * @param v      station point vector (E,N,Up)
   * @param name   station name
   */
  public SketchStationPath( Paint paint, TDVector v, String name, String fullname, String from, boolean forward )
  {
    super( SketchPath.SKETCH_PATH_STATION, paint );
    mV = v;
    mName = name;
    mFullname = fullname;
    mFrom = from;
    mForward = forward;
  }

  void dump( String msg )
  {
    TDLog.v("SKETCH " + msg + " station " + mName + " (" + mFullname + " " + mFrom + ") fwd " + mForward + " " + mV.x + " " + mV.y + " " + mV.z );
  }

  /** @return the station 3D vector point
   */
  TDVector getTDVector() { return mV; }

  // void dump( String msg )
  // {
  //   TDLog.v("SKETCH " + msg + ": " + name + " " + mV.x + " " + mV.y + " " + mV.z );
  // }

  /** @return the station name
   */
  String name() { return mName; }

  /** @return the station fullname
   */
  String fullname() { return mFullname; }

  /** @return the name of the other leg-station
   */
  String from() { return mFrom; }

  /** @return the number of points in the path
   */
  @Override
  public int size() { return 1; }

  boolean isForward() { return mForward; }

  /** write the path to a data stream - it does nothing by default
   * @param dos   output stream
   */
  @Override
  public void toDataStream( DataOutputStream dos ) throws IOException
  {
    dos.write( 'N' );
    dos.writeUTF( mName );
    dos.writeUTF( mFullname );
    dos.writeUTF( mFrom );
    dos.write( mForward? 't' : 'f' );
    dos.writeFloat( mV.x );
    dos.writeFloat( mV.y );
    dos.writeFloat( mV.z );
  }

  /** read from a stream
   * @param cmd  command manager (unused)
   * @param dis  input stream
   * @param version file version
   */
  @Override
  public int fromDataStream( SketchCommandManager cmd, DataInputStream dis, int version ) throws IOException
  {
    dataCheck( "Station", ( dis.read() == 'N' ) );
    mName = dis.readUTF();
    mFullname = dis.readUTF();
    mFrom = dis.readUTF();
    mForward = (dis.read() == 't');
    mV.x = dis.readFloat();
    mV.y = dis.readFloat();
    mV.z = dis.readFloat();
    return 1;
  }

  // -----------------------------------------------------------------------

  /** make projected path in world coords
   * @pre mName and mPaint are not null
   */
  @Override
  Path makeProjectedPath( TDVector C, TDVector X, TDVector Y )
  {
    float len = mPaint.measureText( mName ) + 10;
    TDVector v = mV.minus( C );
    float x = X.dot( v );
    float y = Y.dot( v );
    Path path = new Path();
    path.moveTo( x, y );
    path.lineTo( x + len, y );
    return path;
  }

  /** draw the path on a canvas
   * @param canvas   canvas - N.B. canvas is guaranteed not null
   * @param matrix   transform matrix
   * @param C        center (E,N,Up)
   * @param X        horizontal direction (E,N,Up)
   * @param Y        downward direction in (E,N,Up)
   * @param r        dot radius
   */
  @Override
  public void draw( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y )
  {
    if ( mName == null || mPaint == null ) return;
    Path path = makeProjectedPath( C, X, Y );
    path.transform( mm );
    canvas.drawTextOnPath( mName, path, 0f, 0f, mPaint );
  }

  public void drawPoint( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y, float radius )
  {
    SketchPath.drawVector( mV, canvas, mm, C, X, Y, radius );
  }

}
