/* @file EraseAction.java
 *
 * @author marco corvi
 * @date sept 2015
 *
 * @brief TopoDroid drawing: single erase action
 * 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import android.graphics.Canvas;
// import android.graphics.Paint;
// import android.graphics.Path;
// import android.graphics.Matrix;

import java.util.ArrayList;

class EraseAction // implements ICanvasCommand
{
  static final int ERASE_REMOVE = 0; // drop item
  static final int ERASE_INSERT = 1; // add item
  static final int ERASE_MODIFY = 2; // modify item


  int mInitialType;  // action initial type
  int mType;         // action type
  DrawingPath mPath; // affected path
  private ArrayList< LinePoint > mOldPoints = null;
  private ArrayList< LinePoint > mNewPoints = null;
                 
  /** cstr - copy the path points to the set of "old" points
   * @param type   action type (remove, insert, or modify)
   * @param path   affected sketch item
   */
  EraseAction( int type, DrawingPath path )
  {
    mInitialType = type;
    mType = type;
    mPath = path;
    mNewPoints = null;
    if ( mPath.mType == DrawingPath.DRAWING_PATH_LINE || mPath.mType == DrawingPath.DRAWING_PATH_AREA ) {
      DrawingPointLinePath line = (DrawingPointLinePath)mPath;
      mOldPoints = new ArrayList<>();
      setPoints( mOldPoints, line );
    } else {
      mOldPoints = null;
    }
  }

  /** restore the points of the sketch item - only for line/area
   * @param old   whether to restore the old or the new points
   */
  void restorePoints( boolean old )
  {
    if ( mPath.mType == DrawingPath.DRAWING_PATH_LINE || mPath.mType == DrawingPath.DRAWING_PATH_AREA ) {
      DrawingPointLinePath line = (DrawingPointLinePath)mPath;
      line.resetPath( old ? mOldPoints : mNewPoints );
      if ( mPath.mType == DrawingPath.DRAWING_PATH_AREA ) line.closePath();
    }
  }

  /** close the action - copy the path points to the set of "new" points
   */
  void completeAction()
  {
    mNewPoints = new ArrayList<>();
    DrawingPointLinePath line = (DrawingPointLinePath)mPath;
    setPoints( mNewPoints, line );
  }

  /** copy the line points to a new set
   * @param pts   new set of points
   * @param line  existing point line
   */
  private void setPoints( ArrayList< LinePoint > pts, DrawingPointLinePath line )
  {
    LinePoint prev = null;
    for ( LinePoint lp = line.mFirst; lp != null; lp = lp.mNext ) {
      prev = new LinePoint( lp, prev );
      pts.add( prev );
    }
  }

}
