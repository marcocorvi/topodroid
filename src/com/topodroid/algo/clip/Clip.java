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

import com.topodroid.util.TDLog;
import com.topodroid.TDX.ICanvasCommand;
import com.topodroid.TDX.DrawingLinePath;
import com.topodroid.TDX.DrawingAreaPath;
import com.topodroid.TDX.LinePoint;
// import com.topodroid.TDX.BrushManager;

import java.util.List;
import java.util.ArrayList;

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


  /** clip an area to the outlines
   * @param stack     list of scrap lines
   * @param area      area to clip
   */
  public static void toOutline( List< ICanvasCommand > stack, DrawingAreaPath area )
  {
    TDLog.v("Clipping: area border " + area.size() + " stack size " + stack.size() );
    LinePoint start = area.middle();
    //
    LinePoint p1clip = null;
    LinePoint lp11 = null, lp12 = null;
    DrawingLinePath ln1clip = null;
    int c2 = 0;
    for ( ICanvasCommand cmd : stack ) {
      if ( ( cmd instanceof DrawingLinePath ) ) {
        ln1clip = (DrawingLinePath)cmd;
        if ( ln1clip.hasOutline() ) {
          boolean cubic = ln1clip.isCubic();
          LinePoint p1 = start;
          for ( LinePoint p2 = area.next( p1 ); p2 != area.first() && p1clip == null; p2 = area.next( p2 ) ) {
            // TDLog.v(" check point " + c2 + ": " + p2.x + " " + p2.y );
            if ( LineIntersection.flag( ln1clip, p1, p2, cubic ) == LineIntersection.INTERSECT ) {
              lp11 = LineIntersection.lpQ1;
              lp12 = LineIntersection.lpQ2;
              p2.x = p1.x + LineIntersection.mT * ( p2.x - p1.x );
              p2.y = p1.y + LineIntersection.mT * ( p2.y - p1.y );
              // TDLog.v(" intersection LAST " + c2 + " at " + LineIntersection.mT + ": " + p2.x + " " + p2.y );
              // TDLog.v("   line point " + lp11.x + " " + lp11.y + " .. " + lp12.x + " " + lp12.y );
              p1clip = p2;
              break; // inner for
            }
            p1 = p2;
            ++ c2;
          }
          if ( p1clip != null ) {
            // TDLog.v(" got p1clip " + p1clip.x + " " + p1clip.y );
            break; // for
          // } else {
          //   TDLog.v(" no p1clip " );
          }
        }
      }
    }
    if ( p1clip == null || ln1clip == null ) {
      TDLog.v("Clipping: No LAST intersection");
      return;
    // } else {
    //   TDLog.v("Ok LAST intersection");
    }
    //
    LinePoint p2clip = null;
    LinePoint lp21 = null, lp22 = null;
    DrawingLinePath ln2clip = ln1clip;
    int c1 = 0;
    // for ( ICanvasCommand cmd : stack ) {
    //   if ( ( cmd instanceof DrawingLinePath ) ) {
    //     ln2clip = (DrawingLinePath)cmd;
    //     if ( ln2clip.hasOutline() ) {
          boolean cubic = ln2clip.isCubic();
          LinePoint p1 = start;
          LinePoint p2 = area.prev( p1 );
          while ( p2 != p1clip ) {
            // TDLog.v(" check point " + c1 + ": " + p2.x + " " + p2.y );
            if ( LineIntersection.flag( ln2clip, p1, p2, cubic ) == LineIntersection.INTERSECT ) {
              lp21 = LineIntersection.lpQ1;
              lp22 = LineIntersection.lpQ2;
              p2.x = p1.x + LineIntersection.mT * ( p2.x - p1.x );
              p2.y = p1.y + LineIntersection.mT * ( p2.y - p1.y );
              // TDLog.v(" intersection FIRST " + c1 + " at " + LineIntersection.mT + ": " + p2.x + " " + p2.y );
              // TDLog.v("   line point " + lp21.x + " " + lp21.y + " .. " + lp22.x + " " + lp22.y );
              p2clip = p2;
              break; // while
            }
            p1 = p2;
            p2 = area.prev( p1 );
            ++ c1;
          }
    //       if ( p2clip != null ) break; // for
    //     }
    //   }
    // }
    if ( p2clip == null || ln2clip == null ) {
      TDLog.v("Clipping: No FIRST intersection");
      return;
    } else {
      TDLog.v("Clipping: both intersections with same line");
    }
    //    ----- q22 -- q21 ------<<------- q12 -- q11 ---- forward = true
    //               |                          |
    //                ---------- > -------------
    //
    //    ----- q21 -- q22 ------>>------- q11 -- q12 ---- forwrad = false
    //               |                          |
    //                ---------- > -------------
    //
    // if ( ln1clip != ln2clip ) {
    //   TDLog.v("Not single line intersection");
    //   return;
    // }
    // retrace area from p2clip to p1clip
    ArrayList< LinePoint > pts = new ArrayList<>();
    for ( LinePoint p = p2clip; p != p1clip; p = p.mNext ) {
      if ( p == null ) {
        TDLog.v("Clipping: failed following area border");
        return;
      }
      pts.add( p );
    }
    pts.add( p1clip );
    TDLog.v("Clipping: area-border points " + pts.size() );
    // intersection line is always traverse forward
    LinePoint last = null;
    if ( lp12 == lp11.mNext ) { // scan forward from lp12
      for ( LinePoint q = lp12; q != null; q = q.mNext ) { // scan line forward
        if ( q == lp21 ) { // add points from lp12 to lp21
          last = lp21;
        } else if ( q == lp22 ) {
          last = lp22;
        }
        if ( last != null ) {
          // TDLog.v( " forward add points from Lp12 " + lp12.x + " " + lp12.y + " to " + last.x + " " + last.y );
          for ( LinePoint qq = lp12; qq != last && qq != null; qq = qq.mNext ) pts.add( qq );
          pts.add( last );
          break;
        }
      }
      if ( last == null ) { // scan backward from lp11
        for ( LinePoint q = lp11; q != null; q = q.mPrev ) { // scan line backward
          if ( q == lp21 ) { // add points from lp12 to lp21
            last = lp21;
          } else if ( q == lp22 ) {
            last = lp22;
          }
          if ( last != null ) {
            // TDLog.v( " backward add points from Lp12 " + lp11.x + " " + lp11.y + " to " + last.x + " " + last.y );
            for ( LinePoint qq = lp11; qq != last && qq != null; qq = qq.mPrev ) pts.add( qq );
            pts.add( last );
            break;
          }
        }
      }
    } else if ( lp12 == lp11.mPrev ) { 
      for ( LinePoint q = lp12; q != null; q = q.mPrev ) { // scan line backward
        if ( q == lp21 ) { // add points from lp12 to lp21
          last = lp21;
        } else if ( q == lp22 ) {
          last = lp22;
        }
        if ( last != null ) {
          // TDLog.v( " backward add points from Lp12 " + lp12.x + " " + lp12.y + " to " + last.x + " " + last.y );
          for ( LinePoint qq = lp12; qq != last && qq != null; qq = qq.mPrev ) pts.add( qq );
          pts.add( last );
          break;
        }
      }
      if ( last == null ) { // scan backward from lp11
        for ( LinePoint q = lp11; q != null; q = q.mNext ) { // scan line forward
          if ( q == lp21 ) { // add points from lp12 to lp21
            last = lp21;
          } else if ( q == lp22 ) {
            last = lp22;
          }
          if ( last != null ) {
            // TDLog.v( " forward add points from Lp12 " + lp11.x + " " + lp11.y + " to " + last.x + " " + last.y );
            for ( LinePoint qq = lp11; qq != last && qq != null; qq = qq.mNext ) pts.add( qq );
            pts.add( last );
            break;
          }
        }
      }
    } 
    if ( last != null && pts.size() > 2 ) {
      TDLog.v("Clipping - points " + pts.size() );
      area.resetPath( pts );
      area.recount();
      area.setClosed( true ); // this calls retracePath();
    } else {
      TDLog.v("Clipping: last is null");
    }
  }

}
