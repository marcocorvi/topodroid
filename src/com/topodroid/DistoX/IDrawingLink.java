/* @file IDrawingLink.java
 *
 * @author marco corvi
 * @date jan 2019
 *
 * @brief TopoDroid 2D-point link for a drawing item 
 *        It is used to connect a (section) point to the point link of the item
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.math.Point2D;

interface IDrawingLink
{
  float getLinkX();
  float getLinkY();
  // Point2D getLink(); // TODO
}
