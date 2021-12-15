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
package com.topodroid.DistoX;

// import com.topodroid.prefs.TDSetting;

import java.util.ArrayList;


class DrawingStationSplay
{
  private ArrayList< String > mSplaysOn;  // stations where to show splays
  private ArrayList< String > mSplaysOff; // stations where not to show splays

  /** cstr - initialize structures
   */
  DrawingStationSplay()
  {
    mSplaysOn  = new ArrayList<>();
    mSplaysOff = new ArrayList<>();
  }

  /** clear structures
   */
  void reset()
  {
    mSplaysOn.clear();
    mSplaysOff.clear();
  }

  /** @return true if there is at least one splay ON
   */
  boolean hasSplaysON()  { return mSplaysOn.size() > 0; }

  // /** @return true if there is at least one splay OFF
  //  */
  // boolean hasSplaysOFF() { return mSplaysOff.size() > 0; }

  /** check whether an array of stations name contains the FROM station of the path's block
   * used to decide whether to display splays
   * @param p       drawing path (of a data-block)
   * @param splays  array of station names
   * @return true if the path comes from a data-block and the list os stations contains the block FROM station
   */
  private boolean containsStation( DrawingPath p, ArrayList< String > splays )
  {
    DBlock blk = p.mBlock;
    if ( blk == null ) return false;
    String station = blk.mFrom;
    if ( station == null || station.length() == 0 ) return false;
    return splays.contains( station );
  }

  /** @return true if the FROM station of the path is in the ON-list
   * @param p       drawing path (of a data-block)
   */
  boolean isStationON( DrawingPath p )  { return containsStation( p, mSplaysOn  ); }

  /** @return true if the FROM station of the path is in the OFF-list
   * @param p       drawing path (of a data-block)
   */
  boolean isStationOFF( DrawingPath p ) { return containsStation( p, mSplaysOff ); }

  /** @return true if the station is in the ON-list
   * @param st_name   station name
   */
  boolean isStationSplaysOn( String st_name )
  {
    if ( st_name == null ) return false;
    return mSplaysOn.contains( st_name );
  }

  /** @return true if the station is in the OFF-list
   * @param st_name   station name
   */
  boolean isStationSplaysOff( String st_name )
  {
    if ( st_name == null ) return false;
    return mSplaysOff.contains( st_name );
  }

  /** add/remove a station from the lists
   * @param station          station
   * @param add_on           whether to add to the ON list
   * @param add_off          whether to add to the OFF list
   */
  void toggleStationSplays( String station, boolean add_on, boolean add_off )
  {
    if ( station == null ) return;
    setStationSplays( mSplaysOn,  station, add_on );
    setStationSplays( mSplaysOff, station, add_off );
  }

  /** add/remove a station from a list
   * @param splayStations    list
   * @param station          station
   * @param add              whether to add or remove
   */
  private void setStationSplays( ArrayList< String > splayStations, String station, boolean add )
  {
    if ( splayStations.contains( station ) ) {
      if ( ! add ) splayStations.remove( station );
    } else {
      if ( add ) splayStations.add( station );
    }
  }

  /** hide splays at a station
   * @param station    station
   */
  void hideStationSplays( String station )
  {
    if ( station == null ) return;
    /* if ( mSplaysOn.contains( station ) ) */ mSplaysOn.remove( station );
    if ( ! mSplaysOff.contains( station ) ) mSplaysOff.add( station );
  }

  /** show splays at a station
   * @param station    station
   */
  void showStationSplays( String station )
  {
    if ( station == null ) return;
    /* if ( mSplaysOff.contains( station ) ) */ mSplaysOff.remove( station );
    if ( ! mSplaysOn.contains( station ) ) mSplaysOn.add( station );
  }

}
