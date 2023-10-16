/* @file TrbStruct.java
 *
 * @author marco corvi
 * @date oct 2023
 *
 * @grief TopoRobot struct
 * --------------------------------------------------------
 *  copyright this software is distributed under gpl-3.0 or later
 *  see the file copying.
 * --------------------------------------------------------
 */
package com.topodroid.io.trb;

import java.util.ArrayList;
import java.util.HashMap;

import com.topodroid.TDX.DBlock;

public class TrbStruct
{
  ArrayList< TrbSeries > mTrbSeries;
  ArrayList< DBlock > mShots;
  HashMap< String, String > mMap;

  public TrbStruct()
  {
    mMap = new HashMap< String, String >();
    mShots  = new ArrayList< DBlock >();
    mTrbSeries = new ArrayList< TrbSeries >();
  }

  public String getTrb( String st ) { return mMap.get( st ); }

  public void addShot( DBlock b ) { mShots.add( b ); }

  public void addSeries( TrbSeries sr ) { mTrbSeries.add( sr ); }

  public void put( String k, String v ) { mMap.put( k, v ); }

  public String get( String k ) { return mMap.get( k ); }

  public ArrayList< TrbSeries > getSeries() { return mTrbSeries; }

  public ArrayList< DBlock > getShots() { return mShots; }

  public int getNrSeries() { return mTrbSeries.size(); }

  public int getNrStations() { return mMap.size(); }

  public int getNrShots() { return mShots.size(); }

}
