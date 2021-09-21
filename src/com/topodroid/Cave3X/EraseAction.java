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
package com.topodroid.Cave3X;

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


  int mInitialType;  // action inital type
  int mType;         // action type
  DrawingPath mPath; // affected path
  private ArrayList< LinePoint > mOldPoints = null;
  private ArrayList< LinePoint > mNewPoints = null;
                 
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

  void restorePoints( boolean old )
  {
    if ( mPath.mType == DrawingPath.DRAWING_PATH_LINE || mPath.mType == DrawingPath.DRAWING_PATH_AREA ) {
      DrawingPointLinePath line = (DrawingPointLinePath)mPath;
      line.resetPath( old ? mOldPoints : mNewPoints );
      if ( mPath.mType == DrawingPath.DRAWING_PATH_AREA ) line.closePath();
    }
  }

  void completeAction()
  {
    mNewPoints = new ArrayList<>();
    DrawingPointLinePath line = (DrawingPointLinePath)mPath;
    setPoints( mNewPoints, line );
  }

  private void setPoints( ArrayList< LinePoint > pts, DrawingPointLinePath line )
  {
    LinePoint prev = null;
    for ( LinePoint lp = line.mFirst; lp != null; lp = lp.mNext ) {
      prev = new LinePoint( lp, prev );
      pts.add( prev );
    }
  }

}
