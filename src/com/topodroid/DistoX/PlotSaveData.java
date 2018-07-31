/* @file PlotSaveData.java
 *
 * @author marco corvi
 * @date jul 2018
 *
 * @brief TopoDroid sketch saving data struct
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class PlotSaveData
{
  PlotInfo plot;
  long type;
  DistoXNum num;
  DrawingUtil util;
  DrawingCommandManager cm;
  String name;
  String fname;
  int azimuth;
  int suffix;
  int rotate;

  PlotSaveData( DistoXNum n, DrawingUtil u, PlotInfo p, DrawingCommandManager manager, String nam, String fnam, int a, int s, int r )
  {
    plot = p;
    type = p.type;
    num  = n;
    util = u;
    cm   = manager;
    name  = nam;
    fname = fnam; // fullname
    azimuth = (int)p.azimuth;
    suffix  = s;
    rotate  = r;
  }
}
