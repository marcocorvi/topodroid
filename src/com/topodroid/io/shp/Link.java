/* @file Link.java
 *
 * @author marco corvi
 * @date feb 2022
 *
 * @brief TopoDroid drawing: link from a 2D point to another 2D item
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.io.shp;

// import com.topodroid.utils.TDLog;
import com.topodroid.DistoX.DrawingPointPath;
// import com.topodroid.DistoX.DrawingUtil;
import com.topodroid.DistoX.IDrawingLink;

class Link
{
  DrawingPointPath pt;
  // IDrawingLink link;
  double x = 0;
  double y = 0;
  
  /** cstr
   * @param path   drawing point with a link
   */
  public Link( DrawingPointPath path )
  {
    pt = path;
    IDrawingLink link = path.mLink;
    if ( link != null ) {
      x = link.getLinkX();
      y = link.getLinkY();
    }
  }

  /** @return the level of the point
   */
  int level() { return pt.mLevel; }

  /** @return the scrap index of the point
   */
  int scrap() { return pt.mScrap; }
}
