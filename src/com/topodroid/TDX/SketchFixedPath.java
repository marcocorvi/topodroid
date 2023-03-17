/* @file SketchFixedPath.java
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
// import com.topodroid.prefs.TDSetting;
// import com.topodroid.math.Point2D; // float X-Y

// import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

// import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
// import android.graphics.RectF;


public class SketchFixedPath extends SketchPath 
{
  TDVector mV1; // (E,N,Up)
  TDVector mV2;
  TDVector mU;  // unit vector of the line

  /** cstr
   * @param type   path type: 
   * @param paint  path paint
   * @param v1     first endpoint vector (E,N,Up)
   * @param v2     second endpoint vector
   */
  public SketchFixedPath( int type, Paint paint, TDVector v1, TDVector v2 )
  {
    super( type, paint );
    mV1 = v1;
    mV2 = v2;
    mU  = mV2.minus( mV1 );
    mU.normalize();
  }

  void dump( String msg )
  {
    TDLog.v("SKETCH " + msg + " " + mV1.x + " " + mV1.y + " " + mV1.z + "  .. "  + mV2.x + " " + mV2.y + " " + mV2.z );
  }

  TDVector midpoint() { return new TDVector( (mV1.x+mV2.x)/2, (mV1.y+mV2.y)/2, (mV1.z+mV2.z)/2 ); }

  TDVector oppositeDirection() { return mV1.minus( mV2 ).getUnitVector(); }

  float length() { return mV2.distance( mV1 ); }

  /** @return the number of points in the path
   */
  @Override
  public int size() { return 2; }


  /** write the path to a data stream - it does nothing by default
   * @param dos   output stream
   */
  @Override
  public void toDataStream( DataOutputStream dos ) throws IOException
  {
    dos.write( 'F' );
    toDataStream( dos, mV1 );
    toDataStream( dos, mV2 );
  }

  /** read from a stream
   * @param cmd  command manager (unused)
   * @param dis  input stream
   * @param version file version
   * @return always 1
   */
  @Override
  public int fromDataStream( SketchCommandManager cmd, DataInputStream dis, int version ) throws IOException
  {
    dataCheck( "SECTION", ( dis.read() == 'F' ) );
    mV1 = tdVectorFromDataStream( dis );
    mV2 = tdVectorFromDataStream( dis );
    mU = mV2.minus( mV1 );
    mU.normalize();
    return 1;
  }

  // -----------------------------------------------------------------------

  /** make projected path in world coords
   */
  @Override
  Path makeProjectedPath( TDVector C, TDVector X, TDVector Y )
  {
    TDVector v1 = mV1.minus( C );
    TDVector v2 = mV2.minus( C );
    Path path = new Path();
    path.moveTo( X.dot( v1 ), Y.dot( v1 ) );
    path.lineTo( X.dot( v2 ), Y.dot( v2 ) );
    return path;
  }


}
