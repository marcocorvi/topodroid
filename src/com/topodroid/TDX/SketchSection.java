/* @file SketchSecion.java
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

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;


public class SketchSection extends SketchPath
{
  SketchPoint mP1;
  SketchPoint mP2;
  TDVector    mC;   // midpoint
  TDVector    mN;   // normal
  TDVector    mH;   // X unit rightward
  TDVector    mS;   // Y unit downward
  ArrayList< SketchLinePath > mLines; // lines, in the plane of the section

  public SketchSection( TDVector c, TDVector h, TDVector s, TDVector n )
  {
    super( SketchPath.SKETCH_PATH_SECTION, null ); // null Paint
    mP1 = null;
    mP2 = null;
    mC = c;
    mH = h;
    mS = s;
    mN = n;
    mLines = new ArrayList< SketchLinePath >();
    TDLog.v("SKETCH legviews C " + c.x + " " + c.y + " " + c.z );
  }

  /** cstr
   * @param p1   first base point
   * @param p2   second base point
   * @param vertical 
   */
  public SketchSection( SketchPoint p1, SketchPoint p2, boolean vertical )
  {
    super( SketchPath.SKETCH_PATH_SECTION, null ); // null Paint
    mP1 = p1;
    mP2 = p2;
    mC  = new TDVector( (p1.x+p2.x)/2, (p1.y+p2.y)/2, (p1.z+p2.z)/2 );
    float dx = p2.x - p1.x; // East
    float dy = p2.y - p1.y; // North
    float dz = p2.z - p1.z; // Upward
    TDLog.v("SKETCH section (vert " + vertical + ") " + dx + " " + dy + " " + dz );
    float dh = TDMath.sqrt( dx*dx + dy*dy );
    if ( vertical ) {
      mS = new TDVector(      0,     0, -1 );
      mH = new TDVector(  dx/dh, dy/dh,  0 );
      mN = new TDVector( -dy/dh, dx/dh,  0 ); // H ^ S
    } else {
      mN = new TDVector(      0,     0, -1 ); // downward
      mH = new TDVector(  dx/dh,  dy/dh, 0 );
      mS = new TDVector(  dy/dh, -dx/dh, 0 ); // N ^ H
    }
    mLines = new ArrayList< SketchLinePath >();
  }

  /** @return the world 3D vector of a canvas point
   * @param xw    H world coord
   * @param yw    S world coord
   */
  TDVector toTDVector( float xw, float yw )
  {
    return new TDVector( mC.x + xw*mH.x + yw*mS.x, mC.y + xw*mH.y + yw*mS.y, mC.z + xw*mH.z + yw*mS.z );
  }

  /** delete a line
   * @param line   line to delete
   */
  void deleteLine( SketchLinePath line ) { mLines.remove( line ); }

  /** append a line
   * @param line   line to append
   */
  void appendLine( SketchLinePath line ) { mLines.add( line ); }

  @Override
  public int size() { return mLines.size(); }

  /** clear the section
   */
  void clear() { mLines.clear(); }

  /** @return true if this section has the specified base points
   * @param p1    first section base-point
   * @param p2    second section base-point
   */
  boolean hasBase( SketchPoint p1, SketchPoint p2 )
  {
    if ( p1 == mP1 && p2 == mP2 ) return true;
    if ( p2 == mP1 && p1 == mP2 ) return true;
    return false;
  }

  /** write the path to a data stream - it does nothing by default
   * @param dos   output stream
   */
  @Override
  public void toDataStream( DataOutputStream dos ) { TDLog.Error( "ERROR Sketch Section toDataStream "); }

  @Override
  public void fromDataStream( DataInputStream dis ) { TDLog.Error( "ERROR Sketch Section fromDataStream "); }


  // public void draw( Canvas canvas, TDVector C, TDVector X, TDVector Y, float zoom, float off_x, float off_y )
  // {
  //   for ( SketchLinePath line : mLines ) line.draw( canvas, C, X, Y, zoom, off_x, off_y );
  // }

  public void draw( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y, float zoom, float off_x, float off_y )
  {
    for ( SketchLinePath line : mLines ) line.draw( canvas, mm, C, X, Y, zoom, off_x, off_y );
  }

  public void drawPoints( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y, float zoom, float off_x, float off_y, float r )
  {
    for ( SketchLinePath line : mLines ) line.drawPoints( canvas, mm, C, X, Y, zoom, off_x, off_y, r );
  }

  void addEraseCommand( EraseCommand cmd ) 
  { 
    TDLog.v("TODO Add erase command ");
  }
  
  void eraseAt( float x, float y, float zoom, EraseCommand eraseCmd, float erase_size ) 
  {
    TDLog.v("TODO erase at " + x + " " + y + " zoom " + zoom );
  }

  public void undo () { TDLog.v("TODO undo"); }

  public void redo () { TDLog.v("TODO redo"); }

  boolean hasMoreRedo() { return false; }

  boolean hasMoreUndo() { return false; }

  boolean setRangeAt( float x, float y, float zoom, float size ) { TDLog.v("TODO set range at"); return false; }

}
