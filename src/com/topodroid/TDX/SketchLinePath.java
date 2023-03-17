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
import java.io.IOException;

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
  int mId;  // line ID - the full ID is composed of the line ID and the section ID
  int mSid; // line section ID
  ArrayList< SketchPoint > mPts;

  /** cstr
   * @param type   path type: POINT (1), LINE (2), AREA (3) etc.
   */
  public SketchLinePath( int id, int sid, Paint paint )
  {
    super( SketchPath.SKETCH_PATH_LINE, paint );
    mId  = id;
    mSid = sid;
    mPts = new ArrayList< SketchPoint >();
  }

  /** append a point to the line
   * @param v   point 3D vector
   */
  SketchPoint appendPoint( TDVector v )
  { 
    if ( v == null ) return null;
    // TDLog.v("LINE " + mId + "." + mSid + " add pt " + v.x + " " + v.y + " " + v.z );
    SketchPoint ret = new SketchPoint( v, this );
    mPts.add( ret );
    return ret;
  }


  /** @return the line ID
   */
  int getId() { return mId; }

  /** @return the ID of the line section
   */
  int getSectionId() { return mSid; }

  /** @return the number of points on the line
   */
  @Override
  public int size() { return mPts.size(); }

  /** clear the path
   */
  void clear() { mPts.clear(); }

  /** @return the i-th point
   * @param i  point index
   */
  SketchPoint get( int i ) { return mPts.get( i ); }

  /** write the path to a data stream - it does nothing by default
   * @param dos   output stream
   */
  @Override
  public void toDataStream( DataOutputStream dos ) throws IOException
  {
    dos.write( 'L' );
    dos.writeInt( mId );
    dos.writeInt( mSid );
    dos.writeInt( mPts.size() );
    for ( SketchPoint pt : mPts ) toDataStream( dos, pt );
    // TDLog.v("WRITE line " + mId + "." + mSid + " n.pts " + mPts.size() );
    if ( mPts.size() > 1 ) {
      mPts.get(0).dump("  P[0]");
      mPts.get(mPts.size()-1).dump("  P[N]");
    }
  }

  /** read from a stream
   * @param cmd  command manager (unused)
   * @param dis  input stream
   * @param version file version
   * @return the number of points on the line
   */
  @Override
  public int fromDataStream( SketchCommandManager cmd, DataInputStream dis, int version ) throws IOException
  {
    dataCheck( "LINE", ( dis.read() == 'L' ) );
    mId  = dis.readInt();
    int sid = dis.readInt(); dataCheck( "SID", ( sid == mSid ) );
    int npt = dis.readInt();
    for ( int k=0; k<npt; ++ k ) {
      mPts.add( new SketchPoint( tdVectorFromDataStream( dis ), this ) );
    }
    // TDLog.v("READ line " + mId + "." + mSid + " n.pts " + npt );
    return npt;
  }

  boolean checkInPlane( TDVector C, TDVector N )
  {
    for ( SketchPoint p : mPts ) {
      dataCheck( "line point", ( Math.abs( p.minus(C).dot(N) ) < 0.001 ) );
    }
    return true;
  }

  /** make projected path
   */
  @Override
  Path makeProjectedPath( TDVector C, TDVector X, TDVector Y )
  {
    int sz = mPts.size();
    if ( sz > 2 ) {
      TDVector v = mPts.get(0).minus( C );
      Path path = new Path();
      path.moveTo( X.dot( v ), Y.dot( v ) );
      for ( int k=1; k<sz; ++k ) {
        v = mPts.get(k).minus( C );
        path.lineTo( X.dot( v ), Y.dot( v ) );
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

  /** erase line points
   * @param v    erase center (a point in the section plane)
   * @param eraseCmd
   * @param erase_size
   */
  void eraseAt( TDVector v, EraseCommand eraseCmd, float erase_size ) 
  {
    float min_diff = 10000f;
    for ( int k = 0; k < mPts.size(); ) {
      SketchPoint p = mPts.get(k);
      float diff = p.maxDiff( v );
      if ( diff < min_diff ) min_diff = diff;
      if ( diff < erase_size ) { 
        mPts.remove( p );
      } else {
        ++k;
      }
    }
    // TDLog.v("LINE erase at: min diff " + min_diff );
  }

  /** draw the path on a canvas
   * @param canvas   canvas - N.B. canvas is guaranteed not null
   * @param matrix   transform matrix
   * @param C        center (E,N,Up)
   * @param X        horizontal direction (E,N,Up)
   * @param Y        downward direction in (E,N,Up)
   * @param r        dot radius
   */
  void drawPoints( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y, float r )
  {
    if ( ! mVisible ) return;
    Path path = new Path();
    for ( SketchPoint p : mPts ) {
      TDVector v = p.minus( C );
      float x = X.dot( v );
      float y = Y.dot( v ); // downaward
      path.addCircle( x, y, r, Path.Direction.CCW );
    }
    path.transform( mm );
    // path.offset( off_x, off_y );
    canvas.drawPath( path, BrushManager.fixedOrangePaint );
  }

  /** draw the path on a canvas
   * @param canvas   canvas - N.B. canvas is guaranteed not null
   * @param matrix   transform matrix
   * @param C        center (E,N,Up)
   * @param X        horizontal direction (E,N,Up)
   * @param Y        downward direction in (E,N,Up)
   */
  public void drawBipath( Canvas canvas, Matrix matrix, TDVector C, TDVector X, TDVector Y )
  {
    if ( ! mVisible ) return;
    int sz = mPts.size();
    if ( sz <= 2 ) return;
    TDVector Z = X.cross( Y );
    boolean above = true;
    int n_above = 0;
    int n_below = 0;
    Path path_above = new Path();
    Path path_below = new Path();
    TDVector v = mPts.get(0).minus( C );
    float x = X.dot( v);
    float y = Y.dot( v);
    if ( Z.dot( v ) < 0 ) {
      n_below ++;
      path_below.moveTo( x, y );
      above = false;
    } else {
      n_above ++;
      path_above.moveTo( x, y );
      above = true;
    }
    for ( int k=1; k<sz; ++k ) {
      v = mPts.get(k).minus( C );
      x = X.dot( v);
      y = Y.dot( v);
      if ( Z.dot( v ) < 0 ) {
        n_below ++;
        if ( above ) {
          n_above ++;
          path_above.lineTo( x, y );
          path_below.moveTo( x, y );
        } else {
          path_below.lineTo( x, y );
        }
        above = false;
      } else {
        n_above ++;
        if ( above ) {
          path_above.lineTo( x, y );
        } else {
          n_below ++;
          path_below.lineTo( x, y );
          path_above.moveTo( x, y );
        }
        above = true;
      }
    }

    if ( n_above > 1 ) {
      path_above.transform( matrix );
      // path.offset( off_x, off_y );
      canvas.drawPath( path_above, BrushManager.bgLinePaint );
    }
    if ( n_below > 1 ) {
      path_below.transform( matrix );
      // path.offset( off_x, off_y );
      canvas.drawPath( path_below, BrushManager.fgLinePaint );
    }
  }

}
