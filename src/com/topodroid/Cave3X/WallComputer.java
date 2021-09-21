/** @file WallComputer.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief Cave3D wall computer interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

import java.util.ArrayList;

public interface WallComputer
{
  public ArrayList< Triangle3D > getTriangles();
}
