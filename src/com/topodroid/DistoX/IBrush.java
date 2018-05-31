/* @file IBrush.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: brush interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Path;

/**
 */
interface IBrush {
    void mouseDown( Path path, float x, float y);
    void mouseMove( Path path, float x, float y);
    void mouseUp( Path path, float x, float y);
}
