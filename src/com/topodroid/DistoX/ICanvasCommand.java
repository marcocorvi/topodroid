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

/* interface for the canvas commands
 */
public interface ICanvasCommand {
    public void draw(Canvas canvas);
    public void undo();
}
