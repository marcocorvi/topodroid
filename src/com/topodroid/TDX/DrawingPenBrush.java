/* @file DrawingPenBrush.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: pen base class drawing a path - used for the "current path"
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import android.graphics.Path;

class DrawingPenBrush extends DrawingBrush
{
    /** implements a "mouse-down": move the path to a point (x,y)
     * @param path    path
     * @param x       X coord of the point
     * @param y       Y coord of the point
     */
    @Override
    public void mouseDown(Path path, float x, float y) {
        path.moveTo( x, y );
        // path.lineTo(x, y);
    }

    /** implements a "mouse-draw": trace the path to a point (x,y)
     * @param path    path
     * @param x       X coord of the point
     * @param y       Y coord of the point
     */
    @Override
    public void mouseMove(Path path, float x, float y) {
        path.lineTo( x, y );
    }

    /** implements a "mouse-up": trace the path to a point (x,y)
     * @param path    path
     * @param x       X coord of the point
     * @param y       Y coord of the point
     */
    @Override
    public void mouseUp(Path path, float x, float y) {
        path.lineTo( x, y );
    }
}
