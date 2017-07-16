/* @file IDrawingSurface.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid lister interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.view.SurfaceHolder;

public interface IDrawingSurface 
{
  /**
   * @param holder surface holder
   * 
   * refresh surface
   */
  public void refresh( SurfaceHolder holder );

  public boolean isDrawing();

}
