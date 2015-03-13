/* @file DrawingPenBrush.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: pen base class
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.graphics.Path;

/**
 */
public class DrawingPenBrush extends DrawingBrush
{
    @Override
    public void mouseDown(Path path, float x, float y) {
        path.moveTo( x, y );
        // path.lineTo(x, y);
    }

    @Override
    public void mouseMove(Path path, float x, float y) {
        path.lineTo( x, y );
    }

    @Override
    public void mouseUp(Path path, float x, float y) {
        path.lineTo( x, y );
    }
}
