/* @file EraseCommand.java
 *
 * @author marco corvi
 * @date sept 2015
 *
 * @brief TopoDroid drawing: atomic erase command
 * 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.PrintWriter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.RectF;

// import android.util.FloatMath;
import java.util.ArrayList;
import android.util.Log;

/**
 */
public class EraseCommand implements ICanvasCommand
{
  ArrayList< EraseAction > mActions;

  EraseCommand( )
  {
    mActions = new ArrayList< EraseAction >();
  }

  void addAction( int type, DrawingPath path )
  {
    for ( EraseAction action : mActions ) {
      if ( action.mPath == path ) {
        action.mType = type; // update action type
        return;
      }
    }
    mActions.add( new EraseAction( type, path ) );
  }

  void complete()
  {
    for ( EraseAction action : mActions ) {
      if ( action.mType == EraseAction.ERASE_MODIFY ) {
        action.complete();
      }
    }
  }

  int size() { return mActions.size(); }


  public int  commandType() { return 1; }

  // nothing to draw
  public void draw(Canvas canvas) { }
  public void draw(Canvas canvas, Matrix mat ) { }

  public void flipXAxis() { } 
  public void shiftPathBy( float x, float y ) { }
  public void computeBounds( RectF bounds, boolean b ) { }
  
  // public void undoCommand() 
  // {
  // }
}
