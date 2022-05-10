/** @file BubbleComputer.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief Cave3D simple Tube computer
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.bubble;

import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.Cave3DShot;
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.TDX.Triangle3D;
import com.topodroid.TDX.WallComputer;

import java.util.ArrayList;

public class BubbleComputer implements WallComputer
{
  private TglParser mParser;
  private ArrayList< Cave3DShot > splays;
  private ArrayList< Cave3DShot > shots;
  private ArrayList< Cave3DStation > stations;
  private ArrayList< Triangle3D > triangles = null;

  public BubbleComputer( TglParser parser )
  {
    mParser  = parser;
    shots    = parser.getShots();
    splays   = parser.getSplays();
    stations = parser.getStations();
  }

  public ArrayList< Triangle3D > getTriangles() { return triangles; }

  final int[] colors = { 0xffff0000, 0xffffff00, 0xff00ff00, 0xff00ffff, 0xff0000ff, 0xffff00ff };

  private Bubble getBubble( Cave3DStation st, ArrayList<Bubble> bubbles )
  {
    for ( Bubble bb : bubbles ) if ( bb.getCenter() == st ) return bb;
    return null;
  }

  public boolean computeBubble()
  {
    // TDLog.v( "Bubble computer: " + stations.size() + " " + shots.size() + " " + splays.size() );
    ArrayList< Bubble > bubbles = new ArrayList<>();
    int kcol = 0;
    for ( Cave3DStation st : stations ) {
      // st = center of the bubble
      ArrayList< Point3S > pts = new ArrayList<>();
      for ( Cave3DShot sh : splays ) {
        if ( sh.from_station == st ) {
          pts.add( new Point3S( sh.toPoint3D(), st ) );
        }
      }
      Bubble bb = new Bubble( st, pts );
      if ( bb.prepareDelaunay() ) bubbles.add( bb );
    }
    // TDLog.v( "Bubble computer: bubbles " + bubbles.size() );
    for ( Cave3DShot sh : shots ) {
      Bubble bF = getBubble( sh.from_station, bubbles );
      if ( bF != null ) {
        Bubble bT = getBubble( sh.to_station, bubbles );
        if ( bT != null ) {
          ArrayList<Triangle3S> trFi = bF.computeInsides( bT );
          ArrayList<Triangle3S> trTi = bT.computeInsides( bF );
          ArrayList<Triangle3S> trFr = bF.computeReducedTriangles( trFi, bT );
          ArrayList<Triangle3S> trTr = bT.computeReducedTriangles( trTi, bF );
          bF.reduce( trFi, trFr );
          bT.reduce( trTi, trTr );
        }
      }
    }
    
    triangles = new ArrayList< Triangle3D >();
    for ( Bubble bb : bubbles ) {
      for ( Triangle3S tr : bb.getTriangles() ) triangles.add( new Triangle3D( tr.v1(), tr.v2(), tr.v3(), 0 ) );
    }
    // TDLog.v( "Bubble computer: triangles " + triangles.size() );
    return ( triangles.size() > 0 );
  }

}

