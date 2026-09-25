/** @file Scrap.java
 *
 * @author marco corvi
 * @date oct 2019
 *
 * @brief TopoDroid clipping algo
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.algo.clip;

// import com.topodroid.util.TDLog;
import com.topodroid.TDX.ICanvasCommand;
import com.topodroid.TDX.DrawingLinePath;
import com.topodroid.TDX.LinePoint;
// import com.topodroid.TDX.BrushManager;

import java.util.List;

public class Clip
{

  /** clip a line to the outlines
   * @param stack     list of scrap lines
   * @param line      line to clip
   * @param clip_mode clipping mode (1: "last" end, 2: both ends)
   */
  public static void toOutline( List< ICanvasCommand > stack, DrawingLinePath line, int clip_mode )
  {
    // TDLog.v(" clip line - mode " + clip_mode );
    boolean clipped = false;
    LinePoint start = line.first();
    if ( clip_mode == 2 ) {
      start = line.middle();
    }
    for ( ICanvasCommand cmd : stack ) {
      if ( ( cmd instanceof DrawingLinePath ) ) {
        DrawingLinePath ln = (DrawingLinePath)cmd;
        if ( ln.hasOutline() ) {
          boolean cubic = ln.isCubic();
          LinePoint p1 = start;
          LinePoint p2 = line.next( p1 );
          while ( p2 != null ) {
            if ( LineIntersection.flag( ln, p1, p2, cubic ) == LineIntersection.INTERSECT ) {
              p2.x = p1.x + LineIntersection.mT * ( p2.x - p1.x );
              p2.y = p1.y + LineIntersection.mT * ( p2.y - p1.y );
              // TDLog.v(" intersection LAST at " + com.topodroid.algo.LineIntersection.mT + ": " + p2.x + " " + p2.y );
              line.setLast( p2 );
              clipped = true;
              break;
            }
            p1 = p2;
            p2 = line.next( p1 );
          }
          if ( clipped ) break;
        }
      }
    }
    if ( clip_mode == 2 ) {  
      boolean clipped_2 = false;
      for ( ICanvasCommand cmd : stack ) {
        if ( ( cmd instanceof DrawingLinePath ) ) {
          DrawingLinePath ln = (DrawingLinePath)cmd;
          if ( ln.hasOutline() ) {
            boolean cubic = ln.isCubic();
            LinePoint p1 = start;
            LinePoint p2 = line.prev( p1 );
            while ( p2 != null ) {
              if ( LineIntersection.flag( ln, p1, p2, cubic ) == LineIntersection.INTERSECT ) {
                p2.x = p1.x + LineIntersection.mT * ( p2.x - p1.x );
                p2.y = p1.y + LineIntersection.mT * ( p2.y - p1.y );
                // TDLog.v(" intersection FIRST at " + com.topodroid.algo.LineIntersection.mT + ": " + p2.x + " " + p2.y );
                line.setFirst( p2 );
                clipped_2 = true;
                break;
              }
              p1 = p2;
              p2 = line.prev( p1 );
            }
            if ( clipped_2 ) {
              clipped = true;
              break;
            }
          }
        }
      }
    }
    if ( clipped ) {
      line.recount();
      line.retracePath( );
    }
  }


}
