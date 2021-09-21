/* @file PlotSave.java
 *
 * @author marco corvi
 * @date jan 2015
 *
 * @brief TopoDroid drawing: saving modes
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

class PlotSave
{
  final static int NONE     = 0;
  final static int SAVE     = 1; // save binary (onPause and onBackPressed)
  final static int EXPORT   = 3; // save as therion, svg, ect.
  final static int HANDLER  = 2;
  final static int TOGGLE   = 4; 
  final static int MODIFIED = 5; 
  final static int OVERVIEW = 6; // used by OverviewWindow to save whole therion, svg, etc.
  final static int CREATE   = 7; 

}
