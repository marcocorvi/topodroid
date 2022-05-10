/* @file SavedStation.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: num station with saved info
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
// import com.topodroid.TDX.StationInfo;
import com.topodroid.num.NumStation;

public class SavedStation
{
  public StationInfo mCurrent; // mName, mComment, mFlag
  public NumStation  mNumStation;

  public SavedStation( StationInfo cs, NumStation ns )
  {
    mCurrent    = cs;
    mNumStation = ns;
  }

}
  
