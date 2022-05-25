/* @file DrawingBrush.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: brush base class, empty-drawing on a path
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import android.graphics.Path;

/**
 * generic brush
 */
class DrawingBrush implements  IBrush
{
    public void mouseDown(Path path, float x, float y) {
    }

    public void mouseMove(Path path, float x, float y) {
    }

    public void mouseUp(Path path, float x, float y) {
    }
}
