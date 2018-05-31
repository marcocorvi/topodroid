/* @file IDrawingSurface.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid lister interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.view.SurfaceHolder;

interface IDrawingSurface 
{
  /**
   * @param holder surface holder
   * 
   * refresh surface
   */
  void refresh( SurfaceHolder holder );

  boolean isDrawing();

}
