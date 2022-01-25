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

  /** cstr - default
   */
  EraseCommand( )
  {
    mActions = new ArrayList<>();
  }

  /** add a new action, or update/remove an existing action
   * @param type  action type (for a new action or to update an existing action)
   * @param path  action sketch item
   * @return true if action has been dropped (the action initial type is INSERT, and the type is REMOVE)
   */
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

  /** close the erase command
   */
  void completeCommand()
  {
    for ( EraseAction action : mActions ) {
      if ( action.mType == EraseAction.ERASE_MODIFY ) {
        action.completeAction();
      }
    }
  }

  /** @return the number of actions in the command
   */
  int size() { return mActions.size(); }

  /** @return the type of the command (namely 1)
   */
  public int commandType() { return 1; }

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

  // /** draw the command - it does nothing
  //  * @param canvas   canvas
  //  * @param mat      transform matrix
  //  * @param scale    transform scale
  //  * @param bbox     clipping box
  //  * @note nothing to draw
  //  */
  // public void draw(Canvas canvas, Matrix mat, float scale, RectF bbox ) { }

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
