/* @file TrbSeries.java
 *
 * @author marco corvi
 * @date oct 2023
 *
 * @grief TopoRobot series
 * --------------------------------------------------------
 *  copyright this software is distributed under gpl-3.0 or later
 *  see the file copying.
 * --------------------------------------------------------
 */
package com.topodroid.io.trb;

public class TrbSeries
{
  public int series;
  public int points;
  public int start_series;
  public int start_point;

  public TrbSeries( int sr, int pt, int sr0, int pt0 )
  {
    series = sr;
    points = pt;
    start_series = sr0; 
    start_point  = pt0;
  }
}
