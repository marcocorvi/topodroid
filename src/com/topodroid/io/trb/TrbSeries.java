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

import com.topodroid.utils.TDLog;
import com.topodroid.TDX.DBlock;

public class TrbSeries
{
  public int series;
  public int points = 0;
  public int start_series;
  public int start_point;
  public int end_series;
  public int end_point;

  private TrbShot first_shot;
  private TrbShot last_shot;
  private int     nr_shots; // DEBUG

  public TrbSeries( int sr, /* int pt, */ int sr0, int pt0 )
  {
    series = sr;
    // points = pt;
    start_series = sr0; 
    start_point  = pt0;
    first_shot = null;
    last_shot  = null;
    nr_shots   = 0;
    end_series = -1;
    end_point  = -1;
  }

  /** set the number of points (ie shots)
   * @param pts   number of points
   */
  public void setPoints( int pts ) 
  {
    points    = pts;
    // TDLog.v("TRB series " + series + " points " + points + " shots " + nr_shots + " start " + start_series + "." + start_point );
  }

  /** set the series end-point
   * @param sr   end series
   * @param pt   end point
   */
  public void setEndPoint( int sr, int pt )
  {
    end_series = sr;
    end_point  = pt;
  }

  /** increase the number of points (= nr of shots)
   */
  public void increaseNrPoints() { points ++; }
  

  /** append a new shot to the list
   * @param blk   data block
   * @param forward whether the shot is forward
   */
  public void appendShot( DBlock blk, boolean forward )
  { 
    TrbShot s = new TrbShot( this, blk, forward );
    if ( last_shot != null ) {
      last_shot.setNext( s );
    } else {
      assert( first_shot == null );
      first_shot = s;
    }
    last_shot = s;
    ++ nr_shots;
  }

  /** @return the first shot of the (linked) list of shots
   */
  public TrbShot getShots() { return first_shot; }

  /** @return the number of shots in this series
   */
  public int getNrShots() { return nr_shots; }

  public void dumpBlocks()
  {
    TrbShot shot = first_shot;
    for ( ; shot != null; shot = shot.next ) {
      DBlock b = shot.block;
      TDLog.v("TRB block " + b.mFrom + "-" + b.mTo );
    }
  }
}
