/* @file FixedStation.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: num station with Fixed info
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
// import com.topodroid.TDX.StationInfo;
import com.topodroid.num.NumStation;

public class FixedStation
{
  public FixedInfo  mFixed; // name, comment, source
  public NumStation mNumStation;

  /** cstr
   * @param fi    fixed info associated to the station
   * @param ns    num station 
   */
  public FixedStation( FixedInfo fi, NumStation ns )
  {
    mFixed      = fi;
    mNumStation = ns;
  }

}
  
