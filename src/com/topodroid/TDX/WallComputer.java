/** @file WallComputer.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief Cave3D wall computer interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import java.util.ArrayList;

/** wall-computer interface
 */
public interface WallComputer
{
  ArrayList< Triangle3D > getTriangles();
}
