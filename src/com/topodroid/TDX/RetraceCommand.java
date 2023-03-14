/* @file RetraceCommand.java
 *
 * @author marco corvi
 * @date sept 2015
 *
 * @brief TopoDroid drawing: line/area retrace
 * 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import android.graphics.Canvas;
// import android.graphics.Paint;
// import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.RectF;

import java.util.ArrayList;

class RetraceCommand implements ICanvasCommand
{
  DrawingPointLinePath mPath;
  LinePoint mLPq1, mLPq2; // new PointLinePath endpoints
  LinePoint mLPp1, mLPp2; // old PointLinePath endpoints
  LinePoint mLPp10; // start connection
  LinePoint mLPp20; // end connection
  LinePoint mFirst;
  LinePoint mLast;

  /** cstr - default
   * @param path    point-line path
   * @param lp1     old path start point
   * @param lp2     old path end point
   * @param lq1     new path start point
   * @param lq2     new path end point
   * @param lp10
   * @param lp20
   * @note the command is intantiated after the retrace has been carried out
   *       therefore q1 and q2 are used to get lp10 and lp20
   */
  RetraceCommand( DrawingPointLinePath path, LinePoint lp1, LinePoint lp2, LinePoint lq1, LinePoint lq2, LinePoint lp10, LinePoint lp20 )
  {
    mPath  = path;
    mLPp1  = lp1;
    mLPp2  = lp2;
    mLPq1  = lq1;
    mLPq2  = lq2;
    mLPp10 = lp10;
    mLPp20 = lp20;
    mFirst = path.first();
    mLast  = path.last();
  }

  void undo() { relink( mLPp1, mLPp2 ); }

  void redo() { relink( mLPq1, mLPq2 ); }

  /** relink p10 and p20
   * @patam p1   point p10 is linked to
   * @patam p2   point p20 is linked from
   */
  private void relink( LinePoint p1, LinePoint p2 )
  {
    if ( mPath instanceof DrawingAreaPath ) {
      mPath.chainFirstLast();
      mLPp10.mNext = p1;
      p1.mPrev = mLPp10;
      mLPp20.mPrev = p2;
      p2.mNext = mLPp20;
      // mPath.resetFirstLast( mFirst, mLast );
      mPath.resetFirstLast( p1, mLPp10 );
    } else if ( mPath instanceof DrawingLinePath ) {
      if ( p1 != null ) {
        if ( mLPp10 != null ) {
          mLPp10.mNext = p1;
        } else {
          mPath.setFirst( p1 );
        }
        p1.mPrev = mLPp10;
      } else {
        mPath.setFirst( mFirst );
      }
      if ( p2 != null ) {
        if ( mLPp20 != null ) {
          mLPp20.mPrev = p2;
        } else {
          mPath.setLast( p2 );
        }
        p2.mNext = mLPp20;
      } else {
        mPath.setLast( mLast );
      }
    }
    mPath.recomputeSize();
    mPath.retracePath();
  }

  /** @return the type of the command (namely 2)
   */
  public int commandType() { return 2; }

  /** draw the command - it does nothing
   * @param canvas   canvas
   * @param bbox     clipping box
   * @note nothing to draw
   */
  public void draw(Canvas canvas, RectF bbox) { }

  /** draw the command - it does nothing
   * @param canvas   canvas
   * @param mat      transform matrix
   * @param bbox     clipping box
   * @note nothing to draw
   */
  public void draw(Canvas canvas, Matrix mat, RectF bbox ) { }

  /** draw the command - it does nothing
   * @param canvas   canvas
   * @param mat      transform matrix
   * @param scale    transform scale
   * @param bbox     clipping box
   */
  public void draw(Canvas canvas, Matrix mat, float scale, RectF bbox ) { }

  /** draw the command - it does nothing
   * @param canvas   canvas
   * @param mat      transform matrix
   * @param scale    transform scale
   * @param bbox     clipping box
   * @param xor_color xoring color
   */
  public void draw(Canvas canvas, Matrix mat, float scale, RectF bbox, int xor_color ) { }

  // ------------- from ICanvasCommand
  /** flip X axis - it does nothing
   */
  public void flipXAxis(float z) { } 

  /** shift - it does nothing
   * @param x  X shift
   * @param y  Y shift
   */
  public void shiftPathBy( float x, float y ) { }

  /** rescale - it does nothing
   * @param z  zoom factor
   * @param m  transform matrix
   */
  public void scalePathBy( float z, Matrix m ) { }

  /** affine transform - it does nothing
   * @param mm transform coefficients
   * @param m  transform matrix
   */
  public void affineTransformPathBy( float[] mm, Matrix m ) { }

  /** compute the bounding box - it does nothing
   * @param bounds   bounding box (unaffected)
   * @param b        not used (see android.graphics.Path)
   */
  public void computeBounds( RectF bounds, boolean b ) { }
  
  // public void undoCommand() 
  // {
  // }
}
