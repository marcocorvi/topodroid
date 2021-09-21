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
 *  @note this object is final because its field are supposed to be accessed read-only
 *        although TDNum, PlotInfo, and DrawingCommandManager are references and, therefore,
 *        the underlying object could be changed
 */
package com.topodroid.Cave3X;

import com.topodroid.num.TDNum;

class PlotSaveData
{
  final PlotInfo plot;
  final long type;     // plot type
  final TDNum num;
  // final DrawingUtil util;
  final DrawingCommandManager cm;
  final String name;   
  final String fname;  // filename
  final int azimuth;   // projected profile azimuth (0 for plan and extended profile)
  final int suffix;    // plot save mode
  final int rotate;    // number of backups to rotate (??)

  PlotSaveData( TDNum n, /* DrawingUtil u, */ PlotInfo p, DrawingCommandManager manager, String nam, String fnam, int a, int s, int r )
  {
    plot = p;
    type = p.type;
    num  = n;
    // util = u;
    cm   = manager;
    name  = nam;
    fname = fnam; // fullname
    azimuth = (int)p.azimuth;
    suffix  = s;
    rotate  = r;
  }
}
