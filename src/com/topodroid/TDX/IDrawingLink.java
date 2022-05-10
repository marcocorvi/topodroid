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
package com.topodroid.TDX;

// import com.topodroid.math.Point2D;

/** link to a drawing item
 */
public interface IDrawingLink
{
  /** @return the X coordinate of the linked item
   */
  public float getLinkX();

  /** @return the Y coordinate of the linked item
   */
  public float getLinkY();

  // Point2D getLink(); // TODO
}
