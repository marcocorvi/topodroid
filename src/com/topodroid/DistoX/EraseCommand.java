/* @file EraseCommand.java
 *
 * @author marco corvi
 * @date sept 2015
 *
 * @brief TopoDroid drawing: atomic erase command
 * 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
// import android.graphics.Paint;
// import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.RectF;

import java.util.ArrayList;

class EraseCommand implements ICanvasCommand
{
  ArrayList< EraseAction > mActions;

  EraseCommand( )
  {
    mActions = new ArrayList<>();
  }

  // return true if action has been dropped
  boolean addAction( int type, DrawingPath path )
  {
    // if ( type != EraseAction.ERASE_INSERT ) {
      for ( EraseAction action : mActions ) {
        if ( action.mPath == path ) {
          if ( action.mInitialType == EraseAction.ERASE_INSERT && type == EraseAction.ERASE_REMOVE ) {
            // FIXME: must remove action path from selection
            mActions.remove( action );
            return true;
          }
          action.mType = type; // update action type
          return false;
        }
      }
    // }
    mActions.add( new EraseAction( type, path ) );
    return false;
  }

  void completeCommand()
  {
    for ( EraseAction action : mActions ) {
      if ( action.mType == EraseAction.ERASE_MODIFY ) {
        action.completeAction();
      }
    }
  }

  int size() { return mActions.size(); }


  public int commandType() { return 1; }

  // nothing to draw
  public void draw(Canvas canvas, RectF bbox) { }
  public void draw(Canvas canvas, Matrix mat, float scale, RectF bbox ) { }

  // from ICanvasCommand
  public void flipXAxis(float z) { } 
  public void shiftPathBy( float x, float y ) { }
  public void scalePathBy( float z, Matrix m ) { }
  public void affineTransformPathBy( float[] mm, Matrix m ) { }
  public void computeBounds( RectF bounds, boolean b ) { }
  
  // public void undoCommand() 
  // {
  // }
}
