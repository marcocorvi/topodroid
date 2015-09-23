/* @file ICanvasCommand.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: canvas command interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

/* interface for the canvas commands
 */
public interface ICanvasCommand {
    public int  commandType(); // command type: 0 DrawingPath, 1 EraseCommand
    public void draw(Canvas canvas);
    public void draw(Canvas canvas, Matrix mat );

    // public void undoCommand();

    public void flipXAxis();
    public void shiftPathBy( float x, float y );
    public void computeBounds( RectF bounds, boolean b );

}
