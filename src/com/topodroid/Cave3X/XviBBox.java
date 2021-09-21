/* @file XviBBox.java
 *
 * @author marco corvi
 * @date jan 2019
 *
 * @brief TopoDroid: bounding box of xvi export
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

import com.topodroid.utils.TDLog;

import android.graphics.RectF;

class XviBBox
{
  float xmin, xmax;
  float ymin, ymax;
  int width;
  int height;

  XviBBox( DrawingCommandManager plot ) { init( plot.getBoundingBox( ) ); }

  XviBBox( RectF bbox ) { init( bbox ); }

  private void init( RectF bbox )
  {
    xmin = bbox.left;
    xmax = bbox.right;
    ymin = bbox.top;
    ymax = bbox.bottom;
    int dx = (int)(xmax - xmin);
    int dy = (int)(ymax - ymin);
    if ( dx > 200 ) dx = 200;
    if ( dy > 200 ) dy = 200;
    xmin -= dx;
    xmax += dx;
    ymin -= dy;
    ymax += dy;
    width  = (int)((xmax - xmin));
    height = (int)((ymax - ymin));
  }
}
