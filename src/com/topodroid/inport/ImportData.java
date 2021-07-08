/* @file ImportData.java
 *
 * @author marco corvi
 * @date jul 2021
 *
 * @brief TopoDroid import data struct
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.inport;

import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.SurveyInfo;
import com.topodroid.DistoX.TDLevel;

// import android.util.Log;

public class ImportData
{
  public int     mType;  // import type
  public boolean mLrud;
  public boolean mLeg;   // leg first
  public boolean mTrox;
  public int     mDatamode; // Compass

  private boolean mDiving;

  // cstr
  public ImportData()
  {
    mLrud = false;
    mLeg  = false;
    mTrox = TDSetting.mVTopoTrox;
    if ( TDLevel.overExpert ) {
      mDatamode = TDSetting.mImportDatamode;
      mDiving   = ( mDatamode == SurveyInfo.DATAMODE_DIVING );
    } else {
      mDatamode = SurveyInfo.DATAMODE_NORMAL;
      mDiving = false;
    }
  }

  public void setDiving( boolean diving ) 
  {
    if ( TDLevel.overExpert ) {
      mDiving = diving;
      mDatamode = mDiving? SurveyInfo.DATAMODE_DIVING : SurveyInfo.DATAMODE_NORMAL;
    }
  }

  public boolean getDiving() { return mDiving; }

}
