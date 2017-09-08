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
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

/* interface for the canvas commands
 */
public interface ICanvasCommand {
    public int  commandType(); // command type: 0 DrawingPath, 1 EraseCommand
    public void draw(Canvas canvas, RectF bbox );
    public void draw(Canvas canvas, Matrix mat, float scale, RectF bbox );

    // public void undoCommand();

    public void flipXAxis(float z);
    public void shiftPathBy( float x, float y );
    public void scalePathBy( float z, Matrix m );
    public void computeBounds( RectF bounds, boolean b );

}
