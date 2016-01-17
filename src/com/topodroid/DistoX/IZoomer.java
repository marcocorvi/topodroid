/* @file IZoomer.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid zoomer interface
 * --------------------------------------------------------
 *  Copyright: This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

public interface IZoomer 
{
  // get the value of the zoom
  public float zoom();

  public void checkZoomBtnsCtrl();
}

