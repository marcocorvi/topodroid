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
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
// import com.topodroid.DistoX.CurrentStation;
import com.topodroid.num.NumStation;

public class FixedStation
{
  public FixedInfo  mFixed; // name, comment, source
  public NumStation mNumStation;

  public FixedStation( FixedInfo fi, NumStation ns )
  {
    mFixed      = fi;
    mNumStation = ns;
  }

}
  
