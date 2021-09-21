/* @file IZoomer.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid zoomer interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

interface IZoomer 
{
  // get the value of the zoom
  float zoom();

  void checkZoomBtnsCtrl();
}

