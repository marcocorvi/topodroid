/* @file SketchWall.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketching: section
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

// import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;


public class SketchWall extends SketchPath
{
  ArrayList< SketchSection > mSections;
  ArrayList< SketchFixedPath > mWalls;
  private boolean mCanDraw = false;

  /** cstr
   */
  public SketchWall( Paint paint )
  {
    super( SketchPath.SKETCH_PATH_WALL, paint ); 
    mSections = new ArrayList< SketchSection >();
    mWalls    = new ArrayList< SketchFixedPath >();
    mCanDraw  = false;
  }

  /** delete a section
   * @param section   section to delete
   */
  void deleteSection( SketchSection section ) { if ( mSections != null ) mSections.remove( section ); }

  /** append a section
   * @param section   section to append
   */
  void appendSection( SketchSection section ) { if ( mSections != null ) mSections.add( section ); }

  @Override
  public int size() { return (mSections == null)? 0 : mSections.size(); }

  /** clear the sections
   */
  void clear() 
  {
    if ( mSections != null) mSections.clear();
    if ( mWalls != null) mWalls.clear();
    mCanDraw = false;
  }

  /** make the wall lines
   * @note vectors in [ E, N, Up ] frame
   * @param X   trasversal horizontal unit vector: (uy, -ux, 0)
   * @param U   leg unit vector (ux, uy, uz)
   * @param Z   "vertical" unit vector, (0,0,1) for "horizontal" leg, (ux, uy, 0) for "vertical" leg
   *
   *      z |   ,-' u leg
   *        |,-'
   *     ---+------- n = (ux, uy, 0)
   *        |
   *        |
   *  let ud = sqrt( ux*ux + uy*uy )
   *  X = ( uy x - ux y ) / ud
   *  U = ux x + uy y + uz z
   *  Z =                  z
   *  let Y = U - uz z
   *      ux Y + ud uy X = ud^2 x
   *      uy Y - ud ux X = ud^2 y
   *
   *  P = px x + py y + pz z
   *    = px ( uy/ud X + ux / ud^2 Y ) + py ( -ux/ud X + uy / ud^2 Y ) + pz Z
   *    + (px uy - py ux)/ud X + (px ux + py uy)/ud^2 U + ( pz - (px ux + py uy)/ud^2 ) Z
   *
   *  X is orthogonal to U and Z, but U Z = uz
   *  thus the sum of the squares on non-orthogonal components is (rx=ux/ud, etc., rr=1+rz^2 )
   *  (PX)^2 + (PU)^2 + (PZ)^2 = px^2 (ry^2 + rr rx^2) + py^2 (rx^2 + rr ry^2) + 2 px py rx ry rz^2 + pz^2 - 2 px pz rx rz - 2 py pz ry rz
   *
   */
  void makeLines( /* TDVector x, */ TDVector u /*, TDVector z */ )
  {
    mCanDraw = false;
    if ( mWalls != null) mWalls.clear();
    if ( ! orderSections( u ) ) return;
    for ( SketchSection section : mSections ) section.makeProjMatrix( u );

    // TODO use d2 = x^2 + y^2 + z^2 (1 + uy^2)/un^2 - 2 y z uy/un
    // float ud TDMath.sqrt( ux*ux + uy*uy );
    // float rx = ux / ud;  float rx2 = rx * rx;
    // float ry = uy / ud;  float ry2 = ry * ry;
    // float rz = uz / ud;  
    // float rr = 1 + rz * rz;
    // float rxz = 2 * rx * rz;
    // float ryz = 2 * ry * rz;
    // float rxx = ry2 + rr * rx2;
    // float ryy = rx2 + rr * ry2;
    // float rxy = 2 * rx * ry * rz * rz;

    int sz = size();
    if ( sz == 0 ) return;
    SketchSection s1 = mSections.get( 0 );
    for ( int k2 = 1; k2 < sz; ++ k2 ) {
      SketchSection s2 = mSections.get( k2 );
      for ( SketchLinePath l1 : s1.mLines ) {
        for ( SketchPoint p1 : l1.mPts ) {
          TDVector v1 = new TDVector();
          float d1 = s2.project( p1, v1 );
          SketchPoint pp1 = null;
          float dmin = 1000000;
          for ( SketchLinePath l2 : s2.mLines ) {
            for ( SketchPoint p2 : l2.mPts ) {
              // float dx = p2.x - p1.x;
              // float dy = p2.y - p1.y;
              // float dz = p2.z - p1.z;
              // float d  = rxx * dx*dx + rxy * dx*dy + ryy * dy*dy + dz*dz - rxz * dx*dz - ryz * dy*dz;
              float d = p2.distance( v1 );
              if ( d < dmin ) {
                pp1 = p2;
                dmin = d;
              }
            }
          }
          if (pp1 != null ) {
            mWalls.add( new SketchFixedPath( SketchPath.SKETCH_PATH_WALL, mPaint, p1, pp1 ) );
          }
        }
      }
      s1 = s2;
    }
    mCanDraw = true;
  }

  /** order sections by increasing projections of the center along a vector
   * @param u   vector
   */
  private boolean orderSections( TDVector u ) 
  {
    int sz = size();
    if ( sz <= 1 ) return false;
    for ( int k1 = 0; k1 < sz; ++ k1 ) {
      SketchSection s1 = mSections.get( k1 );
      float d1 = s1.mC.dot ( u );
      for ( int k2 = k1 + 1; k2 < sz; ++ k2 ) {
        SketchSection s2 = mSections.get( k2 );
        float d2 = s2.mC.dot ( u );
        if ( d2 < d1 ) {
          mSections.set( k1, s2 );
          mSections.set( k2, s1 );
          d1 = d2;
        }
      }
    }
    return true;
  }


  public void draw( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y )
  {
    if ( ! mCanDraw ) return;
    if ( mWalls == null ) return;
    for ( SketchPath line : mWalls ) line.draw( canvas, mm, C, X, Y );
  }

  public void undo () { TDLog.v("TODO undo"); }

  public void redo () { TDLog.v("TODO redo"); }

  boolean hasMoreRedo() { return false; }

  boolean hasMoreUndo() { return false; }


  /** write the path to a data stream - it does nothing by default
   * @param dos   output stream
   */
  @Override
  public void toDataStream( DataOutputStream dos ) throws IOException 
  {
    // TDLog.v("WRITE section " + mId + " type " + mType + " max line-ID " + nMaxLineId + " lines " + mWalls.size() );
    dos.write( 'W' );
    dos.writeInt( mSections.size() );
    for ( SketchSection section : mSections ) {
      dos.writeInt( section.getId() );
    }
    dos.writeInt( mWalls.size() );
    for ( SketchFixedPath line : mWalls ) {
      line.toDataStream( dos );
    }
  }

  /** read from a stream
   * @param cmd  command manager - use to add line to this section
   * @param dis  input stream
   * @param version file version
   * @return 0 - UNUSED
   * @note the command manager must be opened on this section
   */
  @Override
  public int fromDataStream( SketchCommandManager cmd, DataInputStream dis, int version ) throws IOException
  {
    float alpha = 0;
    dataCheck( "WALL", ( dis.read() == 'W' ) );
    int nr_sections = dis.readInt();
    for ( int k=0; k < nr_sections; ++k ) {
      int sid = dis.readInt();
      mSections.add( cmd.getSection( sid ) );
    }
    int nr_walls    = dis.readInt();
    for ( int k=0; k < nr_walls; ++ k ) {
      SketchFixedPath line = new SketchFixedPath( SketchPath.SKETCH_PATH_WALL, mPaint, null, null );
      line.fromDataStream( cmd, dis, version );
      mWalls.add( line );
    }
    // cmd.setWall( this ); // TODO
    // TDLog.v("READ wall ");
    return 0;
  }

}
