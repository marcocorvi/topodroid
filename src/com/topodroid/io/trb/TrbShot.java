/* @file TrbShot.java
 *
 * @author marco corvi
 * @date oct 2023
 *
 * @grief TopoRobot shot
 * --------------------------------------------------------
 *  copyright this software is distributed under gpl-3.0 or later
 *  see the file copying.
 * --------------------------------------------------------
 */
package com.topodroid.io.trb;

import com.topodroid.TDX.DBlock;

public class TrbShot
{
  public TrbSeries series;  // series of this shot
  public DBlock    block;   // shot data
  public TrbShot   next;    // next shot in the series
  public boolean   forward; // whether the shot is forward in the series

  public TrbShot( TrbSeries sr, DBlock blk, boolean fwd )
  {
    series = sr;
    block  = blk;
    next   = null;
    forward = fwd;
  }

  public void setNext( TrbShot shot ) { next = shot; }

}

