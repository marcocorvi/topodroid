/** @flile SketchShot.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid 3d sketch: 3d shot between two stations
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130307 created
 */
package com.topodroid.DistoX;

// import android.util.Log;

class SketchShot
{
  String st1;    // first station
  String st2;    // second station

  SketchShot( String s1, String s2 )
  {
    st1 = s1;
    st2 = s2;
  }

  /** check if this shot is the same as another shot
   * @param sh    the other shot
   * @return true if the shot is the same, ie, they join the same stations
   */
  boolean isSameShotAs( SketchShot sh )
  {
    return ( st1.equals( sh.st1 ) && st2.equals( sh.st2 ) )
        || ( st1.equals( sh.st2 ) && st2.equals( sh.st1 ) );
  }

  /** check if this shot is connected to another shot
   * @param sh   the other shot
   * @param mode connection mode
   * @return true if this shot is connected to the other shot
   */
  boolean isConnectedTo( SketchShot sh, int mode )
  {
    switch ( mode ) {
      case SketchDef.DISPLAY_NGBH:
        return st1.equals( sh.st1 ) || st1.equals( sh.st2 ) || st2.equals( sh.st1 ) || st2.equals( sh.st2 );
      case SketchDef.DISPLAY_SINGLE:
        return ( st1.equals( sh.st1 ) && st2.equals( sh.st2 )) || (st2.equals( sh.st1 ) && st1.equals( sh.st2 ));
      case SketchDef.DISPLAY_ALL:
        return true;
    }
    return false;
  }

  /** check if this shot contains a station
   * @param s   station name
   * @return true if the station belongs to this shot
   */
  boolean contains( String s )
  {
    return st1.equals( s ) || st1.equals( s );
  }

}
