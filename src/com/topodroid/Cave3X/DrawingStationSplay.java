/* @file DrawingSatationSplay.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: drawing station splays on/off
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

// import com.topodroid.prefs.TDSetting;

import java.util.ArrayList;


class DrawingStationSplay
{
  private ArrayList< String > mSplaysOn;  // stations where to show splays
  private ArrayList< String > mSplaysOff; // stations where not to show splays

  DrawingStationSplay()
  {
    mSplaysOn  = new ArrayList<>();
    mSplaysOff = new ArrayList<>();
  }

  void reset()
  {
    mSplaysOn.clear();
    mSplaysOff.clear();
  }

  boolean hasSplaysON()  { return mSplaysOn.size() > 0; }
  // boolean hasSplaysOFF() { return mSplaysOff.size() > 0; }

  // check whether an array of stations name contains the FROM station of the path's block
  // used to decide whether to display splays
  // @param p         drawing path 
  // @param stations  array of station names
  private boolean containsStation( DrawingPath p, ArrayList< String > splays )
  {
    DBlock blk = p.mBlock;
    if ( blk == null ) return false;
    String station = blk.mFrom;
    if ( station == null || station.length() == 0 ) return false;
    return splays.contains( station );
  }

  boolean isStationON( DrawingPath p )  { return containsStation( p, mSplaysOn  ); }
  boolean isStationOFF( DrawingPath p ) { return containsStation( p, mSplaysOff ); }

  boolean isStationSplaysOn( String st_name )
  {
    if ( st_name == null ) return false;
    return mSplaysOn.contains( st_name );
  }

  boolean isStationSplaysOff( String st_name )
  {
    if ( st_name == null ) return false;
    return mSplaysOff.contains( st_name );
  }

  void toggleStationSplays( String station, boolean on, boolean off )
  {
    if ( station == null ) return;
    setStationSplays( mSplaysOn,  station, on );
    setStationSplays( mSplaysOff, station, off );
  }

  private void setStationSplays( ArrayList< String > splayStations, String station, boolean on )
  {
    if ( splayStations.contains( station ) ) {
      if ( ! on ) splayStations.remove( station );
    } else {
      if ( on ) splayStations.add( station );
    }
  }

  void hideStationSplays( String station )
  {
    if ( station == null ) return;
    /* if ( mSplaysOn.contains( station ) ) */ mSplaysOn.remove( station );
    if ( ! mSplaysOff.contains( station ) ) mSplaysOff.add( station );
  }

  void showStationSplays( String station )
  {
    if ( station == null ) return;
    /* if ( mSplaysOff.contains( station ) ) */ mSplaysOff.remove( station );
    if ( ! mSplaysOn.contains( station ) ) mSplaysOn.add( station );
  }

}
